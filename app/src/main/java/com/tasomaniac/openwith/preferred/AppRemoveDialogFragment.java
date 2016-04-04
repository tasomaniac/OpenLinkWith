package com.tasomaniac.openwith.preferred;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.Html;

import com.tasomaniac.openwith.R;
import com.tasomaniac.openwith.resolver.DisplayResolveInfo;

public class AppRemoveDialogFragment extends AppCompatDialogFragment {

    private static final String EXTRA_INFO = "EXTRA_INFO";
    private static final String EXTRA_POSITION = "EXTRA_POSITION";

    public static AppRemoveDialogFragment newInstance(DisplayResolveInfo info, int position) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_INFO, info);
        args.putInt(EXTRA_POSITION, position);
        AppRemoveDialogFragment fragment = new AppRemoveDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    Callbacks callbacks = Callbacks.EMPTY;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Callbacks) {
            callbacks = (Callbacks) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = Callbacks.EMPTY;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final DisplayResolveInfo info = getArguments().getParcelable(EXTRA_INFO);
        final int position = getArguments().getInt(EXTRA_POSITION);

        final String message = getString(
                R.string.message_remove_preferred,
                info.getDisplayLabel(),
                info.getExtendedInfo(),
                info.getExtendedInfo()
        );

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_remove_preferred)
                .setMessage(Html.fromHtml(message))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callbacks.onAppRemoved(info, position);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    public interface Callbacks {
        void onAppRemoved(DisplayResolveInfo info, int position);

        Callbacks EMPTY = new Callbacks() {
            @Override
            public void onAppRemoved(DisplayResolveInfo info, int position) {
            }
        };
    }
}
