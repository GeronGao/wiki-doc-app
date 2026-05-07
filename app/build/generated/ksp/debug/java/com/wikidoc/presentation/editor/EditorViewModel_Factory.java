package com.wikidoc.presentation.editor;

import androidx.lifecycle.SavedStateHandle;
import com.wikidoc.domain.repository.DocumentRepository;
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
public final class EditorViewModel_Factory implements Factory<EditorViewModel> {
  private final Provider<DocumentRepository> documentRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public EditorViewModel_Factory(Provider<DocumentRepository> documentRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.documentRepositoryProvider = documentRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public EditorViewModel get() {
    return newInstance(documentRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static EditorViewModel_Factory create(
      Provider<DocumentRepository> documentRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new EditorViewModel_Factory(documentRepositoryProvider, savedStateHandleProvider);
  }

  public static EditorViewModel newInstance(DocumentRepository documentRepository,
      SavedStateHandle savedStateHandle) {
    return new EditorViewModel(documentRepository, savedStateHandle);
  }
}
