package com.tasomaniac.openwith.resolver

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.net.Uri
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.resolver.preferred.PreferredDisplayActivityInfo
import timber.log.Timber

internal class DefaultResolverPresenter(
    private val resources: Resources,
    private val sourceIntent: Intent,
    private val callerPackage: CallerPackage,
    private val useCase: ResolverUseCase,
    private val viewState: ViewState
) : ResolverPresenter {

    override fun bind(view: ResolverView, navigation: ResolverView.Navigation) {
        view.setListener(ViewListener(view, navigation))
        useCase.bind(UseCaseListener(view, navigation))
    }

    override fun unbind(view: ResolverView) {
        view.setListener(null)
        useCase.unbind()
    }

    override fun release() {
        useCase.release()
    }

    private inner class UseCaseListener(
        private val view: ResolverView,
        private val navigation: ResolverView.Navigation
    ) : ResolverUseCase.Listener {

        override fun onPreferredResolved(uri: Uri, preferred: PreferredDisplayActivityInfo) {
            val (app, activityInfo) = preferred
            val shouldStart = app.preferred && !isCallerPackagePreferred(activityInfo.activityInfo)
            if (shouldStart) {
                try {
                    val preferredIntent = Intent(Intent.ACTION_VIEW, uri)
                        .setComponent(app.componentName)
                    navigation.startPreferred(preferredIntent, activityInfo.displayLabel())
                    view.dismiss()
                } catch (e: Exception) {
                    Timber.e(e, "Security Exception for the url: %s", uri)
                    useCase.deleteFailedHost(uri)
                }
            }
        }

        private fun isCallerPackagePreferred(activityInfo: ActivityInfo) =
            activityInfo.packageName == callerPackage.callerPackage

        override fun onIntentResolved(data: IntentResolver.Data) {
            viewState.filteredItem = data.filteredItem

            if (data.isEmpty) {
                Timber.e("No app is found to handle url: %s", sourceIntent.dataString)
                view.toast(R.string.empty_resolver_activity)
                navigation.dismiss()
                return
            }
            if (data.totalCount() == 1) {
                val activityInfo = data.filteredItem ?: data.resolved[0]!!
                try {
                    navigation.startPreferred(
                        activityInfo.intentFrom(sourceIntent),
                        activityInfo.displayLabel()
                    )
                    navigation.dismiss()
                    return
                } catch (e: Exception) {
                    Timber.e(e)
                }

            }

            view.displayData(data)
            view.setTitle(titleForAction(data.filteredItem))
            view.setupActionButtons()
        }

        private fun titleForAction(filteredItem: DisplayActivityInfo?): String {
            return if (filteredItem != null)
                resources.getString(
                    R.string.which_view_application_named,
                    filteredItem.displayLabel()
                )
            else
                resources.getString(R.string.which_view_application)
        }
    }

    private inner class ViewListener(
        private val view: ResolverView,
        private val navigation: ResolverView.Navigation
    ) : ResolverView.Listener {

        override fun onActionButtonClick(always: Boolean) {
            startAndPersist(viewState.checkedItem(), always)
            navigation.dismiss()
        }

        override fun onItemClick(activityInfo: DisplayActivityInfo) {
            if (viewState.shouldUseAlwaysOption() && activityInfo != viewState.lastSelected) {
                view.enableActionButtons()
                viewState.lastSelected = activityInfo
            } else {
                startAndPersist(activityInfo, alwaysCheck = false)
            }
        }

        private fun startAndPersist(activityInfo: DisplayActivityInfo, alwaysCheck: Boolean) {
            val intent = activityInfo.intentFrom(sourceIntent)
            navigation.startSelected(intent)
            useCase.persistSelectedIntent(intent, alwaysCheck)
        }

        override fun onPackagesChanged() {
            useCase.resolve()
        }
    }
}
