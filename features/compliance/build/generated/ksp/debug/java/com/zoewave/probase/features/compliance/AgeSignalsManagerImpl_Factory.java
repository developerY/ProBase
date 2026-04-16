package com.zoewave.probase.features.compliance;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class AgeSignalsManagerImpl_Factory implements Factory<AgeSignalsManagerImpl> {
  private final Provider<Context> contextProvider;

  private AgeSignalsManagerImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AgeSignalsManagerImpl get() {
    return newInstance(contextProvider.get());
  }

  public static AgeSignalsManagerImpl_Factory create(Provider<Context> contextProvider) {
    return new AgeSignalsManagerImpl_Factory(contextProvider);
  }

  public static AgeSignalsManagerImpl newInstance(Context context) {
    return new AgeSignalsManagerImpl(context);
  }
}
