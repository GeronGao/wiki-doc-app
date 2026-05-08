package com.wikidoc.domain.repository

import com.wikidoc.domain.model.Document
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun getAllDocuments(): Flow<List<Document>>
    fun getDocumentsByFolder(folderId: Long): Flow<List<Document>>
    fun getRootDocuments(): Flow<List<Document>>
    fun getFavoriteDocuments(): Flow<List<Document>>
    fun getRecentDocuments(limit: Int): Flow<List<Document>>
    fun searchDocuments(query: String): Flow<List<Document>>
    suspend fun getDocumentById(id: Long): Document?
    suspend fun saveDocument(document: Document): Long
    suspend fun updateDocument(document: Document)
    suspend fun deleteDocument(id: Long)
}
