package com.wikidoc.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wikidoc.data.local.database.dao.DocumentDao
import com.wikidoc.data.local.database.dao.FolderDao
import com.wikidoc.data.local.database.dao.ImageDao
import com.wikidoc.data.local.database.entity.DocumentEntity
import com.wikidoc.data.local.database.entity.FolderEntity
import com.wikidoc.data.local.database.entity.ImageEntity

@Database(
    entities = [DocumentEntity::class, FolderEntity::class, ImageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WikiDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun folderDao(): FolderDao
    abstract fun imageDao(): ImageDao
}
