package com.tasomaniac.openwith.resolver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tasomaniac.openwith.IconLoader;
import com.tasomaniac.openwith.R;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.Lazy;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;
import static com.tasomaniac.openwith.resolver.ResolverActivity.EXTRA_LAST_CHOSEN_COMPONENT;

public class ResolveListAdapter extends RecyclerView.Adapter<ResolveListAdapter.ViewHolder> {

    private static final int TYPE_ITEM = 2;
    private static final int TYPE_HEADER = 1;
    private static final Intent BROWSER_INTENT;

    static {
        BROWSER_INTENT = new Intent();
        BROWSER_INTENT.setAction(Intent.ACTION_VIEW);
        BROWSER_INTENT.addCategory(Intent.CATEGORY_BROWSABLE);
        BROWSER_INTENT.setData(Uri.parse("http:"));
    }

    private final PackageManager packageManager;
    private final Lazy<ResolverComparator> resolverComparator;
    private final IconLoader iconLoader;
    private final Intent sourceIntent;
    private final String callerPackage;
    private final ComponentName lastChosenComponent;
    private final boolean mFilterLastUsed;

    protected final List<DisplayResolveInfo> mList = new ArrayList<>();

    protected boolean mShowExtended;
    private boolean hasHeader;
    private boolean selectionEnabled;
    private int lastChosenPosition = RecyclerView.NO_POSITION;
    private int checkedItemPosition = RecyclerView.NO_POSITION;

    private ItemClickListener itemClickListener;
    private ItemLongClickListener itemLongClickListener;

    public ResolveListAdapter(Context context, IconLoader iconLoader) {
        this(context, new Lazy<ResolverComparator>() {
            @Override
            public ResolverComparator get() {
                return null;
            }
        }, iconLoader, null);
    }

    @Inject
    public ResolveListAdapter(Context context,
                              Lazy<ResolverComparator> resolverComparator,
                              IconLoader iconLoader,
                              Intent intent) {
        this.packageManager = context.getPackageManager();
        this.resolverComparator = resolverComparator;
        this.iconLoader = iconLoader;
        this.sourceIntent = intent;

        if (sourceIntent != null) {
            callerPackage = sourceIntent.getStringExtra(ShareCompat.EXTRA_CALLING_PACKAGE);
            lastChosenComponent = sourceIntent.getParcelableExtra(EXTRA_LAST_CHOSEN_COMPONENT);
            mFilterLastUsed = true;
            rebuildList(sourceIntent);
        } else {
            callerPackage = null;
            lastChosenComponent = null;
            mFilterLastUsed = false;
        }
    }

    void handlePackagesChanged() {
        rebuildList(sourceIntent);
        notifyDataSetChanged();
    }

    DisplayResolveInfo getFilteredItem() {
        if (mFilterLastUsed && lastChosenPosition >= 0) {
            // Not using getItem since it offsets to dodge this position for the list
            return mList.get(lastChosenPosition);
        }
        return null;
    }

    boolean hasFilteredItem() {
        return mFilterLastUsed && lastChosenPosition >= 0;
    }

    private void rebuildList(Intent intent) {
        mList.clear();
        int flag;
        if (SDK_INT >= M) {
            flag = PackageManager.MATCH_ALL;
        } else {
            flag = PackageManager.MATCH_DEFAULT_ONLY;
        }
        flag = flag | (mFilterLastUsed ? PackageManager.GET_RESOLVED_FILTER : 0);

        List<ResolveInfo> currentResolveList = new ArrayList<>();
        currentResolveList.addAll(packageManager.queryIntentActivities(intent, flag));

        if (SDK_INT >= M) {
            addBrowsersToList(currentResolveList, flag);
        }

        //Remove the components from the caller
        if (!TextUtils.isEmpty(callerPackage)) {
            removePackageFromList(callerPackage, currentResolveList);
        }

        int N;
        if ((N = currentResolveList.size()) > 0) {
            // Only display the first matches that are either of equal
            // priority or have asked to be default options.
            ResolveInfo r0 = currentResolveList.get(0);
            for (int i = 1; i < N; i++) {
                ResolveInfo ri = currentResolveList.get(i);

                if (r0.priority != ri.priority ||
                        r0.isDefault != ri.isDefault) {
                    while (i < N) {
                        currentResolveList.remove(i);
                        N--;
                    }
                }
            }

            //If there is no left, return
            if (N <= 0) {
                return;
            }

            if (N > 1) {
                Collections.sort(currentResolveList, resolverComparator.get());
            }

            // Check for applications with same name and use application name or
            // package name if necessary
            r0 = currentResolveList.get(0);
            int start = 0;
            CharSequence r0Label = r0.loadLabel(packageManager);
            mShowExtended = false;
            for (int i = 1; i < N; i++) {
                if (r0Label == null) {
                    r0Label = r0.activityInfo.packageName;
                }
                ResolveInfo ri = currentResolveList.get(i);
                CharSequence riLabel = ri.loadLabel(packageManager);
                if (riLabel == null) {
                    riLabel = ri.activityInfo.packageName;
                }
                if (riLabel.equals(r0Label)) {
                    continue;
                }
                processGroup(currentResolveList, start, (i - 1), r0, r0Label);
                r0 = ri;
                r0Label = riLabel;
                start = i;
            }
            // Process last group
            processGroup(currentResolveList, start, (N - 1), r0, r0Label);
        }
    }

