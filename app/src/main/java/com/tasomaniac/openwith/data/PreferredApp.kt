package com.tasomaniac.openwith.data

import android.arch.persistence.room.*
import android.arch.persistence.room.ColumnInfo.BLOB

@Entity(tableName = "openwith",
    indices = [Index("host", unique = true)]
)
@TypeConverters(BooleanConverter::class)
data class PreferredApp(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") val id: Int = 0,
    val host: String,
    val component: String,
    @ColumnInfo(typeAffinity = BLOB) val preferred: Boolean,
    @ColumnInfo(typeAffinity = BLOB) val last_chosen: Boolean
)

object BooleanConverter {

  @TypeConverter
  @JvmStatic
  fun toBlob(value: Boolean): ByteArray = ByteArray(1) {
    if (value) 1.toByte() else 0.toByte()
  }

  @TypeConverter
  @JvmStatic
  fun toBoolean(value: ByteArray): Boolean = value[0].toInt() == 1


}
