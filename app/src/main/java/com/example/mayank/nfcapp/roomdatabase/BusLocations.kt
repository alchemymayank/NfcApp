package com.example.mayank.nfcapp.roomdatabase

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by Mayank on 16/03/2018.
 */
@Entity(tableName = "bus_locations")
class BusLocations {
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0

    @ColumnInfo(name = "bus_id")
    var busId: String? = null

    @ColumnInfo(name = "latitude")
    var latitude: String? = null

    @ColumnInfo(name = "longitude")
    var longitude: String? = null

    @ColumnInfo(name = "date")
    var trackDate: Long? = null

    @ColumnInfo(name = "sync_state")
    var syncState: Byte? = null
}