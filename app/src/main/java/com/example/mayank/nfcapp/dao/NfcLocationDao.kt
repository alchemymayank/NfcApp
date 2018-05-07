package com.example.mayank.nfcapp.dao

import android.arch.persistence.room.*
import com.example.mayank.nfcapp.roomdatabase.NfcLocations

/**
 * Created by Mayank on 15/03/2018.
 */
@Dao
interface NfcLocationDao {

    @Insert
    fun insert(location: NfcLocations)

    @Query("SELECT * FROM nfc_locations")
    fun getAllLocation(): List<NfcLocations>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(location: List<NfcLocations>?)

    @Update
    fun updateLocation(location: NfcLocations)
}