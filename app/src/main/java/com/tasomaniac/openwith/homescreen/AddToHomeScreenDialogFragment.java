package com.tasomaniac.openwith.homescreen;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.pm.ShortcutInfoCompat;
import android.support.v4.content.pm.ShortcutManagerCompat;
import android.support.v4.graphics.drawable.IconCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.*;
import com.tasomaniac.android.widget.DelayedProgressBar;
import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.resolver.DisplayActivityInfo;
import com.tasomaniac.openwith.util.Intents;
import dagger.android.support.AndroidSupportInjection;
import timber.log.Timber;

import javax.inject.Inject;

import static android.os.Build.VERSION_CODES.M;

@TargetApi(M)
public class AddToHomeScreenDialogFragment extends AppCompatDialogFragment
        implements DialogInterface.OnClickListener {

    private static final String KEY_ACTIVITY_TO_ADD = "activity_to_add";
    private static final String KEY_INTENT = "intent";

    @Inject TitleFetcher titleFetcher;

    @BindView(R.id.add_to_home_screen_title) EditText titleView;
    @BindView(R.id.add_to_home_screen_progress) DelayedProgressBar progressBar;
    @BindBitmap(R.drawable.ic_bookmark) Bitmap shortcutMark;

    private DisplayActivityInfo activityToAdd;
    private ShortcutIconCreator shortcutIconCreator;
    private Intent intent;

    public static AddToHomeScreenDialogFragment newInstance(DisplayActivityInfo activity, Intent intent) {
        AddToHomeScreenDialogFragment fragment = new AddToHomeScreenDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_ACTIVITY_TO_ADD, activity);
        args.putParcelable(KEY_INTENT, intent);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSupportInjection.inject(this);
        activityToAdd = getArguments().getParcelable(KEY_ACTIVITY_TO_ADD);
        intent = getArguments().getParcelable(KEY_INTENT);
        titleFetcher.setListener(new TitleFetcher.Listener() {
            @Override
            public void onFinished() {
                hideProgressBar();
            }

            @Override
            public void onSuccess(final String title) {
                if (!TextUtils.isEmpty(titleView.getText())) {
                    return;
                }
                titleView.post(() -> {
                    if (title != null) {
                        titleView.append(title);
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
        progressBar.post(() -> progressBar.show());
    }

    private void hideProgressBar() {
        progressBar.post(() -> progressBar.hide(true));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        @SuppressLint("InflateParams")
        View view = requireActivity().getLayoutInflater()
                .inflate(R.layout.dialog_add_to_home_screen, null);
        ButterKnife.bind(this, view);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setPositiveButton(R.string.add, this)
                .setNegativeButton(R.string.cancel, null)
                .setView(view)
                .setTitle(R.string.add_to_homescreen)
                .create();
        forceKeyboardVisible(dialog.getWindow());
        return dialog;
    }

    private static void forceKeyboardVisible(Window window) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        createShortcutAndHandleError();
    }

    @OnEditorAction(R.id.add_to_home_screen_title)
    boolean onKeyboardDoneClicked(int actionId) {
        if (EditorInfo.IME_ACTION_GO == actionId) {
            createShortcutAndHandleError();
            return true;
        }
        return false;
    }

    @OnTextChanged(R.id.add_to_home_screen_title)
    void onTitleChanged(CharSequence title) {
        getPositiveButton().setEnabled(!TextUtils.isEmpty(title));
    }

    private Button getPositiveButton() {
        AlertDialog dialog = (AlertDialog) getDialog();
        return dialog.getButton(DialogInterface.BUTTON_POSITIVE);
    }

    private void createShortcutAndHandleError() {
        boolean success = createShortcut();
        if (!success) {
            Toast.makeText(getContext(), R.string.add_to_home_screen_error, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean createShortcut() {
        String id = intent.getDataString() + activityToAdd.packageName();
        String label = titleView.getText().toString();
        try {
            return createShortcutWith(id, label, shortcutIconCreator.createIconFor(activityToAdd.displayIcon()));
        } catch (Exception e) {
            // This method started to fire android.os.TransactionTooLargeException
            Timber.e(e, "Exception while adding shortcut");
            return createShortcutWith(id, label, createSimpleIcon());
        }
    }

    private boolean createShortcutWith(String id, String label, IconCompat icon) {
        ShortcutInfoCompat shortcut = new ShortcutInfoCompat.Builder(requireContext(), id)
                .setIntent(intent)
                .setShortLabel(label)
                .setIcon(icon)
                .build();
        return ShortcutManagerCompat.requestPinShortcut(requireContext(), shortcut, startHomeScreen());
    }

    private IconCompat createSimpleIcon() {
        return IconCompat.createWithResource(requireContext(), R.mipmap.ic_launcher_bookmark);
    }

    private IntentSender startHomeScreen() {
        return PendingIntent.getActivity(requireContext(), 0, Intents.homeScreenIntent(), 0).getIntentSender();
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, "AddToHomeScreen");
    }
}
