package com.wikidoc.data.local.database.dao

import androidx.room.*
import com.wikidoc.data.local.database.entity.ImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Query("SELECT * FROM images ORDER BY createdAt DESC")
    fun getAllImages(): Flow<List<ImageEntity>>

    @Query("SELECT * FROM images WHERE documentId IS NOT NULL ORDER BY createdAt DESC")
    fun getUsedImages(): Flow<List<ImageEntity>>

    @Query("SELECT * FROM images WHERE documentId IS NULL ORDER BY createdAt DESC")
    fun getUnusedImages(): Flow<List<ImageEntity>>

    @Query("SELECT * FROM images WHERE id = :id")
    suspend fun getImageById(id: Long): ImageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: ImageEntity): Long

    @Update
    suspend fun updateImage(image: ImageEntity)

    @Delete
    suspend fun deleteImage(image: ImageEntity)

    @Query("DELETE FROM images WHERE id = :id")
    suspend fun deleteImageById(id: Long)
}
