package com.wikidoc.data.repository;

import com.wikidoc.data.local.database.dao.ImageDao;
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
public final class ImageRepositoryImpl_Factory implements Factory<ImageRepositoryImpl> {
  private final Provider<ImageDao> imageDaoProvider;

  public ImageRepositoryImpl_Factory(Provider<ImageDao> imageDaoProvider) {
    this.imageDaoProvider = imageDaoProvider;
  }

  @Override
  public ImageRepositoryImpl get() {
    return newInstance(imageDaoProvider.get());
  }

  public static ImageRepositoryImpl_Factory create(Provider<ImageDao> imageDaoProvider) {
    return new ImageRepositoryImpl_Factory(imageDaoProvider);
  }

  public static ImageRepositoryImpl newInstance(ImageDao imageDao) {
    return new ImageRepositoryImpl(imageDao);
  }
}
