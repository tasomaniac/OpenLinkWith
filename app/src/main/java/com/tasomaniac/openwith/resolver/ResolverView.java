package com.tasomaniac.openwith.resolver;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

interface ResolverView {

    void displayAddToHomeScreenDialog(DisplayActivityInfo activityInfo, Intent intent);

    void displayData(IntentResolver.Data data);

    void setTitle(String title);

    void setupActionButtons();

    void enableActionButtons();

    void toast(@StringRes int titleRes);

    void setListener(@Nullable Listener listener);

    interface Navigation {
        void startSelected(Intent intent);

        void startPreferred(Intent intent, CharSequence appLabel);

        void dismiss();
    }

    interface Listener {
        void onActionButtonClick(boolean always);

        void onItemClick(DisplayActivityInfo activityInfo);

        void onPackagesChanged();

        Listener EMPTY = new Listener() {

            @Override
            public void onActionButtonClick(boolean always) {

            }

            @Override
            public void onItemClick(DisplayActivityInfo activityInfo) {
                // no-op
            }

            @Override
            public void onPackagesChanged() {

            }
        };
    }
}