    private static void removePackageFromList(final String packageName, List<ResolveInfo> currentResolveList) {
        List<ResolveInfo> infosToRemoved = new ArrayList<>();
        for (ResolveInfo info : currentResolveList) {
            if (info.activityInfo.packageName.equals(packageName)) {
                infosToRemoved.add(info);
            }
        }
        currentResolveList.removeAll(infosToRemoved);
    }

    private void addBrowsersToList(List<ResolveInfo> currentResolveList, int flag) {
        final int initialSize = currentResolveList.size();

        List<ResolveInfo> browsers = queryBrowserIntentActivities(flag);
        for (ResolveInfo browser : browsers) {
            boolean browserFound = false;

            for (int i = 0; i < initialSize; i++) {
                ResolveInfo info = currentResolveList.get(i);

                if (info.activityInfo.packageName.equals(browser.activityInfo.packageName)) {
                    browserFound = true;
                    break;
                }
            }

            if (!browserFound) {
                currentResolveList.add(browser);
            }
        }
    }

    private List<ResolveInfo> queryBrowserIntentActivities(int flags) {
        return packageManager.queryIntentActivities(BROWSER_INTENT, flags);
    }

    private void processGroup(List<ResolveInfo> rList, int start, int end, ResolveInfo ro,
                              CharSequence roLabel) {
        // Process labels from start to i
        int num = end - start + 1;
        if (num == 1) {
            // No duplicate labels. Use label for entry at start
            addResolveInfo(new DisplayResolveInfo(ro, roLabel, null));
            updateLastChosenPosition(ro);
        } else {
            mShowExtended = true;
            boolean usePkg = false;
            CharSequence startApp = ro.activityInfo.applicationInfo.loadLabel(packageManager);
            if (startApp == null) {
                usePkg = true;
            }
            if (!usePkg) {
                // Use HashSet to track duplicates
                HashSet<CharSequence> duplicates =
                        new HashSet<>();
                duplicates.add(startApp);
                for (int j = start + 1; j <= end; j++) {
                    ResolveInfo jRi = rList.get(j);
                    CharSequence jApp = jRi.activityInfo.applicationInfo.loadLabel(packageManager);
                    if ((jApp == null) || (duplicates.contains(jApp))) {
                        usePkg = true;
                        break;
                    } else {
                        duplicates.add(jApp);
                    }
                }
                // Clear HashSet for later use
                duplicates.clear();
            }
            for (int k = start; k <= end; k++) {
                ResolveInfo add = rList.get(k);
                if (usePkg) {
                    // Use application name for all entries from start to end-1
                    addResolveInfo(new DisplayResolveInfo(add, roLabel,
                                                          add.activityInfo.packageName
                    ));
                } else {
                    // Use package name for all entries from start to end-1
                    addResolveInfo(new DisplayResolveInfo(add, roLabel,
                                                          add.activityInfo.applicationInfo.loadLabel(packageManager)
                    ));
                }
                updateLastChosenPosition(add);
            }
        }
    }

    private void updateLastChosenPosition(ResolveInfo info) {
        if (lastChosenComponent != null
                && lastChosenComponent.getPackageName().equals(info.activityInfo.packageName)
                && lastChosenComponent.getClassName().equals(info.activityInfo.name)) {
            lastChosenPosition = mList.size() - 1;
        }
    }

