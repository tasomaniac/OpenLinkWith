package com.tasomaniac.openwith.resolver

import android.content.Intent
import android.content.res.Resources
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.data.PreferredApp
import com.tasomaniac.openwith.data.PreferredAppDao
import timber.log.Timber

internal class DefaultResolverPresenter(
    private val resources: Resources,
    private val history: ChooserHistory,
    private val intentResolver: IntentResolver,
    private val viewState: ViewState,
    private val dao: PreferredAppDao
) : ResolverPresenter {

  override fun bind(view: ResolverView, navigation: ResolverView.Navigation) {
    view.setListener(ViewListener(view, intentResolver, navigation))
    intentResolver.bind(IntentResolverListener(view, navigation))
  }

  override fun unbind(view: ResolverView) {
    view.setListener(null)
    intentResolver.unbind()
  }

  override fun release() {
    intentResolver.release()
  }

  private inner class IntentResolverListener(
      private val view: ResolverView,
      private val navigation: ResolverView.Navigation
  ) : IntentResolver.Listener {

    override fun onIntentResolved(data: IntentResolver.Data) {
      viewState.filteredItem = data.filteredItem

      if (data.isEmpty) {
        Timber.e("No app is found to handle url: %s", intentResolver.sourceIntent.dataString)
        view.toast(R.string.empty_resolver_activity)
        navigation.dismiss()
        return
      }
      if (data.totalCount() == 1) {
        val activityInfo = data.filteredItem ?: data.resolved[0]!!
        try {
          navigation.startPreferred(activityInfo.intentFrom(intentResolver.sourceIntent), activityInfo.displayLabel())
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
        resources.getString(R.string.which_view_application_named, filteredItem.displayLabel())
      else
        resources.getString(R.string.which_view_application)
    }
  }

  private inner class ViewListener(
      private val view: ResolverView,
      private val intentResolver: IntentResolver,
      private val navigation: ResolverView.Navigation
  ) : ResolverView.Listener {

    override fun onActionButtonClick(always: Boolean) {
      startAndPersist(viewState.checkedItem(), always)
      navigation.dismiss()
    }

    private fun persistSelectedIntent(intent: Intent, alwaysCheck: Boolean) {
      if (intent.component == null) {
        return
      }

      dao.insert(PreferredApp(
          host = intent.data.host,
          component = intent.component.flattenToString(),
          preferred = alwaysCheck,
          last_chosen = true
      ))
      history.add(intent.component.packageName)
      history.save()
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
      val intent = activityInfo.intentFrom(intentResolver.sourceIntent)
      navigation.startSelected(intent)
      persistSelectedIntent(intent, alwaysCheck)
    }

    override fun onPackagesChanged() {
      intentResolver.resolve()
    }
  }
}
