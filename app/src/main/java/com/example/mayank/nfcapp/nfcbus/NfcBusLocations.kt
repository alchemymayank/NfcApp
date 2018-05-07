package com.example.mayank.nfcapp.nfcbus

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Created by Mayank on 27/03/2018.
 */
class NfcBusLocations {

    @SerializedName("id")
    var id: Int? = null

    @SerializedName("bus_id")
    var busId: String? = null

    @SerializedName("latitude")
    var latitude: String? = null

    @SerializedName("longitude")
    var longitude: String? = null

    @SerializedName("track_date")
    var trackDate: Date? = null
}