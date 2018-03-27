package com.tasomaniac.openwith.resolver

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.anyVararg
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.atLeastOnce
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.reset
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.data.PreferredAppDao
import com.tasomaniac.openwith.resolver.preferred.PreferredResolver
import com.tasomaniac.openwith.rx.ImmediateScheduling
import io.reactivex.Maybe
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then

class DefaultResolverPresenterTest {

    private val view = mock<ResolverView>()
    private val resources = mock<Resources>()
    private val uri = mock<Uri> {
        on { host } doReturn "host"
    }
    private val sourceIntent = mock<Intent> {
        on { data } doReturn uri
    }
    private val dao = mock<PreferredAppDao> {
        on { preferredAppByHost(any()) } doReturn Maybe.empty()
    }
    private val navigation = mock<ResolverNavigation>()
    private val intentResolver = mock<IntentResolver> {
        on { sourceIntent } doReturn sourceIntent
    }

    private val viewState = ViewState()
    private val useCase = ResolverUseCase(
        sourceIntent, PreferredResolver(mock(), dao),
        intentResolver, mock(), dao, ImmediateScheduling()
    )
    private val callerPackage = CallerPackage.from(mock {
        on { callingPackage } doReturn "callerPackage"
    })

    private val presenter = DefaultResolverPresenter(
        resources, sourceIntent, callerPackage,
        useCase, viewState
    )

    @Before
    fun setUp() {
        presenter.bind(view, navigation)
    }

    @Test
    fun unbindShouldNullifyListeners() {
        presenter.unbind(view)

        then(view).should().setListener(null)
        then(intentResolver).should().unbind()
    }

    @Test
    fun givenEmptyResolveListAndNoFilteredItemShouldDisplayWarningAndDismiss() {
        val listener = captureIntentResolverListener()
        reset(view)

        listener.onIntentResolved(EMPTY_DATA)

        then(view).should().toast(R.string.empty_resolver_activity)
        then(navigation).should().dismiss()
        then(view).shouldHaveNoMoreInteractions()
    }

    @Test
    fun givenSingleResolveListWithNoFilteredItemShouldStartFirstItem() {
        val listener = captureIntentResolverListener()
        reset(view)
        val intent = mock<Intent>()
        val label = "label"
        val item = givenDisplayResolveInfoWithIntentAndLabel(intent, label)

        listener.onIntentResolved(dataWith(item))

        then(navigation).should().startPreferred(intent, label)
        then(navigation).should().dismiss()
        then(view).shouldHaveZeroInteractions()
    }

    @Test
    fun givenEmptyResolveListWithFilteredItemShouldStartFirstItem() {
        val listener = captureIntentResolverListener()
        reset(view)
        val intent = mock<Intent>()
        val label = "label"
        val filteredItem = givenDisplayResolveInfoWithIntentAndLabel(intent, label)

        listener.onIntentResolved(dataWith(emptyList(), filteredItem))

        then(navigation).should().startPreferred(intent, label)
        then(navigation).should().dismiss()
        then(view).shouldHaveZeroInteractions()
    }

    @Test
    fun givenResolveListWithFilteredItemShouldDisplayTitleOfFiltered() {
        givenResourcesFiltered()
        val listener = captureIntentResolverListener()

        val filteredItem = givenDisplayResolveInfoWithIntentAndLabel(null, "filtered")
        listener.onIntentResolved(dataWith(NON_EMPTY_LIST, filteredItem))

        then(view).should().setTitle("filtered")
    }

    @Test
    fun givenMultipleItemsWithNoFilteredItemShouldDisplayFixedTitle() {
        val listener = captureIntentResolverListener()
        givenResources()

        val multipleItems = listOf<DisplayActivityInfo>(mock(), mock())
        listener.onIntentResolved(dataWith(multipleItems, null))

        then(view).should().setTitle("fixed")
    }

    @Test
    fun givenMultipleItemsShouldSetupActionButtons() {
        givenResources()
        val listener = captureIntentResolverListener()

        val multipleItems = listOf<DisplayActivityInfo>(mock(), mock())
        listener.onIntentResolved(dataWith(multipleItems, null))

        then(view).should().setupActionButtons()
    }

    private fun givenResourcesFiltered() {
        given(
            resources.getString(
                eq(R.string.which_view_application_named),
                anyVararg()
            )
        ).willReturn("filtered")
    }

