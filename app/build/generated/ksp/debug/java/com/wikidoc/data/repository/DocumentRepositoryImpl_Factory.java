package com.wikidoc.data.repository;

import com.wikidoc.data.local.database.dao.DocumentDao;
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
public final class DocumentRepositoryImpl_Factory implements Factory<DocumentRepositoryImpl> {
  private final Provider<DocumentDao> documentDaoProvider;

  public DocumentRepositoryImpl_Factory(Provider<DocumentDao> documentDaoProvider) {
    this.documentDaoProvider = documentDaoProvider;
  }

  @Override
  public DocumentRepositoryImpl get() {
    return newInstance(documentDaoProvider.get());
  }

  public static DocumentRepositoryImpl_Factory create(Provider<DocumentDao> documentDaoProvider) {
    return new DocumentRepositoryImpl_Factory(documentDaoProvider);
  }

  public static DocumentRepositoryImpl newInstance(DocumentDao documentDao) {
    return new DocumentRepositoryImpl(documentDao);
  }
}
