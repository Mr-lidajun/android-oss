package com.kickstarter.libs.utils;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.kickstarter.KSApplication;
import com.kickstarter.libs.Build;
import com.kickstarter.libs.CurrentConfigType;
import com.kickstarter.libs.CurrentUserType;
import com.kickstarter.libs.Logout;
import com.kickstarter.libs.preferences.StringPreferenceType;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.extensions.ConfigExtension;
import com.kickstarter.services.ApiClientType;
import com.kickstarter.services.apiresponses.ErrorEnvelope;

import javax.inject.Inject;

public final class ApplicationLifecycleUtil implements Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {
  protected @Inject ApiClientType client;
  protected @Inject CurrentConfigType config;
  protected @Inject CurrentUserType currentUser;
  protected @Inject Logout logout;
  protected @Inject Build build;
  protected @Inject StringPreferenceType featuresFlagPreference;

  private final KSApplication application;
  private boolean isInBackground = true;
  private boolean isLoggedIn;

  public ApplicationLifecycleUtil(final @NonNull KSApplication application) {
    this.application = application;
    application.component().inject(this);

    this.currentUser.isLoggedIn().subscribe(userLoggedIn -> {
      this.isLoggedIn = userLoggedIn;
    });
  }

  @Override
  public void onActivityCreated(final @NonNull Activity activity, final @Nullable Bundle bundle) {
  }

  @Override
  public void onActivityStarted(final @NonNull Activity activity) {
  }

  @Override
  public void onActivityResumed(final @NonNull Activity activity) {
    if(this.isInBackground) {
      // Facebook: logs 'install' and 'app activate' App Events.
      AppEventsLogger.activateApp(activity.getApplication());

      refreshConfigFile();
      refreshUser();

      this.isInBackground = false;
    }
  }

  /**
   * Refresh the config file.
   */
  private void refreshConfigFile() {
    this.client.config()
      .materialize()
      .share()
      .subscribe(notification -> {
        if (notification.hasValue()) {
          //sync save features flags in the config object
          if (this.build.isDebug() || Build.isInternal()) {
            ConfigExtension.syncUserFeatureFlagsFromPref(notification.getValue(), this.featuresFlagPreference);
          }
          this.config.config(notification.getValue());
        }
        if (notification.hasThrowable()) {
          this.handleConfigApiError(ErrorEnvelope.fromThrowable(notification.getThrowable()));
        }
      });
  }

  /**
   * Handles a config API error by logging the user out in the case of a 401. We will interpret
   * 401's on the config request as meaning the user's current access token is no longer valid,
   * as that endpoint should never 401 othewise.
   */
  private void handleConfigApiError(final @NonNull ErrorEnvelope error) {
    if (error.httpCode() == 401) {
      forceLogout("config_api_error");
    }
  }

  /**
   * Forces the current user session to be logged out.
   */
  private void forceLogout(final @NonNull String context) {
    this.logout.execute();
    ApplicationUtils.startNewDiscoveryActivity(this.application);
    final Bundle params = new Bundle();
    params.putString("location", context);

    FirebaseAnalytics.getInstance(this.application).logEvent("force_logout", params);
  }

  /**
   * Refreshes the user object if there is not a user logged in with a non-null access token.
   */
  private void refreshUser() {
    final String accessToken = this.currentUser.getAccessToken();

    // Check if the access token is null and the user is still logged in.
    if (this.isLoggedIn && ObjectUtils.isNull(accessToken)) {
      forceLogout("access_token_null");
    } else {
      if (ObjectUtils.isNotNull(accessToken)) {
        this.client.fetchCurrentUser()
          .compose(Transformers.neverError())
          .subscribe(u -> this.currentUser.refresh(u));
      }
    }
  }

  @Override
  public void onActivityPaused(final @NonNull Activity activity) {
  }

  @Override
  public void onActivityStopped(final @NonNull Activity activity) {
  }

  @Override
  public void onActivitySaveInstanceState(final @NonNull Activity activity, final @Nullable Bundle bundle) {
  }

  @Override
  public void onActivityDestroyed(final @NonNull Activity activity) {
  }

  @Override
  public void onConfigurationChanged(final @NonNull Configuration configuration) {
  }

  @Override
  public void onLowMemory() {
  }

  /**
   * Memory availability callback. TRIM_MEMORY_UI_HIDDEN means the app's UI is no longer visible.
   * This is triggered when the user navigates out of the app and primarily used to free resources used by the UI.
   * http://developer.android.com/training/articles/memory.html
   */
  @Override
  public void onTrimMemory(final int i) {
    if(i == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
      this.isInBackground = true;
    }
  }
}
