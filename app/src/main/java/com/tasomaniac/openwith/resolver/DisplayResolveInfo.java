package com.tasomaniac.openwith.resolver;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.tasomaniac.openwith.util.ResolverInfos;

public final class DisplayResolveInfo implements Parcelable {
    private final ResolveInfo ri;
    private final CharSequence displayLabel;
    private final CharSequence extendedInfo;
    private Drawable displayIcon;

    public DisplayResolveInfo(ResolveInfo ri, CharSequence displayLabel, CharSequence extendedInfo) {
        this.ri = ri;
        this.displayLabel = displayLabel;
        this.extendedInfo = extendedInfo;
    }

    private DisplayResolveInfo(Parcel in) {
        ri = in.readParcelable(ResolveInfo.class.getClassLoader());
        displayLabel = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        extendedInfo = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
    }

    Intent intentFrom(Intent sourceIntent) {
        return new Intent(sourceIntent)
                .setComponent(ResolverInfos.componentName(ri))
                .addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT
                                  | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DisplayResolveInfo that = (DisplayResolveInfo) o;
        return ResolverInfos.equals(ri, that.ri);
    }

    @Override
    public int hashCode() {
        return ResolverInfos.componentName(ri).hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(ri, flags);
        TextUtils.writeToParcel(displayLabel, dest, flags);
        TextUtils.writeToParcel(extendedInfo, dest, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DisplayResolveInfo> CREATOR = new Creator<DisplayResolveInfo>() {
        @Override
        public DisplayResolveInfo createFromParcel(Parcel in) {
            return new DisplayResolveInfo(in);
        }

        @Override
        public DisplayResolveInfo[] newArray(int size) {
            return new DisplayResolveInfo[size];
        }
    };

    public ResolveInfo resolveInfo() {
        return ri;
    }

    public CharSequence displayLabel() {
        return displayLabel;
    }

    public CharSequence extendedInfo() {
        return extendedInfo;
    }

    public Drawable displayIcon() {
        return displayIcon;
    }

    void displayIcon(Drawable displayIcon) {
        this.displayIcon = displayIcon;
    }
}
