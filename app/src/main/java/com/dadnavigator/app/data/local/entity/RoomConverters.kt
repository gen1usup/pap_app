package com.dadnavigator.app.data.local.entity

import androidx.room.TypeConverter
import java.time.Instant

/**
 * Shared Room converters for time fields.
 */
class RoomConverters {

    @TypeConverter
    fun instantToEpochMillis(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun epochMillisToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)
}
