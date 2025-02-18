package com.kickstarter.viewmodels;

import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.kickstarter.KSRobolectricTestCase;
import com.kickstarter.R;
import com.kickstarter.libs.Environment;
import com.kickstarter.libs.MockCurrentUser;
import com.kickstarter.libs.rx.transformers.Transformers;
import com.kickstarter.libs.utils.EventName;
import com.kickstarter.libs.utils.extensions.DiscoveryParamsExtKt;
import com.kickstarter.mock.factories.ApiExceptionFactory;
import com.kickstarter.mock.factories.CategoryFactory;
import com.kickstarter.mock.factories.InternalBuildEnvelopeFactory;
import com.kickstarter.mock.factories.UserFactory;
import com.kickstarter.mock.services.MockApiClient;
import com.kickstarter.models.Category;
import com.kickstarter.models.User;
import com.kickstarter.services.ApiException;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.services.apiresponses.EmailVerificationEnvelope;
import com.kickstarter.services.apiresponses.ErrorEnvelope;
import com.kickstarter.services.apiresponses.InternalBuildEnvelope;
import com.kickstarter.ui.adapters.DiscoveryPagerAdapter;
import com.kickstarter.ui.adapters.data.NavigationDrawerData;
import com.kickstarter.ui.viewholders.discoverydrawer.ChildFilterViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedInViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.LoggedOutViewHolder;
import com.kickstarter.ui.viewholders.discoverydrawer.TopFilterViewHolder;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import rx.Observable;
import rx.observers.TestSubscriber;

public class DiscoveryViewModelTest extends KSRobolectricTestCase {
  private DiscoveryViewModel.ViewModel vm;
  private final TestSubscriber<List<Integer>> clearPages = new TestSubscriber<>();
  private final TestSubscriber<Boolean> drawerIsOpen = new TestSubscriber<>();
  private final TestSubscriber<Integer> drawerMenuIcon = new TestSubscriber<>();
  private final TestSubscriber<Boolean> expandSortTabLayout = new TestSubscriber<>();
  private final TestSubscriber<Void> navigationDrawerDataEmitted = new TestSubscriber<>();
  private final TestSubscriber<Integer> position = new TestSubscriber<>();
  private final TestSubscriber<List<Category>> rootCategories = new TestSubscriber<>();
  private final TestSubscriber<Boolean> rotatedExpandSortTabLayout = new TestSubscriber<>();
  private final TestSubscriber<Integer> rotatedUpdatePage = new TestSubscriber<>();
  private final TestSubscriber<DiscoveryParams> rotatedUpdateParams= new TestSubscriber<>();
  private final TestSubscriber<DiscoveryParams> rotatedUpdateToolbarWithParams = new TestSubscriber<>();
  private final TestSubscriber<Void> showActivityFeed = new TestSubscriber<>();
  private final TestSubscriber<InternalBuildEnvelope> showBuildCheckAlert = new TestSubscriber<>();
  private final TestSubscriber<Void> showCreatorDashboard = new TestSubscriber<>();
  private final TestSubscriber<Void> showHelp = new TestSubscriber<>();
  private final TestSubscriber<Void> showInternalTools = new TestSubscriber<>();
  private final TestSubscriber<Void> showLoginTout = new TestSubscriber<>();
  private final TestSubscriber<Void> showMessages = new TestSubscriber<>();
  private final TestSubscriber<Void> showProfile = new TestSubscriber<>();
  private final TestSubscriber<Void> showSettings = new TestSubscriber<>();
  private final TestSubscriber<Integer> updatePage = new TestSubscriber<>();
  private final TestSubscriber<DiscoveryParams> updateParams= new TestSubscriber<>();
  private final TestSubscriber<DiscoveryParams> updateToolbarWithParams = new TestSubscriber<>();
  private final TestSubscriber<String> showSuccessMessage = new TestSubscriber<>();
  private final TestSubscriber<String> showErrorMessage = new TestSubscriber<>();

  @Test
  public void testBuildCheck() {
    this.vm = new DiscoveryViewModel.ViewModel(environment());
    final InternalBuildEnvelope buildEnvelope = InternalBuildEnvelopeFactory.newerBuildAvailable();

    this.vm.getOutputs().showBuildCheckAlert().subscribe(this.showBuildCheckAlert);

    // Build check should not be shown.
    this.showBuildCheckAlert.assertNoValues();

    // Build check should be shown when newer build is available.
    this.vm.getInputs().newerBuildIsAvailable(buildEnvelope);
    this.showBuildCheckAlert.assertValue(buildEnvelope);
  }

