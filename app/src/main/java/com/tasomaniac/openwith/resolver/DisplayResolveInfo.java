package com.tasomaniac.openwith.resolver;

import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public final class DisplayResolveInfo implements Parcelable {
    private final int id;
    final ResolveInfo ri;
    private final CharSequence displayLabel;
    private final CharSequence extendedInfo;
    private Drawable displayIcon;

    DisplayResolveInfo(ResolveInfo pri, CharSequence displayLabel, CharSequence extendedInfo) {
        this(0, pri, displayLabel, extendedInfo);
    }

    public DisplayResolveInfo(int id, ResolveInfo ri, CharSequence displayLabel, CharSequence extendedInfo) {
        this.id = id;
        this.ri = ri;
        this.displayLabel = displayLabel;
        this.extendedInfo = extendedInfo;
    }

    private DisplayResolveInfo(Parcel in) {
        id = in.readInt();
        ri = in.readParcelable(ResolveInfo.class.getClassLoader());
        displayLabel = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        extendedInfo = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
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

    public int id() {
        return id;
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
