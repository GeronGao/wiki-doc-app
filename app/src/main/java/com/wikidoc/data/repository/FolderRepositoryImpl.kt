package com.wikidoc.data.repository

import com.wikidoc.data.external.ExternalDataStore
import com.wikidoc.data.local.database.entity.FolderEntity
import com.wikidoc.domain.model.Folder
import com.wikidoc.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FolderRepositoryImpl @Inject constructor(
    private val externalDataStore: ExternalDataStore
) : FolderRepository {

    override fun getAllFolders(): Flow<List<Folder>> {
        return externalDataStore.folders.map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRootFolders(): Flow<List<Folder>> {
        return externalDataStore.folders.map { entities ->
            entities.filter { it.parentId == null }.map { it.toDomain() }
        }
    }

    override fun getChildFolders(parentId: Long): Flow<List<Folder>> {
        return externalDataStore.folders.map { entities ->
            entities.filter { it.parentId == parentId }.map { it.toDomain() }
        }
    }

    override suspend fun getFolderById(id: Long): Folder? {
        return externalDataStore.getFolderById(id)?.toDomain()
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

    private fun FolderEntity.toDomain(): Folder {
        return Folder(
            id = id,
            name = name,
            parentId = parentId,
            color = color,
            icon = icon,
            sortOrder = sortOrder,
            createdAt = createdAt
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
