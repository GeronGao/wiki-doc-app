package com.wikidoc.data.repository

import com.wikidoc.data.external.ExternalDataStore
import com.wikidoc.data.local.database.entity.DocumentEntity
import com.wikidoc.domain.model.Document
import com.wikidoc.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DocumentRepositoryImpl @Inject constructor(
    private val externalDataStore: ExternalDataStore
) : DocumentRepository {

    override fun getAllDocuments(): Flow<List<Document>> {
        return externalDataStore.documents.map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDocumentsByFolder(folderId: Long): Flow<List<Document>> {
        return externalDataStore.documents.map { entities ->
            entities.filter { it.folderId == folderId }.map { it.toDomain() }
        }
    }

    override fun getFavoriteDocuments(): Flow<List<Document>> {
        return externalDataStore.documents.map { entities ->
            entities.filter { it.isFavorite }.map { it.toDomain() }
        }
    }

    override fun getRecentDocuments(limit: Int): Flow<List<Document>> {
        return externalDataStore.documents.map { entities ->
            entities.sortedByDescending { it.updatedAt }.take(limit).map { it.toDomain() }
        }
    }

    override fun searchDocuments(query: String): Flow<List<Document>> {
        return externalDataStore.documents.map { entities ->
            entities.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true) ||
                        it.tags.contains(query, ignoreCase = true)
            }.map { it.toDomain() }
        }
    }

    override suspend fun getDocumentById(id: Long): Document? {
        return externalDataStore.getDocumentById(id)?.toDomain()
    }

    override suspend fun saveDocument(document: Document): Long {
        return externalDataStore.insertDocument(document.toEntity())
    }

    override suspend fun updateDocument(document: Document) {
        externalDataStore.updateDocument(document.toEntity())
    }

    override suspend fun deleteDocument(id: Long) {
        externalDataStore.deleteDocument(id)
    }

    private fun DocumentEntity.toDomain(): Document {
        return Document(
            id = id,
            title = title,
            content = content,
            folderId = folderId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            tags = if (tags.isBlank()) emptyList() else tags.split(","),
            isFavorite = isFavorite,
            wordCount = wordCount
        )
    }

    private fun Document.toEntity(): DocumentEntity {
        return DocumentEntity(
            id = id,
            title = title,
            content = content,
            folderId = folderId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            tags = tags.joinToString(","),
            isFavorite = isFavorite,
            wordCount = wordCount
        )
    }
}
