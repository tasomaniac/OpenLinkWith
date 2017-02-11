package com.tasomaniac.openwith.resolver;

import android.content.res.Resources;
import android.support.annotation.Nullable;

import com.tasomaniac.openwith.R;

import java.util.List;

import timber.log.Timber;

class DefaultResolverPresenter implements ResolverPresenter {

    private final Resources resources;
    private final IntentResolver intentResolver;
    private final LastSelectedHolder lastSelectedHolder;

    DefaultResolverPresenter(Resources resources, IntentResolver intentResolver, LastSelectedHolder lastSelectedHolder) {
        this.resources = resources;
        this.intentResolver = intentResolver;
        this.lastSelectedHolder = lastSelectedHolder;
    }

    @Override
    public void bind(ResolverView view) {
        view.setListener(new ViewListener(view, intentResolver, lastSelectedHolder));
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
            boolean hasFilteredItem = filteredItem != null;
            if (totalCount == 1) {
                DisplayResolveInfo dri = hasFilteredItem ? filteredItem : list.get(0);
                view.startWithMessage(dri.intentFrom(intentResolver.getSourceIntent()), dri.displayLabel());
                view.finish();
                return;
            }

            view.setResolvedList(list);
            view.setupList(filteredItem, showExtended);
            view.setTitle(titleForAction(filteredItem));
            if (filteredItem != null) {
                view.setupFilteredView(filteredItem);
            }
        }

        private String titleForAction(DisplayResolveInfo item) {
            return item != null ?
                    resources.getString(R.string.which_view_application_named, item.displayLabel()) :
                    resources.getString(R.string.which_view_application);
        }
    }

    private class ViewListener implements ResolverView.Listener {

        private final ResolverView view;
        private final IntentResolver intentResolver;
        private final LastSelectedHolder lastSelectedHolder;

        ViewListener(ResolverView view, IntentResolver intentResolver, LastSelectedHolder lastSelectedHolder) {
            this.view = view;
            this.intentResolver = intentResolver;
            this.lastSelectedHolder = lastSelectedHolder;
        }

        @Override
        public void onActionButtonClick(boolean always) {
            view.startSelected(shouldUseAlwaysOption() ? lastSelectedHolder.lastSelected : intentResolver.getFilteredItem(), always);
        }

        @Override
        public void onItemClick(DisplayResolveInfo dri) {
            if (shouldUseAlwaysOption() && !dri.equals(lastSelectedHolder.lastSelected)) {
                view.enableActionButtons();
                lastSelectedHolder.lastSelected = dri;
            } else {
                view.startSelected(dri, false);
            }
        }

        @Override
        public void onPackagesChanged() {
            view.displayProgress();
            intentResolver.rebuildList();
        }

        private boolean shouldUseAlwaysOption() {
            return !intentResolver.hasFilteredItem();
        }
    }
}
