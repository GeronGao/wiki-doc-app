package com.wikidoc.domain.repository

import com.wikidoc.domain.model.WikiImage
import kotlinx.coroutines.flow.Flow

interface ImageRepository {
    fun getAllImages(): Flow<List<WikiImage>>
    fun getUsedImages(): Flow<List<WikiImage>>
    fun getUnusedImages(): Flow<List<WikiImage>>
    suspend fun getImageById(id: Long): WikiImage?
    suspend fun saveImage(image: WikiImage): Long
    suspend fun updateImage(image: WikiImage)
    suspend fun deleteImage(id: Long)
}
