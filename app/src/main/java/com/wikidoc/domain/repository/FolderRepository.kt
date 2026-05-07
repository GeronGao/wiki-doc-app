package com.wikidoc.domain.repository

import com.wikidoc.domain.model.Folder
import kotlinx.coroutines.flow.Flow

interface FolderRepository {
    fun getAllFolders(): Flow<List<Folder>>
    fun getRootFolders(): Flow<List<Folder>>
    fun getChildFolders(parentId: Long): Flow<List<Folder>>
    suspend fun getFolderById(id: Long): Folder?
    suspend fun saveFolder(folder: Folder): Long
    suspend fun updateFolder(folder: Folder)
    suspend fun deleteFolder(id: Long)
}
