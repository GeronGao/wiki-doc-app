package com.wikidoc.data.repository

import com.wikidoc.data.external.ExternalDataStore
import com.wikidoc.data.local.database.entity.FolderEntity
import com.wikidoc.domain.model.Folder
import com.wikidoc.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FolderRepositoryImpl @Inject constructor(
    private val externalDataStore: ExternalDataStore
) : FolderRepository {

    override fun getAllFolders(): Flow<List<Folder>> {
        return combine(externalDataStore.folders, externalDataStore.documents) { folders, documents ->
            folders.map { entity ->
                val count = documents.count { it.folderId == entity.id }
                entity.toDomain(count)
            }
        }
    }

    override fun getRootFolders(): Flow<List<Folder>> {
        return combine(externalDataStore.folders, externalDataStore.documents) { folders, documents ->
            folders.filter { it.parentId == null }.map { entity ->
                val count = documents.count { it.folderId == entity.id }
                entity.toDomain(count)
            }
        }
    }

    override fun getChildFolders(parentId: Long): Flow<List<Folder>> {
        return combine(externalDataStore.folders, externalDataStore.documents) { folders, documents ->
            folders.filter { it.parentId == parentId }.map { entity ->
                val count = documents.count { it.folderId == entity.id }
                entity.toDomain(count)
            }
        }
    }

    override suspend fun getFolderById(id: Long): Folder? {
        val entity = externalDataStore.getFolderById(id) ?: return null
        val count = externalDataStore.getDocuments().count { it.folderId == id }
        return entity.toDomain(count)
    }

    override suspend fun saveFolder(folder: Folder): Long {
        return externalDataStore.insertFolder(folder.toEntity())
    }

    override suspend fun updateFolder(folder: Folder) {
        externalDataStore.updateFolder(folder.toEntity())
    }

    override suspend fun deleteFolder(id: Long) {
        externalDataStore.deleteFolder(id)
    }

    private fun FolderEntity.toDomain(documentCount: Int = 0): Folder {
        return Folder(
            id = id,
            name = name,
            parentId = parentId,
            color = color,
            icon = icon,
            sortOrder = sortOrder,
            createdAt = createdAt,
            documentCount = documentCount
        )
    }

    private fun Folder.toEntity(): FolderEntity {
        return FolderEntity(
            id = id,
            name = name,
            parentId = parentId,
            color = color,
            icon = icon,
            sortOrder = sortOrder,
            createdAt = createdAt
        )
    }
}
