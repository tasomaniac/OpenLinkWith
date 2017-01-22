package com.tasomaniac.openwith.settings;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

import com.tasomaniac.openwith.R;

public class LicensesDialogFragment extends AppCompatDialogFragment {

    public static LicensesDialogFragment newInstance() {
        return new LicensesDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_licenses, null);

        WebView licenses = (WebView) view.findViewById(R.id.licenses);
        licenses.loadUrl("file:///android_asset/open_source_licenses.html");

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.pref_title_open_source)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }

}
