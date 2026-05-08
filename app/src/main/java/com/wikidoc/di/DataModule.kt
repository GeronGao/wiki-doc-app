package com.wikidoc.di

import android.content.Context
import com.wikidoc.data.external.ExternalDataStore
import com.wikidoc.data.repository.DocumentRepositoryImpl
import com.wikidoc.data.repository.FolderRepositoryImpl
import com.wikidoc.data.repository.ImageRepositoryImpl
import com.wikidoc.domain.repository.DocumentRepository
import com.wikidoc.domain.repository.FolderRepository
import com.wikidoc.domain.repository.ImageRepository
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideExternalDataStore(
        @ApplicationContext context: Context
    ): ExternalDataStore {
        return ExternalDataStore(context)
    }

    @Provides
    @Singleton
    fun provideDocumentRepository(
        externalDataStore: ExternalDataStore
    ): DocumentRepository {
        return DocumentRepositoryImpl(externalDataStore)
    }

    @Provides
    @Singleton
    fun provideFolderRepository(
        externalDataStore: ExternalDataStore
    ): FolderRepository {
        return FolderRepositoryImpl(externalDataStore)
    }

    @Provides
    @Singleton
    fun provideImageRepository(
        externalDataStore: ExternalDataStore
    ): ImageRepository {
        return ImageRepositoryImpl(externalDataStore)
    }
}
