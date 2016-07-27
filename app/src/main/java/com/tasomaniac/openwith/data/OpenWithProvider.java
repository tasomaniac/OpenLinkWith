package com.tasomaniac.openwith.data;

import android.net.Uri;

import com.tasomaniac.openwith.BuildConfig;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.HOST;
import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.ID;
import static com.tasomaniac.openwith.data.OpenWithDatabase.OpenWithColumns.PREFERRED;

@ContentProvider(authority = OpenWithProvider.AUTHORITY,
        database = OpenWithDatabase.class,
        packageName = "com.tasomaniac.openwith.provider")
public final class OpenWithProvider {

    private OpenWithProvider() {
    }

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID;

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    @TableEndpoint(table = OpenWithDatabase.OPENWITH)
    public static class OpenWithHosts {

        @ContentUri(
                path = OpenWithDatabase.OPENWITH,
                type = "vnd.android.cursor.dir/openwith")
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI,
                OpenWithDatabase.OPENWITH);

        @InexactContentUri(
                name = "OPENWITH_ID",
                path = OpenWithDatabase.OPENWITH + "/#",
                type = "vnd.android.cursor.item/openwith",
                whereColumn = ID,
                pathSegment = 1)
        public static Uri withId(long id) {
            return Uri.withAppendedPath(CONTENT_URI, String.valueOf(id));
        }

        @InexactContentUri(
                name = "OPENWITH_HOST",
                path = OpenWithDatabase.OPENWITH + "/host/" + "*",
                type = "vnd.android.cursor.item/openwith",
                whereColumn = HOST,
                pathSegment = 2)
        public static Uri withHost(String host) {
            return CONTENT_URI.buildUpon()
                    .appendEncodedPath("host")
                    .appendPath(host).build();
        }


        @ContentUri(
                path = OpenWithDatabase.OPENWITH + "/preferred",
                type = "vnd.android.cursor.dir/openwith",
                where = PREFERRED + "=1")
        public static final Uri CONTENT_URI_PREFERRED = Uri.withAppendedPath(CONTENT_URI, "preferred");
    }
}