    private fun givenResources() {
        given(resources.getString(R.string.which_view_application)).willReturn("fixed")
    }

    @Test
    fun shouldReloadWhenPackagedChanged() {
        val listener = captureViewListener()
        reset(intentResolver)

        listener.onPackagesChanged()

        then(intentResolver).should().resolve()
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrowIfActionButtonsClickedBeforeIntentsResolved() {
        val listener = captureViewListener()

        listener.onActionButtonClick(false)
    }

    @Test
    fun givenFilteredItemActionButtonShouldStartFiltered() {
        givenResourcesFiltered()
        val listener = captureViewListener()
        val resolverListener = captureIntentResolverListener()

        val intent = mock<Intent>()
        val filteredItem = givenDisplayResolveInfoWithIntentAndLabel(intent, "filtered")
        resolverListener.onIntentResolved(dataWith(NON_EMPTY_LIST, filteredItem))

        listener.onActionButtonClick(false)

        then(navigation).should().startSelected(intent)
    }

    @Test
    fun givenNoFilteredItemActionButtonShouldStartLastClickedItem() {
        val listener = captureViewListener()
        val resolverListener = captureIntentResolverListener()

        val intent = mock<Intent>()
        val item = givenDisplayResolveInfoWithIntentAndLabel(intent, "filtered")
        resolverListener.onIntentResolved(dataWith(item))

        listener.onItemClick(item)
        listener.onActionButtonClick(false)

        then(navigation).should().startSelected(intent)
    }

    @Test
    fun givenNoFilteredItemActionButtonShouldBeEnabledWhenItemClicked() {
        val listener = captureViewListener()
        val resolverListener = captureIntentResolverListener()

        resolverListener.onIntentResolved(dataWith(NON_EMPTY_LIST, null))

        listener.onItemClick(mock())

        then(view).should().enableActionButtons()
    }

    @Test
    fun givenNoFilteredShouldStoreLastSelectedWhenItemClicked() {
        val listener = captureViewListener()
        val resolverListener = captureIntentResolverListener()

        resolverListener.onIntentResolved(dataWith(NON_EMPTY_LIST, null))

        val item = mock<DisplayActivityInfo>()
        listener.onItemClick(item)

        assertEquals(item, viewState.lastSelected)
    }

    @Test
    fun givenNoFilteredShouldStartItemWhenItemClickedTwice() {
        val listener = captureViewListener()
        val resolverListener = captureIntentResolverListener()
        val intent = mock<Intent>()
        val item = givenDisplayResolveInfoWithIntentAndLabel(intent, "")

        resolverListener.onIntentResolved(dataWith(NON_EMPTY_LIST, null))

        listener.onItemClick(item)
        listener.onItemClick(item)

        then(navigation).should().startSelected(intent)
    }

    @Test
    fun givenFilteredShouldStartItemWhenItemClickedOnce() {
        givenResourcesFiltered()
        val listener = captureViewListener()
        val resolverListener = captureIntentResolverListener()
        val intent = mock<Intent>()
        val item = givenDisplayResolveInfoWithIntentAndLabel(intent, "")

        resolverListener.onIntentResolved(dataWith(NON_EMPTY_LIST, item))

        listener.onItemClick(item)

        then(navigation).should().startSelected(intent)
    }

    private fun captureIntentResolverListener() = with(argumentCaptor<IntentResolver.Listener>()) {
        then(intentResolver).should(atLeastOnce()).bind(capture())
        firstValue
    }

    private fun captureViewListener() = with(argumentCaptor<ResolverView.Listener>()) {
        then(view).should().setListener(capture())
        firstValue
    }

    companion object {
        private val EMPTY_DATA = dataWith(emptyList(), null)
        private val NON_EMPTY_LIST = listOf<DisplayActivityInfo>(mock())

        private fun dataWith(item: DisplayActivityInfo, filteredItem: DisplayActivityInfo? = null) =
            dataWith(listOf(item), filteredItem)

        private fun dataWith(resolved: List<DisplayActivityInfo>, filteredItem: DisplayActivityInfo?): IntentResolver.Data {
            return IntentResolver.Data(resolved, filteredItem, false)
        }

        private fun givenDisplayResolveInfoWithIntentAndLabel(intent: Intent?, label: String) =
            mock<DisplayActivityInfo> {
                on { intentFrom(any()) } doReturn intent
                on { displayLabel() } doReturn label
            }
    }
}
