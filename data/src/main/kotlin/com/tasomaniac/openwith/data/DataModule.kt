package com.tasomaniac.openwith.data

import android.app.Application
import androidx.room.Room
import com.tasomaniac.openwith.data.migrations.Migration1to2
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DataModule {

    @Singleton
    @Provides
    fun room(app: Application): Database =
        Room.databaseBuilder(app, Database::class.java, "openWithDatabase.db")
            .addMigrations(Migration1to2)
            .build()

    @Provides
    fun preferredAppDao(database: Database) = database.preferredAppDao()
}
