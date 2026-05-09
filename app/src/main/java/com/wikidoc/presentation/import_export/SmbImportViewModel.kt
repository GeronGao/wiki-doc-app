package com.wikidoc.presentation.import_export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wikidoc.data.smb.SmbService
import com.wikidoc.domain.model.Document
import com.wikidoc.domain.model.Folder
import com.wikidoc.domain.repository.DocumentRepository
import com.wikidoc.domain.repository.FolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SmbImportViewModel @Inject constructor(
    private val smbService: SmbService,
    private val documentRepository: DocumentRepository,
    private val folderRepository: FolderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmbImportUiState())
    val uiState: StateFlow<SmbImportUiState> = _uiState.asStateFlow()

    private val folderCache = mutableMapOf<String, Long>()

    fun connect(serverAddress: String, username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isConnecting = true, connectionError = null) }

            smbService.connect(serverAddress, username, password).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isConnecting = false,
                            isConnected = true,
                            serverAddress = serverAddress,
                            username = username,
                            password = password
                        )
                    }
                    loadShares()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isConnecting = false,
                            connectionError = error.message ?: "连接失败"
                        )
                    }
                }
            )
        }
    }

    private fun loadShares() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            smbService.listShares().fold(
                onSuccess = { shares ->
                    val items = shares.map { it.toSmbFileItem() }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentPath = "",
                            items = items
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            connectionError = error.message ?: "加载失败"
                        )
                    }
                }
            )
        }
    }

    fun navigateToPath(path: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, currentPath = path) }

            smbService.listDirectory(path).fold(
                onSuccess = { files ->
                    val items = files.map { it.toSmbFileItem() }
                    _uiState.update {
                        it.copy(isLoading = false, items = items)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            connectionError = error.message ?: "加载失败"
                        )
                    }
                }
            )
        }
    }

    fun navigateUp(): Boolean {
        val currentPath = _uiState.value.currentPath
        if (currentPath.isEmpty()) return false

        val parts = currentPath.trim('/').split("/")
        if (parts.size <= 1) {
            loadShares()
            _uiState.update { it.copy(currentPath = "") }
        } else {
            val parentPath = parts.dropLast(1).joinToString("/")
            navigateToPath("/$parentPath")
        }
        return true
    }

    fun toggleItemSelection(path: String) {
        _uiState.update { state ->
            val newSelected = if (state.selectedItems.contains(path)) {
                state.selectedItems - path
            } else {
                state.selectedItems + path
            }
            state.copy(selectedItems = newSelected)
        }
    }

    fun expandFolder(path: String) {
        viewModelScope.launch {
            val currentItems = _uiState.value.items.toMutableList()
            val index = currentItems.indexOfFirst { it.path == path }
            if (index != -1 && currentItems[index].children.isEmpty()) {
                currentItems[index] = currentItems[index].copy(isLoading = true)
                _uiState.update { it.copy(items = currentItems) }

                smbService.listDirectory(path).fold(
                    onSuccess = { files ->
                        val children = files.map { it.toSmbFileItem() }
                        currentItems[index] = currentItems[index].copy(
                            isLoading = false,
                            isExpanded = true,
                            children = children
                        )
                        _uiState.update { it.copy(items = currentItems) }
                    },
                    onFailure = {
                        currentItems[index] = currentItems[index].copy(isLoading = false)
                        _uiState.update { it.copy(items = currentItems) }
                    }
                )
            } else if (index != -1) {
                currentItems[index] = currentItems[index].copy(isExpanded = !currentItems[index].isExpanded)
                _uiState.update { it.copy(items = currentItems) }
            }
        }
    }

    fun selectAll() {
        val allSelectable = getAllSelectableItems(_uiState.value.items)
        _uiState.update { it.copy(selectedItems = allSelectable.toSet()) }
    }

    fun deselectAll() {
        _uiState.update { it.copy(selectedItems = emptySet()) }
    }

    private fun getAllSelectableItems(items: List<SmbFileItem>): List<String> {
        val result = mutableListOf<String>()
        for (item in items) {
            if ((item.isDirectory || item.isSupported) && !item.name.endsWith("\$")) {
                result.add(item.path)
            }
            result.addAll(getAllSelectableItems(item.children))
        }
        return result
    }

    fun importSelected() {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, importProgress = 0f, importStatus = "准备导入...") }

            val selectedPaths = _uiState.value.selectedItems.toList()
            if (selectedPaths.isEmpty()) {
                _uiState.update { it.copy(isImporting = false) }
                return@launch
            }

            val tempDir = File.createTempFile("wikidoc_import_", "")
            tempDir.delete()
            tempDir.mkdirs()

            try {
                folderCache.clear()
                var importedCount = 0
                val totalItems = selectedPaths.size

                for ((index, path) in selectedPaths.withIndex()) {
                    _uiState.update {
                        it.copy(
                            importProgress = (index.toFloat() / totalItems),
                            importStatus = "正在导入: ${path.substringAfterLast("/")}"
                        )
                    }

                    val item = findItemByPath(_uiState.value.items, path)
                    if (item != null) {
                        val result = downloadAndImportItem(item, tempDir.absolutePath)
                        if (result.isSuccess) {
                            importedCount += result.getOrDefault(0)
                        }
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
            } finally {
                tempDir.deleteRecursively()
            }
        }
    }

    private suspend fun downloadAndImportItem(item: SmbFileItem, basePath: String): Result<Int> {
        var importedCount = 0

        when {
            item.isDirectory -> {
                importedCount += downloadAndImportDirectory(item, basePath, item.name)
            }
            item.isMarkdownFile -> {
                val result = downloadAndSaveFile(item, basePath, null)
                if (result.isSuccess) importedCount++
            }
            item.isZipFile -> {
                val result = downloadAndProcessZip(item, basePath)
                if (result.isSuccess) importedCount += result.getOrDefault(0)
            }
        }

        return Result.success(importedCount)
    }

    private suspend fun downloadAndImportDirectory(
        item: SmbFileItem,
        basePath: String,
        folderName: String
    ): Int {
        var importedCount = 0
        val localDir = File(basePath, folderName)
        localDir.mkdirs()

        smbService.listDirectory(item.path).fold(
            onSuccess = { files ->
                for (file in files) {
                    when {
                        file.isDirectory -> {
                            val subItem = file.toSmbFileItem()
                            importedCount += downloadAndImportDirectory(subItem, localDir.absolutePath, file.name)
                        }
                        file.name.endsWith(".md", ignoreCase = true) ||
                        file.name.endsWith(".markdown", ignoreCase = true) -> {
                            val tempFile = File.createTempFile("import_", ".md")
                            val downloadResult = smbService.downloadFile(file.path, tempFile)
                            if (downloadResult.isSuccess) {
                                val doc = createDocumentFromFile(tempFile, localDir.absolutePath, null)
                                if (doc != null) {
                                    documentRepository.saveDocument(doc)
                                    importedCount++
                                }
                            }
                            tempFile.delete()
                        }
                        file.name.endsWith(".zip", ignoreCase = true) -> {
                            val tempFile = File.createTempFile("import_", ".zip")
                            val downloadResult = smbService.downloadFile(file.path, tempFile)
                            if (downloadResult.isSuccess) {
                                val count = downloadAndProcessZipFromFile(tempFile, localDir.absolutePath)
                                importedCount += count
                            }
                            tempFile.delete()
                        }
                    }
                }
            },
            onFailure = { }
        )

        return importedCount
    }

    private val SmbFileItem.isMarkdownFile: Boolean
        get() = name.endsWith(".md", ignoreCase = true) || name.endsWith(".markdown", ignoreCase = true)

    private val SmbFileItem.isZipFile: Boolean
        get() = name.endsWith(".zip", ignoreCase = true)

    private suspend fun downloadAndSaveFile(
        item: SmbFileItem,
        basePath: String,
        parentFolderId: Long?
    ): Result<Unit> {
        val tempFile = File.createTempFile("import_", ".md")
        return smbService.downloadFile(item.path, tempFile).map {
            createDocumentFromFile(tempFile, basePath, parentFolderId)?.let { doc ->
                documentRepository.saveDocument(doc)
            }
            tempFile.delete()
        }
    }

    private suspend fun downloadAndProcessZip(
        item: SmbFileItem,
        basePath: String
    ): Result<Int> {
        val tempFile = File.createTempFile("import_", ".zip")
        return smbService.downloadFile(item.path, tempFile).map {
            val count = downloadAndProcessZipFromFile(tempFile, basePath)
            tempFile.delete()
            count
        }
    }

    private suspend fun downloadAndProcessZipFromFile(
        zipFile: File,
        basePath: String
    ): Int {
        var importedCount = 0
        val zipDirName = zipFile.nameWithoutExtension
        val localDir = File(basePath, zipDirName)
        localDir.mkdirs()

        try {
            java.util.zip.ZipInputStream(zipFile.inputStream()).use { zipInput ->
                var entry = zipInput.nextEntry
                while (entry != null) {
                    val entryName = entry.name.removePrefix("/").removePrefix("\\")
                    val fileName = entryName.substringAfterLast("/")
                    val parentPath = entryName.substringBeforeLast("/", "")

                    if (!entry.isDirectory && fileName.endsWith(".md", ignoreCase = true)) {
                        val parentDirs = if (parentPath.isNotEmpty()) {
                            parentPath.replace("/", "_").replace("\\", "_")
                        } else ""

                        val dirName = if (parentDirs.isNotEmpty()) {
                            "${zipDirName}_${parentDirs}"
                        } else {
                            zipDirName
                        }

                        val docDir = File(localDir.parentFile, dirName)
                        docDir.mkdirs()

                        val localFile = File(docDir, fileName)
                        localFile.outputStream().use { output ->
                            zipInput.copyTo(output)
                        }

                        val doc = Document(
                            title = fileName.removeSuffix(".md").removeSuffix(".MD"),
                            content = localFile.readText(),
                            folderId = null,
                            isFavorite = false
                        )
                        documentRepository.saveDocument(doc)
                        importedCount++
                    }

                    zipInput.closeEntry()
                    entry = zipInput.nextEntry
                }
            }
        } catch (e: Exception) {
        }

        return importedCount
    }

    private suspend fun createDocumentFromFile(
        file: File,
        basePath: String,
        parentFolderId: Long?
    ): Document? {
        return try {
            val content = file.readText()
            val title = file.nameWithoutExtension

            Document(
                title = title,
                content = content,
                folderId = parentFolderId,
                isFavorite = false
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun findItemByPath(items: List<SmbFileItem>, path: String): SmbFileItem? {
        for (item in items) {
            if (item.path == path) return item
            if (item.children.isNotEmpty()) {
                val found = findItemByPath(item.children, path)
                if (found != null) return found
            }
        }
        return null
    }

    fun resetImportState() {
        _uiState.update {
            it.copy(
                isImporting = false,
                importProgress = 0f,
                importStatus = "",
                isImportComplete = false,
                importedCount = 0
            )
        }
    }

    fun disconnect() {
        smbService.disconnect()
        _uiState.update {
            SmbImportUiState()
        }
    }
}
