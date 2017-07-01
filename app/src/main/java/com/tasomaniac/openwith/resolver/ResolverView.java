package com.tasomaniac.openwith.resolver;

import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

interface ResolverView {

    void startSelected(Intent intent);

    void startPreferred(Intent intent, CharSequence appLabel);

    void displayAddToHomeScreenDialog(DisplayResolveInfo dri, Intent intent);

    void displayData(IntentResolver.Data data, @LayoutRes int layoutRes);

    void setTitle(String title);

    void setupActionButtons();

    void enableActionButtons();

    void toast(@StringRes int titleRes);

    void dismiss();

    void setListener(@Nullable Listener listener);

    interface Listener {
        void onActionButtonClick(boolean always);

        void onItemClick(DisplayResolveInfo dri);

        void onPackagesChanged();

        Listener EMPTY = new Listener() {

            @Override
            public void onActionButtonClick(boolean always) {

            }

            @Override
            public void onItemClick(DisplayResolveInfo dri) {
                // no-op
            }

            @Override
            public void onPackagesChanged() {

            }
        };
    }
}