    private void addResolveInfo(DisplayResolveInfo dri) {
        mList.add(dri);
    }

    DisplayResolveInfo displayResolveInfoForPosition(int position, boolean filtered) {
        return filtered ? getItem(position) : mList.get(position);
    }

    @Override
    public int getItemCount() {
        int result = mList.size();
        if (mFilterLastUsed && lastChosenPosition >= 0) {
            result--;
        }
        result += getHeadersCount();
        return result;
    }

    public DisplayResolveInfo getItem(int position) {
        position -= getHeadersCount();
        if (position < 0) {
            position = 0;
        }
        if (mFilterLastUsed && lastChosenPosition >= 0 && position >= lastChosenPosition) {
            position++;
        }
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    protected int getHeadersCount() {
        return hasHeader ? 1 : 0;
    }

    public void displayHeader() {
        hasHeader = true;
    }

    @Override
    public int getItemViewType(int position) {
        if (hasHeader && position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    protected ViewHolder onCreateHeaderViewHolder(ViewGroup parent, int viewType) {
        View headerView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.resolver_different_item_header, parent, false);
        return new ViewHolder(headerView);
    }

    protected void onBindHeaderViewHolder(ViewHolder holder) {
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (TYPE_HEADER == viewType) {
            return onCreateHeaderViewHolder(parent, viewType);
        }
        if (TYPE_ITEM == viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.resolve_list_item, parent, false);
            return new ViewHolder(itemView);
        }
        throw new IllegalStateException("Unknown viewType: + " + viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);

        boolean checked = position == checkedItemPosition;
        holder.itemView.setActivated(checked);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (TYPE_HEADER == getItemViewType(position)) {
            onBindHeaderViewHolder(holder);
            return;
        }

        final DisplayResolveInfo info = getItem(position);

        holder.text.setText(info.displayLabel());
        if (holder.text2 != null) {
            if (mShowExtended) {
                holder.text2.setVisibility(View.VISIBLE);
                holder.text2.setText(info.extendedInfo());
            } else {
                holder.text2.setVisibility(View.GONE);
            }
        }
        if (holder.icon != null) {
            if (info.displayIcon() == null) {
                new LoadIconTask().execute(info);
            }
            holder.icon.setImageDrawable(info.displayIcon());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                itemClickListener.onItemClick(getItem(adapterPosition));
                setItemChecked(adapterPosition);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return itemLongClickListener != null
                        && itemLongClickListener.onItemLongClick(v, holder.getAdapterPosition(), holder.getItemId());
            }
        });
    }

    public void setItemClickListener(@Nullable ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener == null ? ItemClickListener.EMPTY : itemClickListener;
    }

    void setItemLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }

    int getCheckedItemPosition() {
        return checkedItemPosition;
    }

    void setSelectionEnabled(boolean selectionEnabled) {
        this.selectionEnabled = selectionEnabled;
    }

    void setItemChecked(int position) {
        if (!selectionEnabled) {
            return;
        }

        notifyItemChanged(position, true);
        notifyItemChanged(checkedItemPosition, false);

        checkedItemPosition = position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text1)
        public TextView text;
        @Nullable
        @BindView(R.id.text2)
        TextView text2;
        @Nullable
        @BindView(R.id.icon)
        ImageView icon;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    private class LoadIconTask extends AsyncTask<DisplayResolveInfo, Void, DisplayResolveInfo> {
        @Override
        protected DisplayResolveInfo doInBackground(DisplayResolveInfo... params) {
            final DisplayResolveInfo info = params[0];
            if (info.displayIcon() == null) {
                info.displayIcon(iconLoader.loadFor(info.ri));
            }
            return info;
        }

        @Override
        protected void onPostExecute(DisplayResolveInfo info) {
            notifyDataSetChanged();
        }
    }

    Intent intentForDisplayResolveInfo(DisplayResolveInfo dri) {
        Intent intent = new Intent(sourceIntent);
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT
                                | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        if (dri != null && dri.ri != null) {
            ActivityInfo ai = dri.ri.activityInfo;
            if (ai != null) {
                intent.setComponent(new ComponentName(
                        ai.applicationInfo.packageName, ai.name));
            }
        }
        return intent;
    }
}
