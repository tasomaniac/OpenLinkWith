package com.tasomaniac.openwith.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.content.ComponentName

@Entity(
    tableName = "openwith",
    indices = [Index("host", unique = true)]
)
data class PreferredApp(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") val id: Int = 0,
    val host: String,
    val component: String,
    val preferred: Boolean,
    val last_chosen: Boolean
) {

    val componentName: ComponentName
        @Ignore get() = ComponentName.unflattenFromString(component)

}
