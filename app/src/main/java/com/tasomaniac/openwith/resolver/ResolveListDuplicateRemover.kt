package com.tasomaniac.openwith.resolver

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.M
import com.tasomaniac.openwith.util.isEqualTo
import javax.inject.Inject

class ResolveListDuplicateRemover @Inject constructor(
    private val packageManager: PackageManager
) {

    fun process(currentResolveList: MutableList<ResolveInfo>) {
        val flag = if (SDK_INT >= M) PackageManager.MATCH_ALL else PackageManager.MATCH_DEFAULT_ONLY
        val vr = Intent(Intent.ACTION_MAIN).addCategory("com.google.intent.category.DAYDREAM")
        val vrList = packageManager.queryIntentActivities(vr, flag)

        currentResolveList.removeAll { info ->
            vrList.any { vr ->
                vr.activityInfo isEqualTo info.activityInfo
            }
        }
    }
}
