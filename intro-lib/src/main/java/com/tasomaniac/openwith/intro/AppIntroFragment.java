package com.tasomaniac.openwith.intro;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.tasomaniac.openwith.intro.lib.R;

public class AppIntroFragment extends Fragment {

    private int drawable, backgroundColor, titleColor, descColor;
    private int title, description;

    public AppIntroFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().size() != 0) {
            drawable = getArguments().getInt(Builder.ARG_DRAWABLE);
            title = getArguments().getInt(Builder.ARG_TITLE);
            description = getArguments().getInt(Builder.ARG_DESC);
            backgroundColor = getArguments().getInt(Builder.ARG_BG_COLOR);
            titleColor = getArguments().getInt(Builder.ARG_TITLE_COLOR);
            descColor = getArguments().getInt(Builder.ARG_DESC_COLOR);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_intro, container, false);
        TextView t = view.findViewById(R.id.title);
        TextView d = view.findViewById(R.id.description);
        ImageView i = view.findViewById(R.id.image);
        LinearLayout m = view.findViewById(R.id.main);

        if (title != 0) {
            t.setText(Html.fromHtml(getString(title)));
        }
        if (titleColor != 0) {
            t.setTextColor(titleColor);
        }
        if (description != 0) {
            d.setText(Html.fromHtml(getString(description)));
        }
        if (descColor != 0) {
            d.setTextColor(descColor);
        }
        if (drawable != 0) {
            i.setImageDrawable(ContextCompat.getDrawable(requireActivity(), drawable));
        }
        if (backgroundColor != 0) {
            m.setBackgroundColor(backgroundColor);
        }
        return view;
    }

    public static class Builder {

        static final String ARG_TITLE = "title";
        static final String ARG_DESC = "desc";
        static final String ARG_DRAWABLE = "drawable";
        static final String ARG_BG_COLOR = "bg_color";
        static final String ARG_TITLE_COLOR = "title_color";
        static final String ARG_DESC_COLOR = "desc_color";

        int drawable;
        int backgroundColor;
        int titleColor;
        int descriptionColor;
        int title;
        int description;

        public Builder drawable(@DrawableRes int drawable) {
            this.drawable = drawable;
            return this;
        }

        public Builder backgroundColor(@ColorInt int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder titleColor(@ColorInt int titleColor) {
            this.titleColor = titleColor;
            return this;
        }

        public Builder descriptionColor(@ColorInt int descriptionColor) {
            this.descriptionColor = descriptionColor;
            return this;
        }

        public Builder title(@StringRes int title) {
            this.title = title;
            return this;
        }

        public Builder description(@StringRes int description) {
            this.description = description;
            return this;
        }

        public AppIntroFragment build() {

            AppIntroFragment fragment = new AppIntroFragment();

            Bundle args = new Bundle();
            args.putInt(ARG_TITLE, title);
            args.putInt(ARG_DESC, description);
            args.putInt(ARG_DRAWABLE, drawable);
            args.putInt(ARG_BG_COLOR, backgroundColor);
            args.putInt(ARG_TITLE_COLOR, titleColor);
            args.putInt(ARG_DESC_COLOR, descriptionColor);
            fragment.setArguments(args);

            return fragment;
        }
    }
}
