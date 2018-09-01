package com.tasomaniac.openwith.resolver

import android.content.Intent
import dagger.Module
import dagger.Provides

@Module
class ResolverInputModule {

    @Provides
    fun callerPackage(activity: ResolverActivity): CallerPackage {
        return CallerPackage.from(activity)
    }

    @Provides
    fun sourceIntent(activity: ResolverActivity): Intent {
        return Intent(activity.intent).apply {
            component = null
            flags = flags and Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS.inv()
        }
    }
}
