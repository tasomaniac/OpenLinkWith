package com.tasomaniac.openwith.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("MaxLineLength")
object Migration1to2 : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) = database.run {
        execSQL("CREATE TABLE `openwith_backup` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `host` TEXT NOT NULL, `component` TEXT NOT NULL, `preferred` INTEGER NOT NULL)")
        execSQL("INSERT INTO openwith_backup SELECT _id, host, component, preferred FROM openwith WHERE preferred = 1")
        execSQL("DROP TABLE openwith")
        execSQL("ALTER TABLE openwith_backup RENAME TO openwith")
        execSQL("CREATE UNIQUE INDEX `index_openwith_host` ON `openwith` (`host`)")
    }

}
