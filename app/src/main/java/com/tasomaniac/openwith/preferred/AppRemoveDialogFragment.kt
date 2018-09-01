package com.tasomaniac.openwith.preferred

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.core.text.parseAsHtml
import com.tasomaniac.openwith.R
import com.tasomaniac.openwith.resolver.DisplayActivityInfo

class AppRemoveDialogFragment : AppCompatDialogFragment() {

    private var callbacks: Callbacks? = null

    private val info: DisplayActivityInfo
        get() = arguments!!.getParcelable(EXTRA_INFO)!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks
    }

    override fun onDetach() {
        callbacks = null
        super.onDetach()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message = resources.getString(
            R.string.message_remove_preferred,
            info.displayLabel, info.extendedInfo, info.extendedInfo
        ).parseAsHtml()

        return AlertDialog.Builder(activity!!)
            .setTitle(R.string.title_remove_preferred)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { _, _ -> callbacks!!.onAppRemoved(info) }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    interface Callbacks {
        fun onAppRemoved(info: DisplayActivityInfo)
    }

    companion object {

        private const val EXTRA_INFO = "EXTRA_INFO"

        internal fun newInstance(info: DisplayActivityInfo) =
            AppRemoveDialogFragment().apply {
                arguments = bundleOf(EXTRA_INFO to info)
            }
    }
}
