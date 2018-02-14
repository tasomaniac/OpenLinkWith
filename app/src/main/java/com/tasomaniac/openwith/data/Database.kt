package com.tasomaniac.devwidget.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.tasomaniac.openwith.data.PreferredApp
import com.tasomaniac.openwith.data.PreferredAppDao

@Database(entities = [PreferredApp::class], version = 1, exportSchema = false)
abstract class Database : RoomDatabase() {

  abstract fun preferredAppDao(): PreferredAppDao

}
