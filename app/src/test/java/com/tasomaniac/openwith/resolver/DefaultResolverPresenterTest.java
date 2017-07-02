package com.tasomaniac.openwith.resolver;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.Nullable;

import com.tasomaniac.openwith.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

public class DefaultResolverPresenterTest {
    private static final IntentResolver.Data EMPTY_DATA = dataWith(Collections.emptyList(), null);
    private static final List<DisplayResolveInfo> NON_EMPTY_LIST = Collections.singletonList(mock(DisplayResolveInfo.class));

    @Rule public MockitoRule rule = MockitoJUnit.rule();

    @Mock private IntentResolver intentResolver;
    @Mock private ResolverView view;
    @Mock private Resources resources;
    @Mock private ChooserHistory chooserHistory;
    @Mock private ContentResolver contentResolver;
    @Mock private Intent sourceIntent;

    private ResolverPresenter presenter;
    private ViewState viewState = new ViewState();

    @Before
    public void setUp() {
        presenter = new DefaultResolverPresenter(resources, chooserHistory, contentResolver, intentResolver, viewState);
        given(intentResolver.getSourceIntent()).willReturn(sourceIntent);

        presenter.bind(view);
    }

    @Test
    public void unbindShouldNullifyListeners() {
        presenter.unbind(view);

        then(view).should().setListener(null);
        then(intentResolver).should().setListener(null);
    }

    @Test
    public void shouldStartResolvingByDefault() {
        then(intentResolver).should().resolve();
    }

    @Test
    public void givenNonEmptyDataShouldNotifyListenerOnBind() {
        IntentResolver.Data data = mock(IntentResolver.Data.class);
        given(intentResolver.getData()).willReturn(data);

        presenter.bind(view);

        then(view).should().displayData(data, R.layout.resolver_list);
    }