  @Test
  public void testDrawerData() {
    final MockCurrentUser currentUser = new MockCurrentUser();
    final Environment env = environment().toBuilder().currentUser(currentUser).build();
    this.vm = new DiscoveryViewModel.ViewModel(env);

    this.vm.getOutputs().navigationDrawerData().compose(Transformers.ignoreValues()).subscribe(this.navigationDrawerDataEmitted);
    this.vm.getOutputs().drawerIsOpen().subscribe(this.drawerIsOpen);

    // Initialize activity.
    final Intent intent = new Intent(Intent.ACTION_MAIN);
    this.vm.intent(intent);

    // Initial MAGIC page selected.
    this.vm.getInputs().discoveryPagerAdapterSetPrimaryPage(Mockito.mock(DiscoveryPagerAdapter.class), 0);

    // Drawer data should emit. Drawer should be closed.
    this.navigationDrawerDataEmitted.assertValueCount(1);
    this.drawerIsOpen.assertNoValues();
    this.segmentTrack.assertNoValues();

    // Open drawer and click the top PWL filter.
    this.vm.getInputs().openDrawer(true);
    this.vm.getInputs().topFilterViewHolderRowClick(Mockito.mock(TopFilterViewHolder.class), NavigationDrawerData.Section.Row
      .builder()
      .params(DiscoveryParams.builder().staffPicks(true).build())
      .build()
    );

    // Drawer data should emit. Drawer should open, then close upon selection.
    this.navigationDrawerDataEmitted.assertValueCount(2);
    this.drawerIsOpen.assertValues(true, false);
    this.segmentTrack.assertValue(EventName.CTA_CLICKED.getEventName());

    // Open drawer and click a child filter.
    this.vm.getInputs().openDrawer(true);
    this.vm.getInputs().childFilterViewHolderRowClick(Mockito.mock(ChildFilterViewHolder.class), NavigationDrawerData.Section.Row
      .builder()
      .params(DiscoveryParams
        .builder()
        .category(CategoryFactory.artCategory())
        .build()
      )
      .build()
    );

    // Drawer data should emit. Drawer should open, then close upon selection.
    this.navigationDrawerDataEmitted.assertValueCount(3);
    this.drawerIsOpen.assertValues(true, false, true, false);
    this.segmentTrack.assertValues(EventName.CTA_CLICKED.getEventName(), EventName.CTA_CLICKED.getEventName());
  }

  @Test
  public void testUpdateInterfaceElementsWithParams() {
    this.vm = new DiscoveryViewModel.ViewModel(environment());

    this.vm.getOutputs().updateToolbarWithParams().subscribe(this.updateToolbarWithParams);
    this.vm.getOutputs().expandSortTabLayout().subscribe(this.expandSortTabLayout);

    // Initialize activity.
    final Intent intent = new Intent(Intent.ACTION_MAIN);
    this.vm.intent(intent);

    // Initial MAGIC page selected.
    this.vm.getInputs().discoveryPagerAdapterSetPrimaryPage(Mockito.mock(DiscoveryPagerAdapter.class), 0);

    // Sort tab should be expanded.
    this.expandSortTabLayout.assertValues(true, true);

    // Toolbar params should be loaded with initial params.
    this.updateToolbarWithParams.assertValues(DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build());

    // Select POPULAR sort.
    this.vm.getInputs().sortClicked(1);
    this.vm.getInputs().discoveryPagerAdapterSetPrimaryPage(Mockito.mock(DiscoveryPagerAdapter.class), 1);

    this.segmentTrack.assertValue(EventName.CTA_CLICKED.getEventName());

    // Sort tab should be expanded.
    this.expandSortTabLayout.assertValues(true, true, true);

    // Unchanged toolbar params should not emit.
    this.updateToolbarWithParams.assertValues(DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build());

    // Select ALL PROJECTS filter from drawer.
    this.vm.getInputs().topFilterViewHolderRowClick(Mockito.mock(TopFilterViewHolder.class),
      NavigationDrawerData.Section.Row.builder().params(DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build()).build()
    );

    // Sort tab should be expanded.
    this.expandSortTabLayout.assertValues(true, true, true, true, true);
    this.segmentTrack.assertValues(EventName.CTA_CLICKED.getEventName(), EventName.CTA_CLICKED.getEventName());

    // Select ART category from drawer.
    this.vm.getInputs().childFilterViewHolderRowClick(Mockito.mock(ChildFilterViewHolder.class),
      NavigationDrawerData.Section.Row.builder()
        .params(DiscoveryParams.builder().category(CategoryFactory.artCategory()).sort(DiscoveryParams.Sort.POPULAR).build())
        .build()
    );

    // Sort tab should be expanded.
    this.expandSortTabLayout.assertValues(true, true, true, true, true, true, true);
    this.segmentTrack.assertValues(EventName.CTA_CLICKED.getEventName(), EventName.CTA_CLICKED.getEventName(), EventName.CTA_CLICKED.getEventName());

    // Simulate rotating the device and hitting initial getInputs() again.
    this.vm.getOutputs().updateToolbarWithParams().subscribe(this.rotatedUpdateToolbarWithParams);
    this.vm.getOutputs().expandSortTabLayout().subscribe(this.rotatedExpandSortTabLayout);

    // Simulate recreating and setting POPULAR fragment, the previous position before rotation.
    this.vm.getInputs().discoveryPagerAdapterSetPrimaryPage(Mockito.mock(DiscoveryPagerAdapter.class), 1);

    // Sort tab and toolbar params should emit again with same params.
    this.rotatedExpandSortTabLayout.assertValues(true);
    this.rotatedUpdateToolbarWithParams.assertValues(
      DiscoveryParams.builder().category(CategoryFactory.artCategory()).sort(DiscoveryParams.Sort.POPULAR).build()
    );
  }

