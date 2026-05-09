package com.wikidoc.presentation.import_export

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wikidoc.domain.model.Document
import com.wikidoc.domain.model.Folder
import com.wikidoc.domain.repository.DocumentRepository
import com.wikidoc.domain.repository.FolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class LocalImportViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val folderRepository: FolderRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocalImportUiState())
    val uiState: StateFlow<LocalImportUiState> = _uiState.asStateFlow()

    fun processSelectedFiles(uris: List<Uri>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, importProgress = 0f, importStatus = "准备导入...") }

            var importedCount = 0
            val totalFiles = uris.size

            for ((index, uri) in uris.withIndex()) {
                val fileName = getFileName(uri) ?: "未知文件"
                _uiState.update {
                    it.copy(
                        importProgress = (index.toFloat() / totalFiles),
                        importStatus = "正在导入: $fileName"
                    )
                }

                val result = processFile(uri, fileName)
                if (result.isSuccess) {
                    importedCount += result.getOrDefault(0)
                }
            }

            _uiState.update {
                it.copy(
                    isImporting = false,
                    importProgress = 1f,
                    importStatus = "导入完成",
                    isImportComplete = true,
                    importedCount = importedCount
                )
            }
        }
    }

    private suspend fun processFile(uri: Uri, fileName: String): Result<Int> = withContext(Dispatchers.IO) {
        var importedCount = 0

        try {
            val tempFile = File.createTempFile("import_", ".tmp")
            tempFile.outputStream().use { output ->
                context.contentResolver.openInputStream(uri)?.use { input ->
                    input.copyTo(output)
                }
            }

            val originalName = fileName.substringBeforeLast(".")

            when {
                fileName.endsWith(".zip", ignoreCase = true) -> {
                    importedCount = processZipFile(tempFile, originalName)
                }
                fileName.endsWith(".md", ignoreCase = true) ||
                fileName.endsWith(".markdown", ignoreCase = true) -> {
                    importedCount = processMdFile(tempFile)
                }
                else -> {
                    tempFile.delete()
                    return@withContext Result.failure(Exception("不支持的文件格式"))
                }
            }

            tempFile.delete()
            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun processZipFile(zipFile: File, originalName: String): Int {
        var importedCount = 0
        val zipRootName = originalName.ifEmpty { "导入文档" }

        try {
            val cacheDir = File(context.cacheDir, "imports")
            cacheDir.mkdirs()
            val extractDir = File(cacheDir, "extract_${System.currentTimeMillis()}")
            extractDir.mkdirs()

            java.util.zip.ZipInputStream(zipFile.inputStream()).use { zipInput ->
                var entry = zipInput.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val entryName = entry.name.removePrefix("/").removePrefix("\\")
                        val fileName = entryName.substringAfterLast("/")

                        if (fileName.endsWith(".md", ignoreCase = true)) {
                            val outputFile = File(extractDir, entryName)
                            outputFile.parentFile?.mkdirs()
                            outputFile.outputStream().use { output ->
                                zipInput.copyTo(output)
                            }
                        }
                    }
                    zipInput.closeEntry()
                    entry = zipInput.nextEntry
                }
            }

            val rootFolderId = getOrCreateFolder(zipRootName, null, mutableMapOf())

            val allMdFiles = extractDir.walkTopDown().filter { it.isFile && it.name.endsWith(".md", true) }.toList()
            val totalFiles = allMdFiles.size

            allMdFiles.forEachIndexed { index, mdFile ->
                _uiState.update {
                    it.copy(
                        importProgress = (index.toFloat() / totalFiles),
                        importStatus = "正在导入: ${mdFile.name}"
                    )
                }

                val relativePath = mdFile.relativeTo(extractDir).parent ?: ""
                val folderNames = if (relativePath.isNotEmpty()) {
                    relativePath.split(File.separator).filter { it.isNotEmpty() }
                } else emptyList()

                val parentFolderId = if (folderNames.isNotEmpty()) {
                    createNestedFolderStructure(zipRootName, folderNames, mutableMapOf())
                } else rootFolderId

                val content = mdFile.readText()
                val title = mdFile.nameWithoutExtension.removeSuffix(".MD")

                val doc = Document(
                    title = title,
                    content = content,
                    folderId = parentFolderId,
                    isFavorite = false
                )
                documentRepository.saveDocument(doc)
                importedCount++
            }

            extractDir.deleteRecursively()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return importedCount
    }

    private suspend fun createNestedFolderStructure(
        rootName: String,
        folderNames: List<String>,
        folderCache: MutableMap<String, Long>
    ): Long {
        var currentParentId: Long? = null

        for ((index, folderName) in folderNames.withIndex()) {
            val parentFolderName = if (index == 0) rootName else folderNames[index - 1]
            currentParentId = getOrCreateFolder(folderName, currentParentId, folderCache)
        }

        return currentParentId ?: getOrCreateFolder(rootName, null, folderCache)
    }

    private suspend fun getOrCreateFolder(
        name: String,
        parentId: Long?,
        folderCache: MutableMap<String, Long>
    ): Long {
        val cacheKey = "${parentId ?: "root"}/$name"

        if (folderCache.containsKey(cacheKey)) {
            return folderCache[cacheKey]!!
        }

        val existingFolders = folderRepository.getAllFolders().first()
        val existing = existingFolders.find { it.name == name && it.parentId == parentId }

        val folderId = if (existing != null) {
            existing.id
        } else {
            val folder = Folder(
                name = name,
                parentId = parentId,
                createdAt = System.currentTimeMillis()
            )
            folderRepository.saveFolder(folder)
        }

        folderCache[cacheKey] = folderId
        return folderId
    }

    private suspend fun processMdFile(file: File): Int {
        return try {
            val content = file.readText()
            val title = file.nameWithoutExtension

            val doc = Document(
                title = title,
                content = content,
                folderId = null,
                isFavorite = false
            )
            documentRepository.saveDocument(doc)
            1
        } catch (e: Exception) {
            0
        }
    }

    private fun getFileName(uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    cursor.getString(nameIndex)
                } else {
                    uri.lastPathSegment
                }
            }
        } catch (e: Exception) {
            uri.lastPathSegment
        }
    }

    fun resetImportState() {
        _uiState.update {
            LocalImportUiState()
        }
    }
}

data class LocalImportUiState(
    val isImporting: Boolean = false,
    val importProgress: Float = 0f,
    val importStatus: String = "",
    val isImportComplete: Boolean = false,
    val importedCount: Int = 0
)
