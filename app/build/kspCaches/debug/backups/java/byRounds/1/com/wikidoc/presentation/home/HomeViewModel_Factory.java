package com.wikidoc.presentation.home;

import com.wikidoc.domain.repository.DocumentRepository;
import com.wikidoc.domain.repository.FolderRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<DocumentRepository> documentRepositoryProvider;

  private final Provider<FolderRepository> folderRepositoryProvider;

  public HomeViewModel_Factory(Provider<DocumentRepository> documentRepositoryProvider,
      Provider<FolderRepository> folderRepositoryProvider) {
    this.documentRepositoryProvider = documentRepositoryProvider;
    this.folderRepositoryProvider = folderRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(documentRepositoryProvider.get(), folderRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(
      Provider<DocumentRepository> documentRepositoryProvider,
      Provider<FolderRepository> folderRepositoryProvider) {
    return new HomeViewModel_Factory(documentRepositoryProvider, folderRepositoryProvider);
  }

  public static HomeViewModel newInstance(DocumentRepository documentRepository,
      FolderRepository folderRepository) {
    return new HomeViewModel(documentRepository, folderRepository);
  }
}
