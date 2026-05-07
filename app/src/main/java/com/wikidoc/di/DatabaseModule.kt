package com.wikidoc.di

import android.content.Context
import androidx.room.Room
import com.wikidoc.data.local.database.WikiDatabase
import com.wikidoc.data.local.database.dao.DocumentDao
import com.wikidoc.data.local.database.dao.FolderDao
import com.wikidoc.data.local.database.dao.ImageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideWikiDatabase(@ApplicationContext context: Context): WikiDatabase {
        return Room.databaseBuilder(
            context,
            WikiDatabase::class.java,
            "wikidoc_database"
        ).build()
    }

    @Provides
    fun provideDocumentDao(database: WikiDatabase): DocumentDao {
        return database.documentDao()
    }

    @Provides
    fun provideFolderDao(database: WikiDatabase): FolderDao {
        return database.folderDao()
    }

    @Provides
    fun provideImageDao(database: WikiDatabase): ImageDao {
        return database.imageDao()
    }
}
