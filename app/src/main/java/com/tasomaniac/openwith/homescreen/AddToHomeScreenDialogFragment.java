package com.tasomaniac.openwith.homescreen;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.resolver.DisplayResolveInfo;
import com.tasomaniac.openwith.util.Intents;

import butterknife.BindBitmap;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;

public class AddToHomeScreenDialogFragment extends AppCompatDialogFragment
        implements DialogInterface.OnClickListener {

    private static final String KEY_DRI = "dri";
    private static final String KEY_INTENT = "intent";

    @BindBitmap(R.drawable.ic_star_mark)
    Bitmap shortcutMark;

    private DisplayResolveInfo dri;
    private ShortcutIconCreator shortcutIconCreator;
    private Intent intent;
    private String title;

    public static AddToHomeScreenDialogFragment newInstance(DisplayResolveInfo dri, Intent intent) {
        AddToHomeScreenDialogFragment fragment = new AddToHomeScreenDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_DRI, dri);
        args.putParcelable(KEY_INTENT, intent);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dri = getArguments().getParcelable(KEY_DRI);
        intent = getArguments().getParcelable(KEY_INTENT);
    }

    @Override
    public void onStart() {
        super.onStart();
        onTitleChanged("");
        shortcutIconCreator = new ShortcutIconCreator(shortcutMark);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_add_to_home_screen, (ViewGroup) getView(), false);
        ButterKnife.bind(this, view);

        return new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.add, this)
                .setNegativeButton(R.string.cancel, null)
                .setView(view)
                .setTitle(R.string.add_to_homescreen)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        createShortcutFor(dri);
        Intents.launchHomeScreen(getActivity());
    }

    @OnTextChanged(R.id.add_to_home_screen_title)
    void onTitleChanged(CharSequence title) {
        this.title = title.toString();

        AlertDialog dialog = (AlertDialog) getDialog();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setEnabled(!TextUtils.isEmpty(title));
    }

    private void createShortcutFor(DisplayResolveInfo dri) {
        Intent shortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
        shortcutIntent.putExtra(
                Intent.EXTRA_SHORTCUT_ICON,
                shortcutIconCreator.createShortcutIconFor((BitmapDrawable) dri.getDisplayIcon())
        );
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        shortcutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

        getActivity().sendBroadcast(shortcutIntent);
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, "AddToHomeScreen");
    }
}
