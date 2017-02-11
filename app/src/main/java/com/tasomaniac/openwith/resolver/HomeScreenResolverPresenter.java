package com.tasomaniac.openwith.resolver;

import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.homescreen.AddToHomeScreenDialogFragment;

import java.util.List;

import timber.log.Timber;

class HomeScreenResolverPresenter implements ResolverPresenter {

    private final Resources resources;
    private final IntentResolver intentResolver;
    private final FragmentManager fragmentManager;

    HomeScreenResolverPresenter(Resources resources, IntentResolver intentResolver, FragmentManager fragmentManager) {
        this.resources = resources;
        this.intentResolver = intentResolver;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public void bind(ResolverView view) {
        view.setListener(new ViewListener(view, intentResolver));
        intentResolver.setListener(new IntentResolverListener(view));

        view.displayProgress();
        intentResolver.rebuildList();
    }

    @Override
    public void unbind(ResolverView view) {
        view.setListener(null);
        intentResolver.setListener(null);
    }

    private class IntentResolverListener implements IntentResolver.Listener {

        private final ResolverView view;

        IntentResolverListener(ResolverView view) {
            this.view = view;
        }

        @Override
        public void onIntentResolved(List<DisplayResolveInfo> list, @Nullable DisplayResolveInfo filteredItem, boolean showExtended) {
            int totalCount = list.size() + (filteredItem != null ? 1 : 0);
            if (totalCount == 0) {
                Timber.e("No app is found to handle url: %s", intentResolver.getSourceIntent().getDataString());
                view.toast(R.string.empty_resolver_activity);
                view.finish();
                return;
            }
            view.setResolvedList(list);
            view.setupList(filteredItem, showExtended);
            view.setTitle(resources.getString(R.string.add_to_homescreen));
        }

    }

    private class ViewListener implements ResolverView.Listener {

        private final ResolverView view;
        private final IntentResolver intentResolver;

        ViewListener(ResolverView view, IntentResolver intentResolver) {
            this.view = view;
            this.intentResolver = intentResolver;
        }

        @Override
        public void onActionButtonClick(boolean always) {
            throw new IllegalStateException("Action buttons should not be visible for AddToHomeScreen");
        }

        @Override
        public void onItemClick(DisplayResolveInfo dri) {
            Intent intent = dri.intentFrom(intentResolver.getSourceIntent());
            AddToHomeScreenDialogFragment
                    .newInstance(dri, intent)
                    .show(fragmentManager);
        }

        @Override
        public void onPackagesChanged() {
            view.displayProgress();
            intentResolver.rebuildList();
        }
    }
}
