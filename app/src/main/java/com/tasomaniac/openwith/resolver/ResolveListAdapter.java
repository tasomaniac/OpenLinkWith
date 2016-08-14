package com.tasomaniac.openwith.resolver;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.karumi.headerrecyclerview.HeaderRecyclerViewAdapter;
import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.misc.ItemClickListener;
import com.tasomaniac.openwith.misc.ItemLongClickListener;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;

public class ResolveListAdapter extends HeaderRecyclerViewAdapter<ResolveListAdapter.ViewHolder, ResolveListAdapter.Header, DisplayResolveInfo, Void> {

    private static final long USAGE_STATS_PERIOD = TimeUnit.DAYS.toMillis(14);
    private static final Intent BROWSER_INTENT;
    static {
        BROWSER_INTENT = new Intent();
        BROWSER_INTENT.setAction(Intent.ACTION_VIEW);
        BROWSER_INTENT.addCategory(Intent.CATEGORY_BROWSABLE);
        BROWSER_INTENT.setData(Uri.parse("http:"));
    }

    private final Context mContext;
    private final ChooserHistory mHistory;
    private final Intent mIntent;
    private final String mCallerPackage;
    private final ComponentName lastChosenComponent;
    private final boolean mFilterLastUsed;
    private final PackageManager mPm;
    private final Map<String, UsageStats> mStats;
    private final int launcherIconDensity;

    private HashMap<String, Integer> priorityPackages;

    protected final List<DisplayResolveInfo> mList = new ArrayList<>();

    protected boolean mShowExtended;
    private boolean selectionEnabled;
    private int lastChosenPosition = RecyclerView.NO_POSITION;
    private int checkedItemPosition = RecyclerView.NO_POSITION;

    private ItemClickListener itemClickListener;
    private ItemLongClickListener itemLongClickListener;

    public ResolveListAdapter(Context context) {
        this(context, null, null, null, null, false, null);
    }

    ResolveListAdapter(Context context,
                       ChooserHistory history,
                       Intent intent,
                       String callerPackage,
                       ComponentName lastChosen,
                       boolean filterLastUsed,
                       String[] priorityPackages) {

        final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        launcherIconDensity = am.getLauncherLargeIconDensity();

        if (SDK_INT >= LOLLIPOP_MR1) {
            UsageStatsManager mUsm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

            final long sinceTime = System.currentTimeMillis() - USAGE_STATS_PERIOD;
            mStats = mUsm.queryAndAggregateUsageStats(sinceTime, System.currentTimeMillis());
        } else {
            mStats = null;
        }

        mPm = context.getPackageManager();

        mContext = context;
        mHistory = history;
        mIntent = intent;
        mCallerPackage = callerPackage;
        lastChosenComponent = lastChosen;
        mFilterLastUsed = filterLastUsed;

        setPriorityItems(priorityPackages);
        rebuildList();
    }

