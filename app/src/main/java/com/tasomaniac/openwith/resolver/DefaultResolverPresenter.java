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
    private final ViewState viewState;

    DefaultResolverPresenter(Resources resources,
                             ChooserHistory history,
                             ContentResolver contentResolver,
                             IntentResolver intentResolver,
                             ViewState viewState) {
        this.resources = resources;
        this.history = history;
        this.contentResolver = contentResolver;
        this.intentResolver = intentResolver;
        this.viewState = viewState;
    }

    @Override
    public void bind(ResolverView view) {
        view.setListener(new ViewListener(view, intentResolver));

        IntentResolverListener listener = new IntentResolverListener(view);
        intentResolver.setListener(listener);
        if (intentResolver.getState() == null) {
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
            viewState.filteredItem = filteredItem;

            int totalCount = list.size() + (filteredItem != null ? 1 : 0);
            if (totalCount == 0) {
                Timber.e("No app is found to handle url: %s", intentResolver.getSourceIntent().getDataString());
                view.toast(R.string.empty_resolver_activity);
                view.dismiss();
                return;
            }
            if (totalCount == 1) {
                DisplayResolveInfo dri = filteredItem != null ? filteredItem : list.get(0);
                view.startPreferred(dri.intentFrom(intentResolver.getSourceIntent()), dri.displayLabel());
                view.dismiss();
                return;
            }

            view.setResolvedList(list);
            view.setupUI(filteredItem != null ? R.layout.resolver_list_with_default : R.layout.resolver_list, showExtended);
            view.setTitle(titleForAction(filteredItem));
            view.setFilteredItem(filteredItem);
            view.setupActionButtons();
        }

        private String titleForAction(DisplayResolveInfo filteredItem) {
            return filteredItem != null ?
                    resources.getString(R.string.which_view_application_named, filteredItem.displayLabel()) :
                    resources.getString(R.string.which_view_application);
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
            startAndPersist(viewState.checkedItem(), always);
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
            if (viewState.shouldUseAlwaysOption() && !dri.equals(viewState.lastSelected)) {
                view.enableActionButtons();
                viewState.lastSelected = dri;
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
    }
}
