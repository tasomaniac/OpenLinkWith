package com.tasomaniac.openwith.resolver;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.tasomaniac.openwith.util.ActivityInfoExtensionsKt;

public final class DisplayActivityInfo implements Parcelable {
    private final ActivityInfo activityInfo;
    private final CharSequence displayLabel;
    @Nullable private final CharSequence extendedInfo;
    private Drawable displayIcon;

    public DisplayActivityInfo(ActivityInfo activityInfo, CharSequence displayLabel, @Nullable CharSequence extendedInfo) {
        this.activityInfo = activityInfo;
        this.displayLabel = displayLabel;
        this.extendedInfo = extendedInfo;
    }

    private DisplayActivityInfo(Parcel in) {
        activityInfo = in.readParcelable(ActivityInfo.class.getClassLoader());
        displayLabel = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        extendedInfo = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
    }

    public String packageName() {
        return activityInfo.packageName;
    }

    Intent intentFrom(Intent sourceIntent) {
        return new Intent(sourceIntent)
                .setComponent(componentName())
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
        DisplayActivityInfo that = (DisplayActivityInfo) o;
        return componentName().equals(that.componentName());
    }

    @Override
    public int hashCode() {
        return componentName().hashCode();
    }

    private ComponentName componentName() {
        return ActivityInfoExtensionsKt.componentName(activityInfo);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(activityInfo, flags);
        TextUtils.writeToParcel(displayLabel, dest, flags);
        TextUtils.writeToParcel(extendedInfo, dest, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DisplayActivityInfo> CREATOR = new Creator<DisplayActivityInfo>() {
        @Override
        public DisplayActivityInfo createFromParcel(Parcel in) {
            return new DisplayActivityInfo(in);
        }

        @Override
        public DisplayActivityInfo[] newArray(int size) {
            return new DisplayActivityInfo[size];
        }
    };

    public ActivityInfo getActivityInfo() {
        return activityInfo;
    }

    public CharSequence displayLabel() {
        return displayLabel;
    }

    @Nullable public CharSequence extendedInfo() {
        return extendedInfo;
    }

    public Drawable displayIcon() {
        return displayIcon;
    }

    void displayIcon(Drawable displayIcon) {
        this.displayIcon = displayIcon;
    }
}
