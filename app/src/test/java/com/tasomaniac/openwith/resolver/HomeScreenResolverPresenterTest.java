package com.tasomaniac.openwith.resolver;

import android.content.Intent;
import android.content.res.Resources;

import com.tasomaniac.openwith.R;

import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

public class HomeScreenResolverPresenterTest {

    private static final String TITLE = "title";
    private static final IntentResolver.Data EMPTY_DATA = new IntentResolver.Data(Collections.emptyList(), null, false);

    @Rule public MockitoRule rule = MockitoJUnit.rule();

    @Mock private IntentResolver intentResolver;
    @Mock private ResolverView view;
    @Mock private Resources resources;
    @Mock private Intent sourceIntent;
    @Mock private ResolverNavigation navigation;

    private HomeScreenResolverPresenter presenter;

    @Before
    public void setUp() throws Exception {
        presenter = new HomeScreenResolverPresenter(resources, intentResolver);
        given(intentResolver.getSourceIntent()).willReturn(sourceIntent);

        presenter.bind(view, navigation);
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

        presenter.bind(view, navigation);

        then(view).should().displayData(data);
    }

    @Test
    public void shouldHaveNoInteractionWithFilteredItem() {
        IntentResolver.Listener listener = captureIntentResolverListener();

        DisplayResolveInfo filteredItem = mock(DisplayResolveInfo.class);
        listener.onIntentResolved(new IntentResolver.Data(Collections.emptyList(), filteredItem, false));

        then(filteredItem).shouldHaveZeroInteractions();
    }

    @Test
    public void givenEmptyResolveListShouldDisplayWarningAndDismiss() {
        IntentResolver.Listener listener = captureIntentResolverListener();
        reset(view);

        listener.onIntentResolved(EMPTY_DATA);

        then(view).should().toast(R.string.empty_resolver_activity);
        then(navigation).should().dismiss();
        then(view).shouldHaveNoMoreInteractions();
    }

    @Test
    public void givenResolveListShouldSetupUI() {
        IntentResolver.Listener listener = captureIntentResolverListener();

        DisplayResolveInfo item = mock(DisplayResolveInfo.class);
        IntentResolver.Data data = dataWithItem(item);
        listener.onIntentResolved(data);

        then(view).should().displayData(data);
    }

    @Test
    public void givenResolveListShouldDisplayTitle() {
        IntentResolver.Listener listener = captureIntentResolverListener();
        given(resources.getString(R.string.add_to_homescreen)).willReturn(TITLE);

        DisplayResolveInfo item = mock(DisplayResolveInfo.class);
        listener.onIntentResolved(dataWithItem(item));

        then(view).should().setTitle(TITLE);
    }

    @Test
    public void shouldReloadWhenPackagedChanged() {
        ResolverView.Listener listener = captureViewListener();
        reset(intentResolver);

        listener.onPackagesChanged();

        then(intentResolver).should().resolve();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowIfActionButtonsClicked() {
        ResolverView.Listener listener = captureViewListener();

        listener.onActionButtonClick(false);
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

    private static IntentResolver.Data dataWithItem(DisplayResolveInfo item) {
        return new IntentResolver.Data(Collections.singletonList(item), null, false);
    }
}
