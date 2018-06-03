package com.tasomaniac.openwith.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import io.reactivex.Flowable
import io.reactivex.Maybe

@Dao
interface PreferredAppDao {

    @Query("SELECT * FROM openwith WHERE preferred = 1")
    fun allPreferredApps(): Flowable<List<PreferredApp>>

    @Query("SELECT * FROM openwith WHERE host = :host")
    fun preferredAppByHost(host: String): Maybe<PreferredApp>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(preferredApp: PreferredApp)

    @Query("DELETE FROM openwith WHERE host = :host")
    fun deleteHost(host: String)
}
