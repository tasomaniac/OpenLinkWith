package com.tasomaniac.devwidget.data

import android.app.Application
import android.arch.persistence.room.Room
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DataModule {

    @Singleton
    @Provides
    @JvmStatic
    fun room(app: Application): Database =
        Room.databaseBuilder(app, Database::class.java, "openWithDatabase.db").build()

}