  @Test
  public void testClickingInterfaceElements() {
    this.vm = new DiscoveryViewModel.ViewModel(environment());

    this.vm.getOutputs().showActivityFeed().subscribe(this.showActivityFeed);
    this.vm.getOutputs().showCreatorDashboard().subscribe(this.showCreatorDashboard);
    this.vm.getOutputs().showHelp().subscribe(this.showHelp);
    this.vm.getOutputs().showInternalTools().subscribe(this.showInternalTools);
    this.vm.getOutputs().showLoginTout().subscribe(this.showLoginTout);
    this.vm.getOutputs().showMessages().subscribe(this.showMessages);
    this.vm.getOutputs().showProfile().subscribe(this.showProfile);
    this.vm.getOutputs().showSettings().subscribe(this.showSettings);

    this.showActivityFeed.assertNoValues();
    this.showCreatorDashboard.assertNoValues();
    this.showHelp.assertNoValues();
    this.showInternalTools.assertNoValues();
    this.showLoginTout.assertNoValues();
    this.showMessages.assertNoValues();
    this.showProfile.assertNoValues();
    this.showSettings.assertNoValues();

    this.vm.getInputs().loggedInViewHolderActivityClick(Mockito.mock(LoggedInViewHolder.class));
    this.vm.getInputs().loggedOutViewHolderActivityClick(Mockito.mock(LoggedOutViewHolder.class));
    this.vm.getInputs().loggedInViewHolderDashboardClick(Mockito.mock(LoggedInViewHolder.class));
    this.vm.getInputs().loggedOutViewHolderHelpClick(Mockito.mock(LoggedOutViewHolder.class));
    this.vm.getInputs().loggedInViewHolderInternalToolsClick(Mockito.mock(LoggedInViewHolder.class));
    this.vm.getInputs().loggedOutViewHolderLoginToutClick(Mockito.mock(LoggedOutViewHolder.class));
    this.vm.getInputs().loggedInViewHolderMessagesClick(Mockito.mock(LoggedInViewHolder.class));
    this.vm.getInputs().loggedInViewHolderProfileClick(Mockito.mock(LoggedInViewHolder.class), UserFactory.user());
    this.vm.getInputs().loggedInViewHolderSettingsClick(Mockito.mock(LoggedInViewHolder.class), UserFactory.user());

    this.showActivityFeed.assertValueCount(2);
    this.showCreatorDashboard.assertValueCount(1);
    this.showHelp.assertValueCount(1);
    this.showInternalTools.assertValueCount(1);
    this.showLoginTout.assertValueCount(1);
    this.showMessages.assertValueCount(1);
    this.showProfile.assertValueCount(1);
    this.showSettings.assertValueCount(1);
  }