    private void setPriorityItems(final String... packageNames) {
        if (packageNames == null || packageNames.length == 0) {
            priorityPackages = null;
            return;
        }

        int size = packageNames.length;
        priorityPackages = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            // position 0 should have highest priority,
            // starting with 1 for lowest priority.
            priorityPackages.put(packageNames[i], size - i + 1);
        }
    }

    void handlePackagesChanged() {
        rebuildList();
        notifyDataSetChanged();
    }

    DisplayResolveInfo getFilteredItem() {
        if (mFilterLastUsed && lastChosenPosition >= 0) {
            // Not using getItem since it offsets to dodge this position for the list
            return mList.get(lastChosenPosition);
        }
        return null;
    }

    int getFilteredPosition() {
        if (mFilterLastUsed && lastChosenPosition >= 0) {
            return lastChosenPosition;
        }
        return AbsListView.INVALID_POSITION;
    }

    boolean hasFilteredItem() {
        return mFilterLastUsed && lastChosenPosition >= 0;
    }

    protected void rebuildList() {
        mList.clear();
        int flag;
        if (SDK_INT >= M) {
            flag = PackageManager.MATCH_ALL;
        } else {
            flag = PackageManager.MATCH_DEFAULT_ONLY;
        }
        flag = flag | (mFilterLastUsed ? PackageManager.GET_RESOLVED_FILTER : 0);

        List<ResolveInfo> currentResolveList = new ArrayList<>();
        currentResolveList.addAll(mPm.queryIntentActivities(mIntent, flag));

        if (SDK_INT >= M) {
            addBrowsersToList(currentResolveList, flag);
        }

        //Remove the components from the caller
        if (!TextUtils.isEmpty(mCallerPackage) && currentResolveList.size() > 1) {
            removePackageFromList(mCallerPackage, currentResolveList);
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
                Comparator<ResolveInfo> rComparator =
                        new ResolverComparator(mContext, mIntent);
                Collections.sort(currentResolveList, rComparator);
            }

            // Check for applications with same name and use application name or
            // package name if necessary
            r0 = currentResolveList.get(0);
            int start = 0;
            CharSequence r0Label = r0.loadLabel(mPm);
            mShowExtended = false;
            for (int i = 1; i < N; i++) {
                if (r0Label == null) {
                    r0Label = r0.activityInfo.packageName;
                }
                ResolveInfo ri = currentResolveList.get(i);
                CharSequence riLabel = ri.loadLabel(mPm);
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
        return mPm.queryIntentActivities(BROWSER_INTENT, flags);
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
            CharSequence startApp = ro.activityInfo.applicationInfo.loadLabel(mPm);
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
                    CharSequence jApp = jRi.activityInfo.applicationInfo.loadLabel(mPm);
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
                                                          add.activityInfo.applicationInfo.loadLabel(mPm)
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
        result += getHeaderViewsCount();
        return result;
    }

    @Override
    public DisplayResolveInfo getItem(int position) {
        position -= getHeaderViewsCount();
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

    protected int getHeaderViewsCount() {
        return hasHeader() ? 1 : 0;
    }

    @Override
    protected ViewHolder onCreateHeaderViewHolder(ViewGroup parent, int viewType) {
        View headerView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.resolver_different_item_header, parent, false);
        return new ViewHolder(headerView);
    }

    @Override
    protected void onBindHeaderViewHolder(ViewHolder holder, int position) {
    }

    @Override
    protected ViewHolder onCreateFooterViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    protected void onBindFooterViewHolder(ViewHolder holder, int position) {
    }

    @Override
    public ViewHolder onCreateItemViewHolder(ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.resolve_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);

        boolean checked = position == checkedItemPosition;
        holder.itemView.setActivated(checked);
    }

    @Override
    public void onBindItemViewHolder(final ViewHolder holder, final int position) {
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
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(v, holder.getAdapterPosition(), holder.getItemId());
                }

                setItemChecked(holder.getAdapterPosition());
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

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
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
        public TextView text2;
        @Nullable
        @BindView(R.id.icon)
        public ImageView icon;

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
                info.displayIcon(loadIconForResolveInfo(mPm, info.ri, launcherIconDensity));
            }
            return info;
        }

        @Override
        protected void onPostExecute(DisplayResolveInfo info) {
            notifyDataSetChanged();
        }
    }

    static Drawable loadIconForResolveInfo(PackageManager mPm, ResolveInfo ri, int mIconDpi) {
        Drawable dr;
        try {
            if (ri.resolvePackageName != null && ri.icon != 0) {
                dr = getIcon(mPm.getResourcesForApplication(ri.resolvePackageName), ri.icon, mIconDpi);
                if (dr != null) {
                    return dr;
                }
            }
            final int iconRes = ri.getIconResource();
            if (iconRes != 0) {
                dr = getIcon(mPm.getResourcesForApplication(ri.activityInfo.packageName), iconRes, mIconDpi);
                if (dr != null) {
                    return dr;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e, "Couldn't find resources for package");
        }
        return ri.loadIcon(mPm);
    }

    private static Drawable getIcon(Resources res, int resId, int mIconDpi) {
        Drawable result;
        try {
            //noinspection deprecation
            result = res.getDrawableForDensity(resId, mIconDpi);
        } catch (Resources.NotFoundException e) {
            result = null;
        }

        return result;
    }

    private class ResolverComparator implements Comparator<ResolveInfo> {
        private final Collator mCollator;
        private final boolean mHttp;

        ResolverComparator(Context context, Intent intent) {
            mCollator = Collator.getInstance(context.getResources().getConfiguration().locale);
            String scheme = intent.getScheme();
            mHttp = "http".equals(scheme) || "https".equals(scheme);
        }

        @Override
        public int compare(ResolveInfo lhs, ResolveInfo rhs) {

            if (mHttp) {
                // Special case: we want filters that match URI paths/schemes to be
                // ordered before others.  This is for the case when opening URIs,
                // to make native apps go above browsers.
                final boolean lhsSpecific = isSpecificUriMatch(lhs.match);
                final boolean rhsSpecific = isSpecificUriMatch(rhs.match);
                if (lhsSpecific != rhsSpecific) {
                    return lhsSpecific ? -1 : 1;
                }
            }

            if (mHistory != null) {
                int leftCount = mHistory.get(lhs.activityInfo.packageName);
                int rightCount = mHistory.get(rhs.activityInfo.packageName);
                if (leftCount != rightCount) {
                    return rightCount - leftCount;
                }
            }

            if (priorityPackages != null) {
                int leftPriority = getPriority(lhs);
                int rightPriority = getPriority(rhs);
                if (leftPriority != rightPriority) {
                    return rightPriority - leftPriority;
                }
            }

            if (mStats != null) {
                final long timeDiff =
                        getPackageTimeSpent(rhs.activityInfo.packageName) -
                                getPackageTimeSpent(lhs.activityInfo.packageName);

                if (timeDiff != 0) {
                    return timeDiff > 0 ? 1 : -1;
                }
            }

            CharSequence sa = lhs.loadLabel(mPm);
            if (sa == null) {
                sa = lhs.activityInfo.name;
            }
            CharSequence sb = rhs.loadLabel(mPm);
            if (sb == null) {
                sb = rhs.activityInfo.name;
            }

            return mCollator.compare(sa.toString(), sb.toString());
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        private long getPackageTimeSpent(String packageName) {
            if (mStats != null) {
                final UsageStats stats = mStats.get(packageName);
                if (stats != null) {
                    return stats.getTotalTimeInForeground();
                }

            }
            return 0;
        }

        private int getPriority(ResolveInfo lhs) {
            final Integer integer = priorityPackages.get(lhs.activityInfo.packageName);
            return integer != null ? integer : 0;
        }
    }

    Intent intentForDisplayResolveInfo(DisplayResolveInfo dri) {
        Intent intent = new Intent(mIntent);
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

    private static boolean isSpecificUriMatch(int match) {
        match = match & IntentFilter.MATCH_CATEGORY_MASK;
        return match >= IntentFilter.MATCH_CATEGORY_HOST
                && match <= IntentFilter.MATCH_CATEGORY_PATH;
    }

    public static class Header {
    }
}
