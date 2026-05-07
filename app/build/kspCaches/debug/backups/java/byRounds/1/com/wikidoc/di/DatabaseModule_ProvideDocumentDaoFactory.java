package com.wikidoc.di;

import com.wikidoc.data.local.database.WikiDatabase;
import com.wikidoc.data.local.database.dao.DocumentDao;
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
public final class DatabaseModule_ProvideDocumentDaoFactory implements Factory<DocumentDao> {
  private final Provider<WikiDatabase> databaseProvider;

  public DatabaseModule_ProvideDocumentDaoFactory(Provider<WikiDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public DocumentDao get() {
    return provideDocumentDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideDocumentDaoFactory create(
      Provider<WikiDatabase> databaseProvider) {
    return new DatabaseModule_ProvideDocumentDaoFactory(databaseProvider);
  }

  public static DocumentDao provideDocumentDao(WikiDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideDocumentDao(database));
  }
}
