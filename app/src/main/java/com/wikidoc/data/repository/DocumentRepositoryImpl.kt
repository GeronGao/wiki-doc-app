package com.wikidoc.data.repository

import com.wikidoc.data.local.database.dao.DocumentDao
import com.wikidoc.data.local.database.entity.DocumentEntity
import com.wikidoc.domain.model.Document
import com.wikidoc.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DocumentRepositoryImpl @Inject constructor(
    private val documentDao: DocumentDao
) : DocumentRepository {

    override fun getAllDocuments(): Flow<List<Document>> {
        return documentDao.getAllDocuments().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDocumentsByFolder(folderId: Long): Flow<List<Document>> {
        return documentDao.getDocumentsByFolder(folderId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFavoriteDocuments(): Flow<List<Document>> {
        return documentDao.getFavoriteDocuments().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRecentDocuments(limit: Int): Flow<List<Document>> {
        return documentDao.getRecentDocuments(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchDocuments(query: String): Flow<List<Document>> {
        return documentDao.searchDocuments(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getDocumentById(id: Long): Document? {
        return documentDao.getDocumentById(id)?.toDomain()
    }

    override suspend fun saveDocument(document: Document): Long {
        return documentDao.insertDocument(document.toEntity())
    }

    override suspend fun updateDocument(document: Document) {
        documentDao.updateDocument(document.toEntity())
    }

    override suspend fun deleteDocument(id: Long) {
        documentDao.deleteDocumentById(id)
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
