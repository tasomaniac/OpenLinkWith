package com.tasomaniac.openwith.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = [PreferredApp::class], version = 2, exportSchema = false)
abstract class Database : RoomDatabase() {

  abstract fun preferredAppDao(): PreferredAppDao

}
