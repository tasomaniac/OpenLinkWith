package com.tasomaniac.openwith.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PreferredApp::class], version = 2, exportSchema = false)
abstract class Database : RoomDatabase() {

    abstract fun preferredAppDao(): PreferredAppDao

}
