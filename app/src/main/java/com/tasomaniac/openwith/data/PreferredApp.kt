package com.tasomaniac.openwith.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "openwith",
    indices = [Index("host", unique = true)]
)
data class PreferredApp(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") val id: Int,
    val host: String,
    val component: String,
    /*@ColumnInfo(typeAffinity = BLOB) */val preferred: Boolean,
    /*@ColumnInfo(typeAffinity = BLOB) */val last_chosen: Boolean
)
