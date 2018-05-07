package com.example.mayank.nfcapp.dao

import android.arch.persistence.room.*
import com.example.mayank.nfcapp.roomdatabase.BusLocations
import com.example.mayank.nfcapp.roomdatabase.NfcLocations

/**
 * Created by Mayank on 16/03/2018.
 */
@Dao
interface BusLocationDao {

    @Insert
    fun insert(location: BusLocations)

    @Query("SELECT * FROM bus_locations")
    fun getAllLocation(): List<BusLocations>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(location: List<BusLocations>?)

    @Update
    fun updateLocation(location: BusLocations)
}