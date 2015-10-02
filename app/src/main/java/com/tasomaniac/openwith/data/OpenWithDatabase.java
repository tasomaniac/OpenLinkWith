package com.tasomaniac.openwith.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.ConflictResolutionType;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.Table;
import net.simonvt.schematic.annotation.Unique;

import static net.simonvt.schematic.annotation.DataType.Type.BLOB;
import static net.simonvt.schematic.annotation.DataType.Type.INTEGER;
import static net.simonvt.schematic.annotation.DataType.Type.TEXT;

@Database(version = OpenWithDatabase.VERSION,
        packageName = "com.tasomaniac.openwith.provider")
public final class OpenWithDatabase {

    private OpenWithDatabase() {
    }

    public static final int VERSION = 1;

    @Table(OpenWithColumns.class)
    public static final String OPENWITH = "openwith";

    public interface OpenWithColumns {

        @DataType(INTEGER) @PrimaryKey @AutoIncrement String ID = "_id";

        @DataType(TEXT) @NotNull @Unique(onConflict = ConflictResolutionType.REPLACE) String HOST = "host";

        @DataType(TEXT) @NotNull String COMPONENT = "component";

        @DataType(BLOB) String PREFERRED = "preferred";

        @DataType(BLOB) String LAST_CHOSEN = "last_chosen";
    }
}