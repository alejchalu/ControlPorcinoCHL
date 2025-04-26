package com.control.porcinochl

import androidx.room.TypeConverter
import java.util.Date

/**
 * Conversores personalizados para almacenar tipos no primitivos (como Date) en Room.
 */
class Converters {

    @TypeConverter
    fun fromDate(date: Date?): Long? = date?.time

    @TypeConverter
    fun toDate(timestamp: Long?): Date? = timestamp?.let { Date(it) }
}
