package com.tasomaniac.openwith.resolver;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;

import com.tasomaniac.openwith.R;

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
    public void bind(ResolverView view, ResolverView.Navigation navigation) {
        view.setListener(new ViewListener(view, intentResolver, navigation));
        intentResolver.bind(new IntentResolverListener(view, navigation));
    }

    @Override
    public void unbind(ResolverView view) {
        view.setListener(null);
        intentResolver.unbind();
    }

    @Override
    public void release() {
        intentResolver.release();
    }

    private class IntentResolverListener implements IntentResolver.Listener {

        private final ResolverView view;
        private final ResolverView.Navigation navigation;

        IntentResolverListener(ResolverView view, ResolverView.Navigation navigation) {
            this.view = view;
            this.navigation = navigation;
        }

        @Override
        public void onIntentResolved(IntentResolver.Data data) {
            viewState.filteredItem = data.filteredItem;

            if (data.isEmpty()) {
                Timber.e("No app is found to handle url: %s", intentResolver.getSourceIntent().getDataString());
                view.toast(R.string.empty_resolver_activity);
                navigation.dismiss();
                return;
            }
            if (data.totalCount() == 1) {
                DisplayResolveInfo dri = data.filteredItem != null ? data.filteredItem : data.resolved.get(0);
                try {
                    navigation.startPreferred(dri.intentFrom(intentResolver.getSourceIntent()), dri.displayLabel());
                    navigation.dismiss();
                    return;
                } catch (Exception e) {
                    Timber.e(e);
                }
            }

            view.displayData(data);
            view.setTitle(titleForAction(data.filteredItem));
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
        private final ResolverView.Navigation navigation;

        ViewListener(ResolverView view, IntentResolver intentResolver, ResolverView.Navigation navigation) {
            this.view = view;
            this.intentResolver = intentResolver;
            this.navigation = navigation;
        }

        @Override
        public void onActionButtonClick(boolean always) {
            startAndPersist(viewState.checkedItem(), always);
            navigation.dismiss();
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
            navigation.startSelected(intent);
            persistSelectedIntent(intent, alwaysCheck);
        }

        @Override
        public void onPackagesChanged() {
            intentResolver.resolve();
        }
    }
}
