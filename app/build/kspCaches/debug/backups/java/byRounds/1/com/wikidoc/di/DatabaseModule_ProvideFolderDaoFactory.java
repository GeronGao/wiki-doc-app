package com.wikidoc.di;

import com.wikidoc.data.local.database.WikiDatabase;
import com.wikidoc.data.local.database.dao.FolderDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideFolderDaoFactory implements Factory<FolderDao> {
  private final Provider<WikiDatabase> databaseProvider;

  public DatabaseModule_ProvideFolderDaoFactory(Provider<WikiDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public FolderDao get() {
    return provideFolderDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideFolderDaoFactory create(
      Provider<WikiDatabase> databaseProvider) {
    return new DatabaseModule_ProvideFolderDaoFactory(databaseProvider);
  }

  public static FolderDao provideFolderDao(WikiDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideFolderDao(database));
  }
}
