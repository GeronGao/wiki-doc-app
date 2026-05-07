package com.wikidoc.presentation.folder;

import androidx.lifecycle.SavedStateHandle;
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
public final class FolderViewModel_Factory implements Factory<FolderViewModel> {
  private final Provider<DocumentRepository> documentRepositoryProvider;

  private final Provider<FolderRepository> folderRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public FolderViewModel_Factory(Provider<DocumentRepository> documentRepositoryProvider,
      Provider<FolderRepository> folderRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.documentRepositoryProvider = documentRepositoryProvider;
    this.folderRepositoryProvider = folderRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public FolderViewModel get() {
    return newInstance(documentRepositoryProvider.get(), folderRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static FolderViewModel_Factory create(
      Provider<DocumentRepository> documentRepositoryProvider,
      Provider<FolderRepository> folderRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new FolderViewModel_Factory(documentRepositoryProvider, folderRepositoryProvider, savedStateHandleProvider);
  }

  public static FolderViewModel newInstance(DocumentRepository documentRepository,
      FolderRepository folderRepository, SavedStateHandle savedStateHandle) {
    return new FolderViewModel(documentRepository, folderRepository, savedStateHandle);
  }
}
