package com.example.mayank.nfcapp.roomdatabase

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import com.example.mayank.nfcapp.dao.BusLocationDao
import com.example.mayank.nfcapp.dao.NfcLocationDao

/**
 * Created by Mayank on 15/03/2018.
 */

@Database(entities = [(NfcLocations::class), (BusLocations::class)], version = 1)
@TypeConverters(Converters::class)
abstract class NfcDatabase : RoomDatabase() {

    abstract fun nfcLocationDao() : NfcLocationDao
    abstract fun busLocationDao(): BusLocationDao

}