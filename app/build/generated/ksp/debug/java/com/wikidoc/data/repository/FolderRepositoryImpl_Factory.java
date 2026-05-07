package com.wikidoc.data.repository;

import com.wikidoc.data.local.database.dao.FolderDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class FolderRepositoryImpl_Factory implements Factory<FolderRepositoryImpl> {
  private final Provider<FolderDao> folderDaoProvider;

  public FolderRepositoryImpl_Factory(Provider<FolderDao> folderDaoProvider) {
    this.folderDaoProvider = folderDaoProvider;
  }

  @Override
  public FolderRepositoryImpl get() {
    return newInstance(folderDaoProvider.get());
  }

  public static FolderRepositoryImpl_Factory create(Provider<FolderDao> folderDaoProvider) {
    return new FolderRepositoryImpl_Factory(folderDaoProvider);
  }

  public static FolderRepositoryImpl newInstance(FolderDao folderDao) {
    return new FolderRepositoryImpl(folderDao);
  }
}
