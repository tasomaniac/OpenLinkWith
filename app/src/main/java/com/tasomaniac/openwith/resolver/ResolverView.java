package com.tasomaniac.openwith.resolver;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import java.util.List;

interface ResolverView {

    void displayProgress();

    void startSelected(Intent intent);

    void startPreferred(Intent intent, CharSequence appLabel);

    void setResolvedList(List<DisplayResolveInfo> list);

    void setupList(DisplayResolveInfo filteredItem, boolean shouldDisplayExtendedInfo);

    void setTitle(String title);

    void setupFilteredView(DisplayResolveInfo filteredItem);

    void enableListSelection(boolean value);

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
