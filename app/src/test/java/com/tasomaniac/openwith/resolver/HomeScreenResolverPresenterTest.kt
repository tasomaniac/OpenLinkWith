package com.tasomaniac.openwith.resolver

import android.content.Intent
import android.content.res.Resources
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.then
import com.tasomaniac.openwith.R
import org.junit.Test

class HomeScreenResolverPresenterTest {

    private val intentResolver = mock<IntentResolver>()
    private val view = mock<ResolverView>()
    private val resources = mock<Resources>()
    private val sourceIntent = mock<Intent>()
    private val navigation = mock<ResolverNavigation>()

    private var presenter = HomeScreenResolverPresenter(resources, intentResolver).also {
        it.bind(view, navigation)
    }

    @Test
    fun unbindShouldNullifyListeners() {
        presenter.unbind(view)

        then(view).should().setListener(null)
        then(intentResolver).should().unbind()
    }

    @Test
    fun shouldHaveNoInteractionWithFilteredItem() {
        val listener = captureIntentResolverListener()

        val filteredItem = mock<DisplayActivityInfo>()
        listener.onIntentResolved(
            IntentResolverResult(
                emptyList(),
                filteredItem,
                false
            )
        )

        then(filteredItem).shouldHaveNoInteractions()
    }

    @Test
    fun givenEmptyResolveListShouldDisplayWarningAndDismiss() {
        given(intentResolver.sourceIntent).willReturn(sourceIntent)
        val listener = captureIntentResolverListener()
        reset(view)

        listener.onIntentResolved(EMPTY_RESULT)

        then(view).should().toast(R.string.empty_resolver_activity)
        then(navigation).should().dismiss()
        then(view).shouldHaveNoMoreInteractions()
    }

    @Test
    fun givenResolveListShouldSetupUI() {
        val listener = captureIntentResolverListener()

        val item = mock<DisplayActivityInfo>()
        val result = resultWithItem(item)
        listener.onIntentResolved(result)

        then(view).should().displayData(result)
    }

    @Test
    fun givenResolveListShouldDisplayTitle() {
        val listener = captureIntentResolverListener()
        given(resources.getString(R.string.add_to_homescreen)).willReturn(TITLE)

        val item = mock<DisplayActivityInfo>()
        listener.onIntentResolved(resultWithItem(item))

        then(view).should().setTitle(TITLE)
    }

    @Test
    fun shouldReloadWhenPackagedChanged() {
        val listener = captureViewListener()
        reset(intentResolver)

        listener.onPackagesChanged()

        then(intentResolver).should().resolve()
    }

    @Test(expected = IllegalStateException::class)
    fun shouldThrowIfActionButtonsClicked() {
        val listener = captureViewListener()

        listener.onActionButtonClick(false)
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

        private const val TITLE = "title"
        private val EMPTY_RESULT = IntentResolverResult(emptyList(), null, false)

        private fun resultWithItem(item: DisplayActivityInfo): IntentResolverResult {
            return IntentResolverResult(listOf(item), null, false)
        }
    }
}
