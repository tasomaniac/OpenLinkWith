package com.tasomaniac.openwith.settings;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.webkit.WebView;

import com.tasomaniac.openwith.R;

public class LicensesDialogFragment extends DialogFragment {

    public static LicensesDialogFragment newInstance() {
        return new LicensesDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        @SuppressLint("InflateParams")
        WebView view = (WebView) LayoutInflater.from(getActivity()).inflate(R.layout.dialog_licenses, null);
        view.loadUrl("file:///android_asset/open_source_licenses.html");

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.pref_title_open_source)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }

}