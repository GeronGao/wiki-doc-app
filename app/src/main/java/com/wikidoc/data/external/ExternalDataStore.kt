package com.wikidoc.data.external

import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import com.wikidoc.data.local.database.entity.DocumentEntity
import com.wikidoc.data.local.database.entity.FolderEntity
import com.wikidoc.data.local.database.entity.ImageEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ExternalDocument(
    val id: Long,
    val title: String,
    val content: String,
    val folderId: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val tags: String,
    val isFavorite: Boolean,
    val wordCount: Int
)

@Serializable
data class ExternalFolder(
    val id: Long,
    val name: String,
    val parentId: Long?,
    val color: String?,
    val icon: String?,
    val sortOrder: Int,
    val createdAt: Long
)

@Serializable
data class ExternalImage(
    val id: Long,
    val fileName: String,
    val originalName: String,
    val path: String,
    val mimeType: String,
    val size: Long,
    val width: Int?,
    val height: Int?,
    val documentId: Long?,
    val createdAt: Long
)

@Serializable
data class WikiDataStore(
    val version: Int = 1,
    val documents: List<ExternalDocument> = emptyList(),
    val folders: List<ExternalFolder> = emptyList(),
    val images: List<ExternalImage> = emptyList(),
    val nextDocumentId: Long = 1,
    val nextFolderId: Long = 1,
    val nextImageId: Long = 1
)

