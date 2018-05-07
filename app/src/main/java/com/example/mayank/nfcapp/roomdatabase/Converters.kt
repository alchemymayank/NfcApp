package com.example.mayank.nfcapp.roomdatabase

import android.arch.persistence.room.TypeConverter
import java.util.*

/**
 * Created by Mayank on 15/03/2018.
 */
object Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date): Long? {
        return (if (date == null) null else date.time)?.toLong()
    }
}