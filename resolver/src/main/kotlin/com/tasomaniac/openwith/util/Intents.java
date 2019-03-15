package com.tasomaniac.openwith.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

public class Intents {

    public static Intent homeScreenIntent() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return homeIntent;
    }

    public static void startActivityFixingIntent(Context context, Intent intent)
            throws SecurityException, ActivityNotFoundException {
        context.startActivity(IntentFixer.fixIntents(context, intent));
    }

    private Intents() {
        //no instance
    }
}
