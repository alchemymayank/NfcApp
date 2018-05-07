package com.example.mayank.nfcapp.newlocationservice

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.location.Location
import android.util.Log
import android.widget.Toast
import com.example.mayank.nfcapp.Constants
import com.example.mayank.nfcapp.Constants.showLogDebug
import com.example.mayank.nfcapp.NfcApplication
import com.example.mayank.nfcapp.network.TokenService
import com.example.mayank.nfcapp.nfcbus.IBusLocation
import com.example.mayank.nfcapp.nfcbus.NfcBusLocations
import com.example.mayank.nfcapp.roomdatabase.BusLocations
import com.example.mayank.nfcapp.roomdatabase.Converters
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

/**
 * Created by Mayank on 4/6/2018.
 */
class UpdatedLocationHelper(newLocationService: NewLocationService, jobParameters: JobParameters?, tokenService: TokenService) {

    private val TAG = UpdatedLocationHelper::class.java.simpleName

    private val newLocationService = newLocationService
    private val jobParameters = jobParameters
    val tokenService = tokenService


    init {
        getLastLocation()
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        val locationClient = LocationServices.getFusedLocationProviderClient(newLocationService)

        locationClient.lastLocation
                .addOnSuccessListener { location ->
                    // GPS location can be null if GPS is switched off
                    if (location != null) {
                        onLocationChanged(location)
                    }
                }
                .addOnFailureListener { e ->
                    Log.d("MapDemoActivity", "Error trying to get last GPS location")
                    e.printStackTrace()
                }
    }

    private fun onLocationChanged(location: Location) {
        showLogDebug(TAG, "Inside on Location changed")
        val NEW_TAG = "onLocationChanged"
        val message = "Latitude : ${location.latitude} Longitude : ${location.longitude}"
        showLogDebug(NEW_TAG, message)
//        Toast.makeText(newLocationService, message, Toast.LENGTH_SHORT).show()
//
        val busLocation = BusLocations()
        busLocation.busId = "1234"
        busLocation.latitude = location.latitude.toString()
        busLocation.longitude = location.longitude.toString()
        busLocation.trackDate = Converters.dateToTimestamp(Calendar.getInstance().time)

        if (Constants.checkInternetConnection(newLocationService)) {
            val location = NfcBusLocations()

            location.busId = busLocation.busId
            location.latitude = busLocation.latitude
            location.longitude = busLocation.longitude
            location.trackDate = Converters.fromTimestamp(busLocation.trackDate)
            val iBusLocations: IBusLocation = tokenService.getService()
            val call = iBusLocations.addCurrentLocation(location)
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>?, response: Response<Void>?) {
                    showLogDebug(TAG, "Response :" + response)
                    showLogDebug(TAG, "Response Body : ${response?.body()}")
                    if (response?.isSuccessful!!) {
                        showLogDebug(TAG, "Response is successful")
                        busLocation.syncState = 1
                        showLogDebug(TAG, "Bus Location Sync State : ${busLocation.syncState}")
                        NfcApplication.database.busLocationDao().insert(busLocation)
                        showLogDebug(TAG, "Data inserted to room successfully")
                    } else {
                        showLogDebug(TAG, "Response Failed")
                    }
                }

                override fun onFailure(call: Call<Void>?, t: Throwable?) {
                    showLogDebug(TAG, "Response Error : $t")
                    showLogDebug(TAG, "${t?.message}")
                    busLocation.syncState = Constants.DEFAULT_SYNC_STATE
                    NfcApplication.database.busLocationDao().insert(busLocation)
                    showLogDebug(TAG, "Data inserted to room successfully")
                }
            })
        } else {
            showLogDebug(TAG, "No Internet Connection")
            busLocation.syncState = Constants.DEFAULT_SYNC_STATE
            showLogDebug(TAG, "Bus Location Sync State : ${busLocation.syncState}")
            NfcApplication.database.busLocationDao().insert(busLocation)
            showLogDebug(TAG, "Data inserted to room successfully")
        }
        newLocationService.jobFinished(jobParameters, false)
    }


}