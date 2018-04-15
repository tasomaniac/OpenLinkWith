package com.tasomaniac.openwith.homescreen

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Dialog
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.graphics.Bitmap
import android.os.Build.VERSION_CODES.M
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.content.pm.ShortcutInfoCompat
import android.support.v4.content.pm.ShortcutManagerCompat
import android.support.v4.graphics.drawable.IconCompat
import android.support.v7.app.AlertDialog
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.core.widget.toast
import butterknife.BindBitmap
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnEditorAction
import butterknife.OnTextChanged
import com.tasomaniac.android.widget.DelayedProgressBar
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.resolver.DisplayActivityInfo
import com.tasomaniac.openwith.util.Intents
import dagger.android.support.DaggerAppCompatDialogFragment
import timber.log.Timber
import javax.inject.Inject

@TargetApi(M)
class AddToHomeScreenDialogFragment : DaggerAppCompatDialogFragment() {

    @Inject lateinit var titleFetcher: TitleFetcher

    @BindView(R.id.add_to_home_screen_title) lateinit var titleView: EditText
    @BindView(R.id.add_to_home_screen_progress) lateinit var progressBar: DelayedProgressBar
    @BindBitmap(R.drawable.ic_bookmark) lateinit var shortcutMark: Bitmap

    private lateinit var shortcutIconCreator: ShortcutIconCreator

    private val activityToAdd: DisplayActivityInfo
        get() = arguments!!.getParcelable(KEY_ACTIVITY_TO_ADD)
    private val intent: Intent
        get() = arguments!!.getParcelable(KEY_INTENT)

    private val positiveButton: Button
        get() = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)


    override fun onStart() {
        super.onStart()
        onTitleChanged(titleView.text)
        shortcutIconCreator = ShortcutIconCreator(shortcutMark)

        if (titleView.text.isEmpty()) {
            showProgressBar()
            titleFetcher.fetch(
                intent.dataString,
                { title ->
                    hideProgressBar()
                    if (title != null && titleView.text.isEmpty()) {
                        titleView.post {
                            titleView.append(title)
                        }
                    }
                },
                ::hideProgressBar
            )
        }
    }

    override fun onDestroy() {
        titleFetcher.cancel()
        super.onDestroy()
    }

    private fun showProgressBar() {
        progressBar.post { progressBar.show() }
    }

    private fun hideProgressBar() {
        progressBar.post { progressBar.hide(true) }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        @SuppressLint("InflateParams")
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_add_to_home_screen, null)
        ButterKnife.bind(this, view)

        return AlertDialog.Builder(requireContext())
            .setPositiveButton(R.string.add) { _, _ -> createShortcutAndHandleError() }
            .setNegativeButton(R.string.cancel, null)
            .setView(view)
            .setTitle(R.string.add_to_homescreen)
            .create()
            .also { forceKeyboardVisible(it.window!!) }
    }

    @OnEditorAction(R.id.add_to_home_screen_title)
    fun onKeyboardDoneClicked(actionId: Int) = when (actionId) {
        EditorInfo.IME_ACTION_GO -> {
            createShortcutAndHandleError()
            true
        }
        else -> false
    }

    @OnTextChanged(R.id.add_to_home_screen_title)
    fun onTitleChanged(title: CharSequence) {
        positiveButton.isEnabled = title.isNotEmpty()
    }

    private fun createShortcutAndHandleError() {
        val success = createShortcut()
        if (!success) {
            requireContext().toast(R.string.add_to_home_screen_error)
        }
    }

    private fun createShortcut(): Boolean {
        val id = intent.dataString!! + activityToAdd.packageName()
        val label = titleView.text.toString()
        return try {
            createShortcutWith(id, label, shortcutIconCreator.createIconFor(activityToAdd.displayIcon()))
        } catch (e: Exception) {
            // This method started to fire android.os.TransactionTooLargeException
            Timber.e(e, "Exception while adding shortcut")
            createShortcutWith(id, label, createSimpleIcon())
        }
    }

    private fun createShortcutWith(id: String, label: String, icon: IconCompat): Boolean {
        val shortcut = ShortcutInfoCompat.Builder(requireContext(), id)
            .setIntent(intent)
            .setShortLabel(label)
            .setIcon(icon)
            .build()
        return ShortcutManagerCompat.requestPinShortcut(requireContext(), shortcut, startHomeScreen())
    }

    private fun createSimpleIcon(): IconCompat =
        IconCompat.createWithResource(requireContext(), R.mipmap.ic_launcher_bookmark)

    private fun startHomeScreen(): IntentSender =
        PendingIntent.getActivity(requireContext(), 0, Intents.homeScreenIntent(), 0).intentSender

    fun show(fragmentManager: FragmentManager) = show(fragmentManager, "AddToHomeScreen")

    companion object {

        private const val KEY_ACTIVITY_TO_ADD = "activity_to_add"
        private const val KEY_INTENT = "intent"

        @JvmStatic
        fun newInstance(activityInfo: DisplayActivityInfo, intent: Intent) =
            AddToHomeScreenDialogFragment().apply {
                arguments = bundleOf(
                    KEY_ACTIVITY_TO_ADD to activityInfo,
                    KEY_INTENT to intent
                )
            }

        private fun forceKeyboardVisible(window: Window) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }

    }
}
