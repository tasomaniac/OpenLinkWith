package com.tasomaniac.openwith.resolver;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.Nullable;

import com.tasomaniac.openwith.R;

import java.util.List;

import timber.log.Timber;

import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.*;
import static com.tasomaniac.openwith.data.OpenWithProvider.OpenWithHosts.CONTENT_URI;

class DefaultResolverPresenter implements ResolverPresenter {

    private final Resources resources;
    private final ChooserHistory history;
    private final ContentResolver contentResolver;
    private final IntentResolver intentResolver;
    private final LastSelectedHolder lastSelectedHolder;

    DefaultResolverPresenter(Resources resources, ChooserHistory history, ContentResolver contentResolver, IntentResolver intentResolver, LastSelectedHolder lastSelectedHolder) {
        this.resources = resources;
        this.history = history;
        this.contentResolver = contentResolver;
        this.intentResolver = intentResolver;
        this.lastSelectedHolder = lastSelectedHolder;
    }

    @Override
    public void bind(ResolverView view) {
        view.setListener(new ViewListener(view, intentResolver, lastSelectedHolder));

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
        public void onLoading() {
            view.displayProgress();
        }

        @Override
        public void onIntentResolved(List<DisplayResolveInfo> list, @Nullable DisplayResolveInfo filteredItem, boolean showExtended) {
            int totalCount = list.size() + (filteredItem != null ? 1 : 0);
            if (totalCount == 0) {
                Timber.e("No app is found to handle url: %s", intentResolver.getSourceIntent().getDataString());
                view.toast(R.string.empty_resolver_activity);
                view.dismiss();
                return;
            }
            boolean hasFilteredItem = filteredItem != null;
            if (totalCount == 1) {
                DisplayResolveInfo dri = hasFilteredItem ? filteredItem : list.get(0);
                view.startPreferred(dri.intentFrom(intentResolver.getSourceIntent()), dri.displayLabel());
                view.dismiss();
                return;
            }

            view.setResolvedList(list);
            view.setupList(filteredItem, showExtended);
            view.setTitle(titleForAction(filteredItem));
            if (hasFilteredItem) {
                view.setupFilteredView(filteredItem);

            }
            view.enableListSelection(!hasFilteredItem);
            view.setupActionButtons();
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
            DisplayResolveInfo dri = shouldUseAlwaysOption() ? lastSelectedHolder.lastSelected : intentResolver.getFilteredItem();
            startAndPersist(dri, always);
        }

        private void persistSelectedIntent(Intent intent, boolean alwaysCheck) {
            if (intent.getComponent() == null) {
                return;
            }
            ContentValues values = new ContentValues(4);
            values.put(HOST, intent.getData().getHost());
            values.put(COMPONENT, intent.getComponent().flattenToString());

            if (alwaysCheck) {
                values.put(PREFERRED, true);
            }
            values.put(LAST_CHOSEN, true);
            try {
                contentResolver.insert(CONTENT_URI, values);
            } catch (Exception e) {
                Timber.e(e, "Error while saving selected Intent");
            }

            history.add(intent.getComponent().getPackageName());
            history.save();
        }

        @Override
        public void onItemClick(DisplayResolveInfo dri) {
            if (shouldUseAlwaysOption() && !dri.equals(lastSelectedHolder.lastSelected)) {
                view.enableActionButtons();
                lastSelectedHolder.lastSelected = dri;
            } else {
                startAndPersist(dri, false);
            }
        }

        private void startAndPersist(DisplayResolveInfo dri, boolean alwaysCheck) {
            Intent intent = dri.intentFrom(intentResolver.getSourceIntent());
            view.startSelected(intent);
            persistSelectedIntent(intent, alwaysCheck);
        }

        @Override
        public void onPackagesChanged() {
            intentResolver.resolve();
        }

        private boolean shouldUseAlwaysOption() {
            return !intentResolver.hasFilteredItem();
        }
    }
}
