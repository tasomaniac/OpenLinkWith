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

    static AppRemoveDialogFragment newInstance(DisplayResolveInfo info) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_INFO, info);
        AppRemoveDialogFragment fragment = new AppRemoveDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private Callbacks callbacks = Callbacks.EMPTY;

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
        if (info == null) {
            throw new IllegalArgumentException("Use newInstance method to create the Dialog");
        }

        final String message = getString(
                R.string.message_remove_preferred,
                info.displayLabel(),
                info.extendedInfo(),
                info.extendedInfo()
        );

        //noinspection deprecation
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_remove_preferred)
                .setMessage(Html.fromHtml(message))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callbacks.onAppRemoved(info);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    interface Callbacks {
        void onAppRemoved(DisplayResolveInfo info);

        Callbacks EMPTY = new Callbacks() {
            @Override
            public void onAppRemoved(DisplayResolveInfo info) {
            }
        };
    }
}
