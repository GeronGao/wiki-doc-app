package com.wikidoc.presentation.import_export;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class SmbImportViewModel_Factory implements Factory<SmbImportViewModel> {
  @Override
  public SmbImportViewModel get() {
    return newInstance();
  }

  public static SmbImportViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SmbImportViewModel newInstance() {
    return new SmbImportViewModel();
  }

  private static final class InstanceHolder {
    private static final SmbImportViewModel_Factory INSTANCE = new SmbImportViewModel_Factory();
  }
}
