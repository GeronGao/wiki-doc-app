package com.wikidoc.presentation.image;

import com.wikidoc.domain.repository.ImageRepository;
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
public final class ImageManagerViewModel_Factory implements Factory<ImageManagerViewModel> {
  private final Provider<ImageRepository> imageRepositoryProvider;

  public ImageManagerViewModel_Factory(Provider<ImageRepository> imageRepositoryProvider) {
    this.imageRepositoryProvider = imageRepositoryProvider;
  }

  @Override
  public ImageManagerViewModel get() {
    return newInstance(imageRepositoryProvider.get());
  }

  public static ImageManagerViewModel_Factory create(
      Provider<ImageRepository> imageRepositoryProvider) {
    return new ImageManagerViewModel_Factory(imageRepositoryProvider);
  }

  public static ImageManagerViewModel newInstance(ImageRepository imageRepository) {
    return new ImageManagerViewModel(imageRepository);
  }
}
