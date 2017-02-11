package com.tasomaniac.openwith.resolver;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.List;

interface ResolverView {

    void displayProgress();

    void setListener(@Nullable Listener listener);

    void startSelected(DisplayResolveInfo dri, boolean always);

    void startWithMessage(Intent intent, CharSequence appLabel);

    void enableActionButtons();

    void setTitle(String title);

    void setResolvedList(List<DisplayResolveInfo> list);

    void setupList(DisplayResolveInfo filteredItem, boolean shouldDisplayExtendedInfo);

    void setupFilteredView(DisplayResolveInfo filteredItem);

    void toast(@StringRes int titleRes);

    void finish();

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
