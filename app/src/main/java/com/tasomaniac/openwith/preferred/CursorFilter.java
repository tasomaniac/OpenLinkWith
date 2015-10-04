package com.tasomaniac.openwith.preferred;

import android.database.Cursor;
import android.widget.Filter;

/**
 * <p>The CursorFilter delegates most of the work to the CursorAdapter.
 * Subclasses should override these delegate methods to run the queries
 * and convert the results into String that can be used by auto-completion
 * widgets.</p>
 */
public class CursorFilter extends Filter {

    CursorFilterClient mClient;

    interface CursorFilterClient {
        CharSequence convertToString(Cursor cursor);

        Cursor runQueryOnBackgroundThread(CharSequence constraint);

        Cursor getCursor();

        void changeCursor(Cursor cursor);
    }

    CursorFilter(CursorFilterClient client) {
        mClient = client;
    }

    @Override
    public CharSequence convertResultToString(Object resultValue) {
        return mClient.convertToString((Cursor) resultValue);
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        Cursor cursor = mClient.runQueryOnBackgroundThread(constraint);

        FilterResults results = new FilterResults();
        if (cursor != null) {
            results.count = cursor.getCount();
            results.values = cursor;
        } else {
            results.count = 0;
            results.values = null;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        Cursor oldCursor = mClient.getCursor();

        if (results.values != null && results.values != oldCursor) {
            mClient.changeCursor((Cursor) results.values);
        }
    }
}
