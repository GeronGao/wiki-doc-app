package com.wikidoc.data.repository

import com.wikidoc.data.local.database.dao.ImageDao
import com.wikidoc.data.local.database.entity.ImageEntity
import com.wikidoc.domain.model.WikiImage
import com.wikidoc.domain.repository.ImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor(
    private val imageDao: ImageDao
) : ImageRepository {

    override fun getAllImages(): Flow<List<WikiImage>> {
        return imageDao.getAllImages().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getUsedImages(): Flow<List<WikiImage>> {
        return imageDao.getUsedImages().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getUnusedImages(): Flow<List<WikiImage>> {
        return imageDao.getUnusedImages().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getImageById(id: Long): WikiImage? {
        return imageDao.getImageById(id)?.toDomain()
    }

    override suspend fun saveImage(image: WikiImage): Long {
        return imageDao.insertImage(image.toEntity())
    }

    override suspend fun updateImage(image: WikiImage) {
        imageDao.updateImage(image.toEntity())
    }

    override suspend fun deleteImage(id: Long) {
        imageDao.deleteImageById(id)
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
