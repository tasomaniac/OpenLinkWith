package com.tasomaniac.openwith.homescreen;

import android.annotation.TargetApi;
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
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.tasomaniac.android.widget.DelayedProgressBar;
import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.data.Injector;
import com.tasomaniac.openwith.resolver.DisplayResolveInfo;
import com.tasomaniac.openwith.util.Intents;

import javax.inject.Inject;

import butterknife.BindBitmap;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import okhttp3.OkHttpClient;
import timber.log.Timber;

import static android.os.Build.VERSION_CODES.M;

@TargetApi(M)
public class AddToHomeScreenDialogFragment extends AppCompatDialogFragment
        implements DialogInterface.OnClickListener {

    private static final String KEY_DRI = "dri";
    private static final String KEY_INTENT = "intent";

    @Inject OkHttpClient client;

    @BindView(R.id.add_to_home_screen_title) EditText titleView;
    @BindView(R.id.add_to_home_screen_progress) DelayedProgressBar progressBar;
    @BindBitmap(R.drawable.ic_star_mark) Bitmap shortcutMark;

    private DisplayResolveInfo dri;
    private ShortcutIconCreator shortcutIconCreator;
    private Intent intent;
    private TitleFetcher titleFetcher;

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
        Injector.obtain(getContext()).inject(this);
        dri = getArguments().getParcelable(KEY_DRI);
        intent = getArguments().getParcelable(KEY_INTENT);
        titleFetcher = new TitleFetcher(client);
        titleFetcher.setListener(new TitleFetcher.Listener() {
            @Override
            public void onFailure() {
                hideProgressBar();
            }

            @Override
            public void onSuccess(final String title) {
                hideProgressBar();
                if (!TextUtils.isEmpty(titleView.getText())) {
                    return;
                }
                titleView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (title != null) {
                            titleView.append(title);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        onTitleChanged(titleView.getText());
        shortcutIconCreator = new ShortcutIconCreator(shortcutMark);

        if (TextUtils.isEmpty(titleView.getText())) {
            showProgressBar();
            titleFetcher.fetch(intent.getDataString());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        titleFetcher.setListener(null);
    }

    private void showProgressBar() {
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                progressBar.show();
            }
        });
    }

    private void hideProgressBar() {
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                progressBar.hide(true);
            }
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_add_to_home_screen, (ViewGroup) getView(), false);
        ButterKnife.bind(this, view);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.add, this)
                .setNegativeButton(R.string.cancel, null)
                .setView(view)
                .setTitle(R.string.add_to_homescreen)
                .create();
        forceKeyboardVisible(dialog.getWindow());
        return dialog;
    }

    private static void forceKeyboardVisible(Window window) {
        window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        );
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        createShortcutFor(dri);
        Intents.launchHomeScreen(getActivity());
    }

    @OnEditorAction(R.id.add_to_home_screen_title)
    boolean onKeyboardDoneClicked(int actionId) {
        if (EditorInfo.IME_ACTION_GO == actionId) {
            createShortcutFor(dri);
            Intents.launchHomeScreen(getActivity());
            return true;
        }
        return false;
    }

    @OnTextChanged(R.id.add_to_home_screen_title)
    void onTitleChanged(CharSequence title) {

        AlertDialog dialog = (AlertDialog) getDialog();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setEnabled(!TextUtils.isEmpty(title));
    }

    private void createShortcutFor(DisplayResolveInfo dri) {
        try {
            Intent shortcutIntent = createShortcutIntent();
            shortcutIntent.putExtra(
                    Intent.EXTRA_SHORTCUT_ICON,
                    shortcutIconCreator.createShortcutIconFor((BitmapDrawable) dri.displayIcon())
            );
            getActivity().sendBroadcast(Intents.fixIntents(getContext(), shortcutIntent));
        } catch (Exception e) {
            // This method started to fire android.os.TransactionTooLargeException
            Timber.e(e, "Exception while adding shortcut");
            createShortcutWithoutIcon();
        }
    }

    private void createShortcutWithoutIcon() {
        try {
            Intent shortcutIntent = createShortcutIntent();
            Intent.ShortcutIconResource icon = Intent.ShortcutIconResource.fromContext(getContext(), R.mipmap.ic_bookmark);
            shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
            getActivity().sendBroadcast(Intents.fixIntents(getContext(), shortcutIntent));
        } catch (Exception e) {
            Timber.e(e, "Exception while adding shortcut without icon");
        }
    }

    private Intent createShortcutIntent() {
        Intent shortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, titleView.getText().toString());
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        shortcutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        return shortcutIntent;
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, "AddToHomeScreen");
    }
}
