package com.example.mayank.nfcapp.network

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Created by Mayank on 17/03/2018.
 */
class NfcStudentLocations {

    @SerializedName("id")
    var id: Int? = null

    @SerializedName("student_id")
    var studentId: String? = null

    @SerializedName("latitude")
    var latitude: String? = null

    @SerializedName("longitude")
    var longitude: String? = null

    @SerializedName("track_date")
    var trackDate: Date? = null
}