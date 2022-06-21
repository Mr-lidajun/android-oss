package com.kickstarter;

import com.facebook.FacebookSdk;

import dagger.hilt.EntryPoints;
import dagger.hilt.components.SingletonComponent;

public class TestKSApplication extends KSApplication {

  @Override
  public SingletonComponent getComponent() {
    return EntryPoints.get(this, SingletonComponent.class);
  }

  @Override
  public void onCreate() {
    // - LoginToutViewModelTest needs the FacebookSDK initialized
    FacebookSdk.sdkInitialize(this);
    super.onCreate();
  }

  @Override
  public boolean isInUnitTests() {
    return true;
  }
}

