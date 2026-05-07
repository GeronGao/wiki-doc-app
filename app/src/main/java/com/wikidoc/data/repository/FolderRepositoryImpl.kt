package com.wikidoc.data.repository

import com.wikidoc.data.local.database.dao.FolderDao
import com.wikidoc.data.local.database.entity.FolderEntity
import com.wikidoc.domain.model.Folder
import com.wikidoc.domain.repository.FolderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FolderRepositoryImpl @Inject constructor(
    private val folderDao: FolderDao
) : FolderRepository {

    override fun getAllFolders(): Flow<List<Folder>> {
        return folderDao.getAllFolders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRootFolders(): Flow<List<Folder>> {
        return folderDao.getRootFolders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getChildFolders(parentId: Long): Flow<List<Folder>> {
        return folderDao.getChildFolders(parentId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getFolderById(id: Long): Folder? {
        return folderDao.getFolderById(id)?.toDomain()
    }

    override suspend fun saveFolder(folder: Folder): Long {
        return folderDao.insertFolder(folder.toEntity())
    }

    override suspend fun updateFolder(folder: Folder) {
        folderDao.updateFolder(folder.toEntity())
    }

    override suspend fun deleteFolder(id: Long) {
        folderDao.deleteFolderById(id)
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