    @Test
    public void givenEmptyResolveListAndNoFilteredItemShouldDisplayWarningAndDismiss() {
        IntentResolver.Listener listener = captureIntentResolverListener();
        reset(view);

        listener.onIntentResolved(EMPTY_DATA);

        then(view).should().toast(R.string.empty_resolver_activity);
        then(view).should().dismiss();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void givenSingleResolveListWithNoFilteredItemShouldStartFirstItem() {
        IntentResolver.Listener listener = captureIntentResolverListener();
        reset(view);
        Intent intent = mock(Intent.class);
        String label = "label";
        DisplayResolveInfo item = givenDisplayResolveInfoWithIntentAndLabel(intent, label);

        listener.onIntentResolved(dataWith(item));

        then(view).should().startPreferred(intent, label);
        then(view).should().dismiss();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void givenEmptyResolveListWithFilteredItemShouldStartFirstItem() {
        IntentResolver.Listener listener = captureIntentResolverListener();
        reset(view);
        Intent intent = mock(Intent.class);
        String label = "label";
        DisplayResolveInfo filteredItem = givenDisplayResolveInfoWithIntentAndLabel(intent, label);

        listener.onIntentResolved(dataWith(Collections.emptyList(), filteredItem));

        then(view).should().startPreferred(intent, label);
        then(view).should().dismiss();
        then(view).shouldHaveNoMoreInteractions();
    }

    private DisplayResolveInfo givenDisplayResolveInfoWithIntentAndLabel(Intent intent, String label) {
        DisplayResolveInfo filteredItem = mock(DisplayResolveInfo.class);
        given(filteredItem.intentFrom(any(Intent.class))).willReturn(intent);
        given(filteredItem.displayLabel()).willReturn(label);
        return filteredItem;
    }

    @Test
    public void givenResolveListWithFilteredItemShouldDisplayTitleOfFiltered() {
        IntentResolver.Listener listener = captureIntentResolverListener();
        givenResources();

        DisplayResolveInfo filteredItem = givenDisplayResolveInfoWithIntentAndLabel(null, "filtered");
        listener.onIntentResolved(dataWith(NON_EMPTY_LIST, filteredItem));

        then(view).should().setTitle("filtered");
    }

    @Test
    public void givenMultipleItemsWithNoFilteredItemShouldDisplayFixedTitle() {
        IntentResolver.Listener listener = captureIntentResolverListener();
        givenResources();

        List<DisplayResolveInfo> multipleItems = Arrays.asList(mock(DisplayResolveInfo.class), mock(DisplayResolveInfo.class));
        listener.onIntentResolved(dataWith(multipleItems, null));

        then(view).should().setTitle("fixed");
    }

    @Test
    public void givenMultipleItemsShouldSetupActionButtons() {
        IntentResolver.Listener listener = captureIntentResolverListener();

        List<DisplayResolveInfo> multipleItems = Arrays.asList(mock(DisplayResolveInfo.class), mock(DisplayResolveInfo.class));
        listener.onIntentResolved(dataWith(multipleItems, null));

        then(view).should().setupActionButtons();
    }

    private void givenResources() {
        given(resources.getString(eq(R.string.which_view_application_named), any())).willReturn("filtered");
        given(resources.getString(R.string.which_view_application)).willReturn("fixed");
    }

    @Test
    public void shouldReloadWhenPackagedChanged() {
        ResolverView.Listener listener = captureViewListener();
        reset(intentResolver);

        listener.onPackagesChanged();

        then(intentResolver).should().resolve();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfActionButtonsClickedBeforeIntentsResolved() {
        ResolverView.Listener listener = captureViewListener();

        listener.onActionButtonClick(false);
    }

    @Test
    public void givenFilteredItemActionButtonShouldStartFiltered() {
        ResolverView.Listener listener = captureViewListener();
        IntentResolver.Listener resolverListener = captureIntentResolverListener();

        Intent intent = mock(Intent.class);
        DisplayResolveInfo filteredItem = givenDisplayResolveInfoWithIntentAndLabel(intent, "filtered");
        resolverListener.onIntentResolved(dataWith(NON_EMPTY_LIST, filteredItem));

        listener.onActionButtonClick(false);

        then(view).should().startSelected(intent);
    }

    @Test
    public void givenNoFilteredItemActionButtonShouldStartLastClickedItem() {
        ResolverView.Listener listener = captureViewListener();
        IntentResolver.Listener resolverListener = captureIntentResolverListener();

        Intent intent = mock(Intent.class);
        DisplayResolveInfo item = givenDisplayResolveInfoWithIntentAndLabel(intent, "filtered");
        resolverListener.onIntentResolved(dataWith(item));

        listener.onItemClick(item);
        listener.onActionButtonClick(false);

        then(view).should().startSelected(intent);
    }

    @Test
    public void givenNoFilteredItemActionButtonShouldBeEnabledWhenItemClicked() {
        ResolverView.Listener listener = captureViewListener();
        IntentResolver.Listener resolverListener = captureIntentResolverListener();

        resolverListener.onIntentResolved(dataWith(NON_EMPTY_LIST, null));

        DisplayResolveInfo item = mock(DisplayResolveInfo.class);
        listener.onItemClick(item);

        then(view).should().enableActionButtons();
    }

    @Test
    public void givenNoFilteredShouldStoreLastSelectedWhenItemClicked() {
        ResolverView.Listener listener = captureViewListener();
        IntentResolver.Listener resolverListener = captureIntentResolverListener();

        resolverListener.onIntentResolved(dataWith(NON_EMPTY_LIST, null));

        DisplayResolveInfo item = mock(DisplayResolveInfo.class);
        listener.onItemClick(item);

        assertEquals(item, viewState.lastSelected);
    }

    @Test
    public void givenNoFilteredShouldStartItemWhenItemClickedTwice() {
        ResolverView.Listener listener = captureViewListener();
        IntentResolver.Listener resolverListener = captureIntentResolverListener();
        Intent intent = mock(Intent.class);
        DisplayResolveInfo item = givenDisplayResolveInfoWithIntentAndLabel(intent, "");

        resolverListener.onIntentResolved(dataWith(NON_EMPTY_LIST, null));

        listener.onItemClick(item);
        listener.onItemClick(item);

        then(view).should().startSelected(intent);
    }

    @Test
    public void givenFilteredShouldStartItemWhenItemClickedOnce() {
        ResolverView.Listener listener = captureViewListener();
        IntentResolver.Listener resolverListener = captureIntentResolverListener();
        Intent intent = mock(Intent.class);
        DisplayResolveInfo item = givenDisplayResolveInfoWithIntentAndLabel(intent, "");

        resolverListener.onIntentResolved(dataWith(NON_EMPTY_LIST, item));

        listener.onItemClick(item);

        then(view).should().startSelected(intent);
    }

    private IntentResolver.Listener captureIntentResolverListener() {
        ArgumentCaptor<IntentResolver.Listener> argumentCaptor = ArgumentCaptor.forClass(IntentResolver.Listener.class);
        then(intentResolver).should(atLeastOnce()).setListener(argumentCaptor.capture());
        return argumentCaptor.getValue();
    }

    private ResolverView.Listener captureViewListener() {
        ArgumentCaptor<ResolverView.Listener> argumentCaptor = ArgumentCaptor.forClass(ResolverView.Listener.class);
        then(view).should().setListener(argumentCaptor.capture());
        return argumentCaptor.getValue();
    }

    private static IntentResolver.Data dataWith(DisplayResolveInfo item) {
        return dataWith(item, null);
    }

    private static IntentResolver.Data dataWith(DisplayResolveInfo item, @Nullable DisplayResolveInfo filteredItem) {
        return dataWith(Collections.singletonList(item), filteredItem);
    }

    private static IntentResolver.Data dataWith(List<DisplayResolveInfo> resolved, @Nullable DisplayResolveInfo filteredItem) {
        return new IntentResolver.Data(resolved, filteredItem, false);
    }
}