  @Test
  public void testInteractionBetweenParamsAndPageAdapter() {
    this.vm = new DiscoveryViewModel.ViewModel(environment());

    this.vm.getOutputs().updateParamsForPage().subscribe(this.updateParams);
    this.vm.getOutputs().updateParamsForPage().map(params -> DiscoveryParamsExtKt.positionFromSort(params.sort())).subscribe(this.updatePage);

    // Start initial activity.
    final Intent intent = new Intent(Intent.ACTION_MAIN);
    this.vm.intent(intent);

    // Initial MAGIC page selected.
    this.vm.getInputs().discoveryPagerAdapterSetPrimaryPage(Mockito.mock(DiscoveryPagerAdapter.class), 0);

    // Initial params should emit. Page should not be updated yet.
    this.updateParams.assertValues(
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build(), DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build()
    );
    this.updatePage.assertValues(0, 0);

    // Select POPULAR sort position.
    this.vm.getInputs().discoveryPagerAdapterSetPrimaryPage(Mockito.mock(DiscoveryPagerAdapter.class), 1);

    // Params and page should update with new POPULAR sort values.
    this.updateParams.assertValues(
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build(),
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build(),
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build()
    );
    this.updatePage.assertValues(0, 0, 1);

    // Select ART category from the drawer.
    this.vm.getInputs().childFilterViewHolderRowClick(Mockito.mock(ChildFilterViewHolder.class),
      NavigationDrawerData.Section.Row.builder()
        .params(DiscoveryParams.builder().category(CategoryFactory.artCategory()).build())
        .build()
    );

    // Params should update with new category; page should remain the same.
    this.updateParams.assertValues(
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build(),
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build(),
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build(),
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).category(CategoryFactory.artCategory()).build(),
      DiscoveryParams.builder().category(CategoryFactory.artCategory()).sort(DiscoveryParams.Sort.POPULAR).build()
    );
    this.updatePage.assertValues(0, 0, 1, 1, 1);

    // Select MAGIC sort position.
    this.vm.getInputs().discoveryPagerAdapterSetPrimaryPage(Mockito.mock(DiscoveryPagerAdapter.class), 0);

