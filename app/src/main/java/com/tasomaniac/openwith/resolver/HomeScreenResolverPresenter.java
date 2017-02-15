package com.tasomaniac.openwith.resolver;

import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.Nullable;

import com.tasomaniac.openwith.R;

import java.util.List;

import timber.log.Timber;

class HomeScreenResolverPresenter implements ResolverPresenter {

    private final Resources resources;
    private final IntentResolver intentResolver;

    HomeScreenResolverPresenter(Resources resources, IntentResolver intentResolver) {
        this.resources = resources;
        this.intentResolver = intentResolver;
    }

    @Override
    public void bind(ResolverView view) {
        view.setListener(new ViewListener(intentResolver, view));

        IntentResolverListener listener = new IntentResolverListener(view);
        intentResolver.setListener(listener);
        if (intentResolver.getState() == IntentResolver.State.IDLE) {
            intentResolver.resolve();
        } else {
            intentResolver.getState().notify(listener);
        }
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
            int totalCount = list.size();
            if (totalCount == 0) {
                Timber.e("No app is found to handle url: %s", intentResolver.getSourceIntent().getDataString());
                view.toast(R.string.empty_resolver_activity);
                view.dismiss();
                return;
            }
            view.setResolvedList(list);
            view.setupUI(R.layout.resolver_list, showExtended);
            view.setTitle(resources.getString(R.string.add_to_homescreen));
        }
    }

    private class ViewListener implements ResolverView.Listener {

        private final IntentResolver intentResolver;
        private final ResolverView view;

        ViewListener(IntentResolver intentResolver, ResolverView view) {
            this.intentResolver = intentResolver;
            this.view = view;
        }

        @Override
        public void onActionButtonClick(boolean always) {
            throw new IllegalStateException("Action buttons should not be visible for AddToHomeScreen");
        }

        @Override
        public void onItemClick(DisplayResolveInfo dri) {
            Intent intent = dri.intentFrom(intentResolver.getSourceIntent());
            view.displayAddToHomeScreenDialog(dri, intent);
        }

        @Override
        public void onPackagesChanged() {
            intentResolver.resolve();
        }
    }
}
