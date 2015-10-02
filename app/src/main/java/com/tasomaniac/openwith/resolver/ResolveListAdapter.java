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
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.karumi.headerrecyclerview.HeaderRecyclerViewAdapter;
import com.tasomaniac.openwith.R;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class ResolveListAdapter extends HeaderRecyclerViewAdapter<ResolveListAdapter.ViewHolder, ResolveListAdapter.Header, DisplayResolveInfo, Void> {
    private static final String TAG = "ResolveListAdapter";

    private boolean mShowExtended;
    private final int mIconDpi;

    private Intent mIntent;

    private ResolveInfo mLastChosen;
    private final LayoutInflater mInflater;

    List<DisplayResolveInfo> mList;
    List<ResolveInfo> mOrigResolveList;

    private int mLastChosenPosition = -1;
    private boolean mFilterLastUsed;

    private PackageManager mPm;
    private Map<String, UsageStats> mStats;
    private static final long USAGE_STATS_PERIOD = 1000 * 60 * 60 * 24 * 14;

    private OnItemClickedListener mOnItemClickedListener;

    private OnItemLongClickedListener mOnItemLongClickedListener;
    private ChooserHistory mHistory;
    private final Context mContext;
    private final ComponentName mCallerActivity;

    private HashMap<String, Integer> mPriorities;

    private int checkedItemPosition = AbsListView.INVALID_POSITION;
    private boolean mIsSelectable = false;

    public void setOnItemClickedListener(OnItemClickedListener mOnItemClickedListener) {
        this.mOnItemClickedListener = mOnItemClickedListener;
    }

    public void setOnItemLongClickedListener(OnItemLongClickedListener mOnItemLongClickedListener) {
        this.mOnItemLongClickedListener = mOnItemLongClickedListener;
    }

    public ResolveListAdapter(Context context,
                              ChooserHistory history,
                              Intent intent,
                              ComponentName callerActivity,
                              ResolveInfo lastChosen,
                              boolean filterLastUsed) {

        final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        mIconDpi = am.getLauncherLargeIconDensity();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            UsageStatsManager mUsm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

            final long sinceTime = System.currentTimeMillis() - USAGE_STATS_PERIOD;
            mStats = mUsm.queryAndAggregateUsageStats(sinceTime, System.currentTimeMillis());
        }

        mPm = context.getPackageManager();
        mInflater = LayoutInflater.from(context);
        mList = new ArrayList<>();

        mContext = context;
        mHistory = history;
        mIntent = intent;
        mCallerActivity = callerActivity;
        mLastChosen = lastChosen;
        mFilterLastUsed = filterLastUsed;

        rebuildList();
    }


    public void setPriorityItems(String... packageNames) {
        if (packageNames == null || packageNames.length == 0) {
            mPriorities = null;
            rebuildList();
            return;
        }

        int size = packageNames.length;
        mPriorities = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            // position 0 should have highest priority,
            // starting with 1 for lowest priority.
            mPriorities.put(packageNames[0], size - i + 1);
        }
        rebuildList();
    }

    public void handlePackagesChanged() {
        rebuildList();
        notifyDataSetChanged();
    }

    public DisplayResolveInfo getFilteredItem() {
        if (mFilterLastUsed && mLastChosenPosition >= 0) {
            // Not using getItem since it offsets to dodge this position for the list
            return mList.get(mLastChosenPosition);
        }
        return null;
    }

    public int getFilteredPosition() {
        if (mFilterLastUsed && mLastChosenPosition >= 0) {
            return mLastChosenPosition;
        }
        return AbsListView.INVALID_POSITION;
    }

    public boolean hasFilteredItem() {
        return mFilterLastUsed && mLastChosenPosition >= 0;
    }

    private void rebuildList() {

        mList.clear();
        List<ResolveInfo> currentResolveList = mOrigResolveList = mPm.queryIntentActivities(
                mIntent, PackageManager.MATCH_DEFAULT_ONLY
                        | (mFilterLastUsed ? PackageManager.GET_RESOLVED_FILTER : 0));
        int N;
        if ((currentResolveList != null) && ((N = currentResolveList.size()) > 0)) {
            // Only display the first matches that are either of equal
            // priority or have asked to be default options.
            ResolveInfo r0 = currentResolveList.get(0);
            for (int i = 1; i < N; i++) {
                ResolveInfo ri = currentResolveList.get(i);

                if (r0.priority != ri.priority ||
                        r0.isDefault != ri.isDefault) {
                    while (i < N) {
                        if (mOrigResolveList == currentResolveList) {
                            mOrigResolveList = new ArrayList<>(mOrigResolveList);
                        }
                        currentResolveList.remove(i);
                        N--;
                    }
                }
            }

            if (mCallerActivity != null) {
                for (ResolveInfo info : currentResolveList) {

                    if (info.activityInfo.packageName.equals(mCallerActivity.getPackageName())
                            && info.activityInfo.name.equals(mCallerActivity.getClassName())) {
                        currentResolveList.remove(info);
                        break;
                    }
                }
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

    private void processGroup(List<ResolveInfo> rList, int start, int end, ResolveInfo ro,
                              CharSequence roLabel) {
        // Process labels from start to i
        int num = end - start + 1;
        if (num == 1) {
            // No duplicate labels. Use label for entry at start
            addResolveInfo(new DisplayResolveInfo(ro, roLabel, null, null));
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
                            add.activityInfo.packageName, null));
                } else {
                    // Use package name for all entries from start to end-1
                    addResolveInfo(new DisplayResolveInfo(add, roLabel,
                            add.activityInfo.applicationInfo.loadLabel(mPm), null));
                }
                updateLastChosenPosition(add);
            }
        }
    }

    private void updateLastChosenPosition(ResolveInfo info) {
        if (mLastChosen != null
                && mLastChosen.activityInfo.packageName.equals(info.activityInfo.packageName)
                && mLastChosen.activityInfo.name.equals(info.activityInfo.name)) {
            mLastChosenPosition = mList.size() - 1;
        }
    }

    private void addResolveInfo(DisplayResolveInfo dri) {
        mList.add(dri);
    }

    public ResolveInfo resolveInfoForPosition(int position, boolean filtered) {
        return (filtered ? getItem(position) : mList.get(position)).ri;
    }

    public Intent intentForPosition(int position, boolean filtered) {
        DisplayResolveInfo dri = filtered ? getItem(position) : mList.get(position);
        return intentForDisplayResolveInfo(dri);
    }

    @Override
    public int getItemCount() {
        int result = mList.size();
        if (mFilterLastUsed && mLastChosenPosition >= 0) {
            result--;
        }
        result += getHeaderViewsCount();
        return result;
    }

    public DisplayResolveInfo getItem(int position) {
        position -= getHeaderViewsCount();
        if (position < 0) {
            throw new IllegalArgumentException(
                    String.format(
                            "Position: %d It cannot be negative or header cannot be selected.",
                            position
                    )
            );
        }
        if (mFilterLastUsed && mLastChosenPosition >= 0 && position >= mLastChosenPosition) {
            position++;
        }
        return mList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }


    public int getHeaderViewsCount() {
        return hasHeader() ? 1 : 0;
    }

    @Override
    protected ViewHolder onCreateHeaderViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater
                .inflate(R.layout.resolver_different_item_header, parent, false));
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
    public ViewHolder onCreateItemViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(mInflater
                .inflate(R.layout.resolve_list_item, viewGroup, false));
    }

    @Override
    public void onBindItemViewHolder(ViewHolder holder, final int position) {
        final DisplayResolveInfo info = getItem(position);

        holder.text.setText(info.displayLabel);
        if (mShowExtended) {
            holder.text2.setVisibility(View.VISIBLE);
            holder.text2.setText(info.extendedInfo);
        } else {
            holder.text2.setVisibility(View.GONE);
        }
        if (info.displayIcon == null) {
            new LoadIconTask().execute(info);
        }
        holder.icon.setImageDrawable(info.displayIcon);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSelectable()) {
                    v.setActivated(!v.isActivated());
                    setSelection(position);
                }
                if (mOnItemClickedListener != null) {
                    mOnItemClickedListener.onItemClicked(position);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return mOnItemLongClickedListener != null
                        && mOnItemLongClickedListener.onItemLongClicked(position);
            }
        });

        if (isSelectable()) {
            holder.itemView.setActivated(getCheckedItemPosition() == position);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView text;
        public TextView text2;
        public ImageView icon;

        public ViewHolder(View view) {
            super(view);
            text = (TextView) view.findViewById(R.id.text1);
            text2 = (TextView) view.findViewById(R.id.text2);
            icon = (ImageView) view.findViewById(R.id.icon);
        }
    }

    public interface OnItemClickedListener {
        void onItemClicked(int position);
    }

    public interface OnItemLongClickedListener {
        boolean onItemLongClicked(int position);
    }

    class LoadIconTask extends AsyncTask<DisplayResolveInfo, Void, DisplayResolveInfo> {
        @Override
        protected DisplayResolveInfo doInBackground(DisplayResolveInfo... params) {
            final DisplayResolveInfo info = params[0];
            if (info.displayIcon == null) {
                info.displayIcon = loadIconForResolveInfo(mPm, info.ri, mIconDpi);
            }
            return info;
        }

        @Override
        protected void onPostExecute(DisplayResolveInfo info) {
            notifyDataSetChanged();
        }
    }

    public static Drawable loadIconForResolveInfo(PackageManager mPm, ResolveInfo ri, int mIconDpi) {
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
            Log.e(TAG, "Couldn't find resources for package", e);
        }
        return ri.loadIcon(mPm);
    }

    public static Drawable getIcon(Resources res, int resId, int mIconDpi) {
        Drawable result;
        try {
            //noinspection deprecation
            result = res.getDrawableForDensity(resId, mIconDpi);
        } catch (Resources.NotFoundException e) {
            result = null;
        }

        return result;
    }

    class ResolverComparator implements Comparator<ResolveInfo> {
        private final Collator mCollator;
        private final boolean mHttp;

        public ResolverComparator(Context context, Intent intent) {
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

            if (mPriorities != null) {
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
            if (sa == null) sa = lhs.activityInfo.name;
            CharSequence sb = rhs.loadLabel(mPm);
            if (sb == null) sb = rhs.activityInfo.name;

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
            final Integer integer = mPriorities.get(lhs.activityInfo.packageName);
            return integer != null ? integer : 0;
        }
    }

    Intent intentForDisplayResolveInfo(DisplayResolveInfo dri) {
        Intent intent = new Intent(dri.origIntent != null ? dri.origIntent : mIntent);
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT
                | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        ActivityInfo ai = dri.ri.activityInfo;
        intent.setComponent(new ComponentName(
                ai.applicationInfo.packageName, ai.name));
        return intent;
    }

    static boolean isSpecificUriMatch(int match) {
        match = match & IntentFilter.MATCH_CATEGORY_MASK;
        return match >= IntentFilter.MATCH_CATEGORY_HOST
                && match <= IntentFilter.MATCH_CATEGORY_PATH;
    }

    public static class Header {}

    public int getCheckedItemPosition() {
        return checkedItemPosition;
    }

    public void setSelection(int position) {
        checkedItemPosition = position;
        notifyDataSetChanged();
    }

    public void setSelectable(boolean selectable) {
        mIsSelectable = selectable;
        notifyDataSetChanged();
    }

    public boolean isSelectable() {
        return mIsSelectable;
    }
}