    // Params and page should update with new MAGIC sort value.
    this.updateParams.assertValues(
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build(),
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build(),
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).build(),
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.POPULAR).category(CategoryFactory.artCategory()).build(),
      DiscoveryParams.builder().category(CategoryFactory.artCategory()).sort(DiscoveryParams.Sort.POPULAR).build(),
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).category(CategoryFactory.artCategory()).build()
    );
    this.updatePage.assertValues(0, 0, 1, 1, 1, 0);

    // Simulate rotating the device and hitting initial getInputs() again.
    this.vm.getOutputs().updateParamsForPage().subscribe(this.rotatedUpdateParams);
    this.vm.getOutputs().updateParamsForPage().map(params -> DiscoveryParamsExtKt.positionFromSort(params.sort())).subscribe(this.rotatedUpdatePage);

    // Should emit again with same params.
    this.rotatedUpdateParams.assertValues(
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).category(CategoryFactory.artCategory()).build()
    );
    this.rotatedUpdatePage.assertValues(0);
  }

  @Test
  public void testDefaultParams_withUserLoggedOut() {
    setUpDefaultParamsTest(null);

    this.updateParams.assertValues(
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build(),
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build()
    );
  }

  @Test
  public void testDefaultParams_withUserLoggedIn_optedIn() {
    setUpDefaultParamsTest(UserFactory.user());

    this.updateParams.assertValues(
      DiscoveryParams.builder().recommended(true).backed(-1).sort(DiscoveryParams.Sort.MAGIC).build(),
      DiscoveryParams.builder().recommended(true).backed(-1).sort(DiscoveryParams.Sort.MAGIC).build()
    );
  }

  @Test
  public void testDefaultParams_withUserLoggedIn_optedOut() {
    setUpDefaultParamsTest(UserFactory.noRecommendations());

    this.updateParams.assertValues(
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build(),
      DiscoveryParams.builder().sort(DiscoveryParams.Sort.MAGIC).build()
    );
  }

  @Test
  public void testClearingPages() {
    this.vm = new DiscoveryViewModel.ViewModel(environment());

    this.vm.getOutputs().clearPages().subscribe(this.clearPages);

    // Start initial activity.
    final Intent intent = new Intent(Intent.ACTION_MAIN);
    this.vm.intent(intent);

    this.clearPages.assertNoValues();

    this.vm.getInputs().discoveryPagerAdapterSetPrimaryPage(Mockito.mock(DiscoveryPagerAdapter.class), 1);

    this.clearPages.assertNoValues();

    this.vm.getInputs().discoveryPagerAdapterSetPrimaryPage(Mockito.mock(DiscoveryPagerAdapter.class), 3);

    this.clearPages.assertNoValues();

    // Select ART category from the drawer.
    this.vm.getInputs().childFilterViewHolderRowClick(Mockito.mock(ChildFilterViewHolder.class),
      NavigationDrawerData.Section.Row.builder()
        .params(DiscoveryParams.builder().category(CategoryFactory.artCategory()).build())
        .build()
    );

    this.clearPages.assertValues(Arrays.asList(0, 1, 2));

    this.vm.getInputs().discoveryPagerAdapterSetPrimaryPage(Mockito.mock(DiscoveryPagerAdapter.class), 1);

    // Select MUSIC category from the drawer.
    this.vm.getInputs().childFilterViewHolderRowClick(Mockito.mock(ChildFilterViewHolder.class),
      NavigationDrawerData.Section.Row.builder()
        .params(DiscoveryParams.builder().category(CategoryFactory.musicCategory()).build())
        .build()
    );

    this.clearPages.assertValues(Arrays.asList(0, 1, 2), Arrays.asList(0, 2, 3));
  }

  @Test
  public void testRootCategoriesEmitWithPosition() {
    this.vm = new DiscoveryViewModel.ViewModel(environment());

    this.vm.getOutputs().rootCategoriesAndPosition().map(cp -> cp.first).subscribe(this.rootCategories);
    this.vm.getOutputs().rootCategoriesAndPosition().map(cp -> cp.second).subscribe(this.position);

    // Start initial activity.
    this.vm.intent(new Intent(Intent.ACTION_MAIN));

    // Initial MAGIC page selected.
    this.vm.getInputs().discoveryPagerAdapterSetPrimaryPage(Mockito.mock(DiscoveryPagerAdapter.class), 0);

    // Root categories should emit for the initial MAGIC sort this.position.
    this.rootCategories.assertValueCount(1);
    this.position.assertValues(0);

    // Select POPULAR sort position.
    this.vm.getInputs().discoveryPagerAdapterSetPrimaryPage(Mockito.mock(DiscoveryPagerAdapter.class), 1);

    // Root categories should emit for the POPULAR sort position.
    this.rootCategories.assertValueCount(2);
    this.position.assertValues(0, 1);

    // Select ART category from the drawer.
    this.vm.getInputs().childFilterViewHolderRowClick(Mockito.mock(ChildFilterViewHolder.class),
      NavigationDrawerData.Section.Row.builder()
        .params(DiscoveryParams.builder().category(CategoryFactory.artCategory()).build())
        .build()
    );

    // Root categories should not emit again for the same position.
    this.rootCategories.assertValueCount(2);
    this.position.assertValues(0, 1);
  }

  @Test
  public void testDrawerMenuIcon_whenLoggedOut() {
    this.vm = new DiscoveryViewModel.ViewModel(environment());

    this.vm.getOutputs().drawerMenuIcon().subscribe(this.drawerMenuIcon);

    this.drawerMenuIcon.assertValue(R.drawable.ic_menu);
  }

  @Test
  public void testDrawerMenuIcon_afterLogInRefreshAndLogOut() {
    final MockCurrentUser currentUser = new MockCurrentUser();

    this.vm = new DiscoveryViewModel.ViewModel(environment().toBuilder().currentUser(currentUser).build());
    this.vm.getOutputs().drawerMenuIcon().subscribe(this.drawerMenuIcon);

    this.drawerMenuIcon.assertValue(R.drawable.ic_menu);

    currentUser.refresh(UserFactory.user().toBuilder().unreadMessagesCount(4).build());
    this.drawerMenuIcon.assertValues(R.drawable.ic_menu, R.drawable.ic_menu_indicator);

    currentUser.refresh(UserFactory.user().toBuilder().erroredBackingsCount(2).build());
    this.drawerMenuIcon.assertValues(R.drawable.ic_menu, R.drawable.ic_menu_indicator, R.drawable.ic_menu_error_indicator);

    currentUser.refresh(UserFactory.user().toBuilder().unreadMessagesCount(4).unseenActivityCount(3).erroredBackingsCount(2).build());
    this.drawerMenuIcon.assertValues(R.drawable.ic_menu, R.drawable.ic_menu_indicator, R.drawable.ic_menu_error_indicator);

    currentUser.logout();
    this.drawerMenuIcon.assertValues(R.drawable.ic_menu, R.drawable.ic_menu_indicator, R.drawable.ic_menu_error_indicator,
      R.drawable.ic_menu);
  }

  @Test
  public void testDrawerMenuIcon_whenUserHasNoUnreadMessagesOrUnseenActivityOrErroredBackings() {
    final MockCurrentUser currentUser = new MockCurrentUser(UserFactory.user());
    this.vm = new DiscoveryViewModel.ViewModel(environment()
      .toBuilder()
      .currentUser(currentUser)
      .build());

    this.vm.getOutputs().drawerMenuIcon().subscribe(this.drawerMenuIcon);

    this.drawerMenuIcon.assertValue(R.drawable.ic_menu);
  }

  @Test
  public void testShowSnackBar_whenIntentFromDeepLinkSuccessResponse_showSuccessMessage() {
    final String url = "https://*.kickstarter.com/profile/verify_email";
    final Intent intentWithUrl = new Intent().setData(Uri.parse(url));

    final MockApiClient mockApiClient = new MockApiClient() {
      @NonNull
      @Override
      public Observable<EmailVerificationEnvelope> verifyEmail(final @NonNull String token) {
        return Observable.just(EmailVerificationEnvelope.Companion.builder()
          .code(200)
          .message("Success")
          .build());
      }
    };

    final Environment mockedClientEnvironment = environment().toBuilder()
            .apiClient(mockApiClient)
            .build();

    this.vm = new DiscoveryViewModel.ViewModel(mockedClientEnvironment);
    this.vm.getOutputs().showSuccessMessage().subscribe(this.showSuccessMessage);
    this.vm.getOutputs().showErrorMessage().subscribe(this.showErrorMessage);

    this.vm.intent(intentWithUrl);

    this.showSuccessMessage.assertValue("Success");
    this.showErrorMessage.assertNoValues();
  }

  @Test
  public void testShowSnackBar_whenIntentFromDeepLinkSuccessResponse_showErrorMessage() {
    final String url = "https://*.kickstarter.com/profile/verify_email";
    final Intent intentWithUrl = new Intent().setData(Uri.parse(url));

    final ErrorEnvelope errorEnvelope = ErrorEnvelope.builder()
            .httpCode(403).errorMessages(Collections.singletonList("expired")).build();
    final ApiException apiException = ApiExceptionFactory.apiError(errorEnvelope);

    final MockApiClient mockApiClient = new MockApiClient() {
      @NonNull
      @Override
      public Observable<EmailVerificationEnvelope> verifyEmail(final @NonNull String token) {
        return Observable.error(apiException);
      }
    };

    final Environment mockedClientEnvironment = environment().toBuilder()
            .apiClient(mockApiClient)
            .build();

    this.vm = new DiscoveryViewModel.ViewModel(mockedClientEnvironment);
    this.vm.getOutputs().showSuccessMessage().subscribe(this.showSuccessMessage);
    this.vm.getOutputs().showErrorMessage().subscribe(this.showErrorMessage);

    this.vm.intent(intentWithUrl);

    this.showSuccessMessage.assertNoValues();
    this.showErrorMessage.assertValue("expired");
  }

  @Test
  public void testIntentWithUri_whenGivenSort_shouldEmitSort() {
    final String url = "https://www.kickstarter.com/discover/advanced?sort=end_date";
    final Intent intentWithUrl = new Intent().setData(Uri.parse(url));

    this.vm = new DiscoveryViewModel.ViewModel(environment());
    this.vm.getOutputs().updateParamsForPage().subscribe(this.updateParams);

    this.vm.intent(intentWithUrl);

    this.updateParams.assertValue(DiscoveryParams.builder().sort(DiscoveryParams.Sort.ENDING_SOON).build());
  }

  private void setUpDefaultParamsTest(final @Nullable User user) {
    final Environment.Builder environmentBuilder = environment().toBuilder();

    if (user != null) {
      final MockCurrentUser currentUser = new MockCurrentUser(user);
      environmentBuilder.currentUser(currentUser);
    }

    this.vm = new DiscoveryViewModel.ViewModel(environmentBuilder.build());
    this.vm.getOutputs().updateParamsForPage().subscribe(this.updateParams);

    // Start initial activity.
    final Intent intent = new Intent(Intent.ACTION_MAIN);
    this.vm.intent(intent);

    // Initial MAGIC page selected.
    this.vm.getInputs().discoveryPagerAdapterSetPrimaryPage(Mockito.mock(DiscoveryPagerAdapter.class), 0);
  }
}
