package com.example.mayank.nfcapp.roomdatabase

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by Mayank on 15/03/2018.
 */
@Entity(tableName = "nfc_locations")
class NfcLocations {

    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @ColumnInfo(name = "nfc_id")
    var nfcId: String? = null

    @ColumnInfo(name = "latitude")
    var latitude: String? = null

    @ColumnInfo(name = "longitude")
    var longitude: String? = null

    @ColumnInfo(name = "location_time")
    var trackTime: Long? = null

    @ColumnInfo(name = "sync_state")
    var syncState: Byte? = null
}
