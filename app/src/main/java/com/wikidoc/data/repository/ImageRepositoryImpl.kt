package com.wikidoc.data.repository

import com.wikidoc.data.external.ExternalDataStore
import com.wikidoc.data.local.database.entity.ImageEntity
import com.wikidoc.domain.model.WikiImage
import com.wikidoc.domain.repository.ImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val externalDataStore: ExternalDataStore
) : ImageRepository {

    override fun getAllImages(): Flow<List<WikiImage>> {
        return externalDataStore.images.map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getUsedImages(): Flow<List<WikiImage>> {
        return externalDataStore.images.map { entities ->
            entities.filter { it.documentId != null }.map { it.toDomain() }
        }
    }

    override fun getUnusedImages(): Flow<List<WikiImage>> {
        return externalDataStore.images.map { entities ->
            entities.filter { it.documentId == null }.map { it.toDomain() }
        }
    }

    override suspend fun getImageById(id: Long): WikiImage? {
        return externalDataStore.getImageById(id)?.toDomain()
    }

    override suspend fun saveImage(image: WikiImage): Long {
        return externalDataStore.insertImage(image.toEntity())
    }

    override suspend fun updateImage(image: WikiImage) {
        externalDataStore.updateImage(image.toEntity())
    }

    override suspend fun deleteImage(id: Long) {
        externalDataStore.deleteImage(id)
    }

    private fun ImageEntity.toDomain(): WikiImage {
        return WikiImage(
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

    private fun WikiImage.toEntity(): ImageEntity {
        return ImageEntity(
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
}