@Singleton
class ExternalDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _documents = MutableStateFlow<List<DocumentEntity>>(emptyList())
    val documents: Flow<List<DocumentEntity>> = _documents.asStateFlow()

    private val _folders = MutableStateFlow<List<FolderEntity>>(emptyList())
    val folders: Flow<List<FolderEntity>> = _folders.asStateFlow()

    private val _images = MutableStateFlow<List<ImageEntity>>(emptyList())
    val images: Flow<List<ImageEntity>> = _images.asStateFlow()

    private var dataStore = WikiDataStore()
    private var dataFile: File? = null
    private var initialized = false

    private fun getDataDirectory(): File {
        val wikiDir = File(context.getExternalFilesDir(null), "WikiDoc")
        if (!wikiDir.exists()) {
            wikiDir.mkdirs()
        }
        return wikiDir
    }

    private fun getDataFile(): File {
        if (dataFile == null) {
            dataFile = File(getDataDirectory(), "wikidoc_data.json")
        }
        return dataFile!!
    }

    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (initialized) return@withContext

        val file = getDataFile()
        if (file.exists()) {
            try {
                val content = file.readText()
                if (content.isNotBlank()) {
                    dataStore = json.decodeFromString<WikiDataStore>(content)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                dataStore = WikiDataStore()
            }
        }

        _documents.value = dataStore.documents.map { it.toEntity() }
        _folders.value = dataStore.folders.map { it.toEntity() }
        _images.value = dataStore.images.map { it.toEntity() }

        initialized = true
    }

    private suspend fun save() = withContext(Dispatchers.IO) {
        try {
            val file = getDataFile()
            val content = json.encodeToString(dataStore)
            file.writeText(content)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun insertDocument(document: DocumentEntity): Long = withContext(Dispatchers.IO) {
        val newId = dataStore.nextDocumentId
        val newDocument = document.copy(id = newId)
        dataStore = dataStore.copy(
            documents = dataStore.documents + newDocument.toExternal(),
            nextDocumentId = newId + 1
        )
        _documents.value = dataStore.documents.map { it.toEntity() }
        save()
        newId
    }

    suspend fun updateDocument(document: DocumentEntity) = withContext(Dispatchers.IO) {
        dataStore = dataStore.copy(
            documents = dataStore.documents.map {
                if (it.id == document.id) document.toExternal() else it
            }
        )
        _documents.value = dataStore.documents.map { it.toEntity() }
        save()
    }

    suspend fun deleteDocument(id: Long) = withContext(Dispatchers.IO) {
        dataStore = dataStore.copy(
            documents = dataStore.documents.filter { it.id != id }
        )
        _documents.value = dataStore.documents.map { it.toEntity() }
        save()
    }

    suspend fun getDocumentById(id: Long): DocumentEntity? = withContext(Dispatchers.IO) {
        dataStore.documents.find { it.id == id }?.toEntity()
    }

    suspend fun insertFolder(folder: FolderEntity): Long = withContext(Dispatchers.IO) {
        val newId = dataStore.nextFolderId
        val newFolder = folder.copy(id = newId)
        dataStore = dataStore.copy(
            folders = dataStore.folders + newFolder.toExternal(),
            nextFolderId = newId + 1
        )
        _folders.value = dataStore.folders.map { it.toEntity() }
        save()
        newId
    }

    suspend fun updateFolder(folder: FolderEntity) = withContext(Dispatchers.IO) {
        dataStore = dataStore.copy(
            folders = dataStore.folders.map {
                if (it.id == folder.id) folder.toExternal() else it
            }
        )
        _folders.value = dataStore.folders.map { it.toEntity() }
        save()
    }

    suspend fun deleteFolder(id: Long) = withContext(Dispatchers.IO) {
        dataStore = dataStore.copy(
            folders = dataStore.folders.filter { it.id != id }
        )
        _folders.value = dataStore.folders.map { it.toEntity() }
        save()
    }

    suspend fun getFolderById(id: Long): FolderEntity? = withContext(Dispatchers.IO) {
        dataStore.folders.find { it.id == id }?.toEntity()
    }

    suspend fun insertImage(image: ImageEntity): Long = withContext(Dispatchers.IO) {
        val newId = dataStore.nextImageId
        val newImage = image.copy(id = newId)
        dataStore = dataStore.copy(
            images = dataStore.images + newImage.toExternal(),
            nextImageId = newId + 1
        )
        _images.value = dataStore.images.map { it.toEntity() }
        save()
        newId
    }

    suspend fun updateImage(image: ImageEntity) = withContext(Dispatchers.IO) {
        dataStore = dataStore.copy(
            images = dataStore.images.map {
                if (it.id == image.id) image.toExternal() else it
            }
        )
        _images.value = dataStore.images.map { it.toEntity() }
        save()
    }

    suspend fun deleteImage(id: Long) = withContext(Dispatchers.IO) {
        dataStore = dataStore.copy(
            images = dataStore.images.filter { it.id != id }
        )
        _images.value = dataStore.images.map { it.toEntity() }
        save()
    }

    suspend fun getImageById(id: Long): ImageEntity? = withContext(Dispatchers.IO) {
        dataStore.images.find { it.id == id }?.toEntity()
    }

    fun getDocumentsFlow(): Flow<List<DocumentEntity>> = documents

    fun getFoldersFlow(): Flow<List<FolderEntity>> = folders

    fun getImagesFlow(): Flow<List<ImageEntity>> = images

    fun getDataDirectoryPath(): String = getDataDirectory().absolutePath

    private fun ExternalDocument.toEntity() = DocumentEntity(
        id = id,
        title = title,
        content = content,
        folderId = folderId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        tags = tags,
        isFavorite = isFavorite,
        wordCount = wordCount
    )

    private fun DocumentEntity.toExternal() = ExternalDocument(
        id = id,
        title = title,
        content = content,
        folderId = folderId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        tags = tags,
        isFavorite = isFavorite,
        wordCount = wordCount
    )

    private fun ExternalFolder.toEntity() = FolderEntity(
        id = id,
        name = name,
        parentId = parentId,
        color = color,
        icon = icon,
        sortOrder = sortOrder,
        createdAt = createdAt
    )

    private fun FolderEntity.toExternal() = ExternalFolder(
        id = id,
        name = name,
        parentId = parentId,
        color = color,
        icon = icon,
        sortOrder = sortOrder,
        createdAt = createdAt
    )

    private fun ExternalImage.toEntity() = ImageEntity(
        id = id,
        fileName = fileName,
        originalName = originalName,
        path = path,
        mimeType = mimeType,
        size = size,
        width = width,
        height = height,
        documentId = documentId,
        createdAt = createdAt
    )

    private fun ImageEntity.toExternal() = ExternalImage(
        id = id,
        fileName = fileName,
        originalName = originalName,
        path = path,
        mimeType = mimeType,
        size = size,
        width = width,
        height = height,
        documentId = documentId,
        createdAt = createdAt
    )
}
