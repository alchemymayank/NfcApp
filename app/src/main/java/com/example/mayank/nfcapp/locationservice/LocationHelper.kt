package com.example.mayank.nfcapp.locationservice

import android.Manifest
import android.app.job.JobParameters
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.widget.Toast
import com.example.mayank.nfcapp.Constants
import com.example.mayank.nfcapp.Constants.DEFAULT_SYNC_STATE
import com.example.mayank.nfcapp.Constants.checkInternetConnection
import com.example.mayank.nfcapp.roomdatabase.NfcDatabase
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.example.mayank.nfcapp.Constants.showLogDebug
import com.example.mayank.nfcapp.NfcApplication
import com.example.mayank.nfcapp.network.TokenService
import com.example.mayank.nfcapp.nfcbus.IBusLocation
import com.example.mayank.nfcapp.nfcbus.NfcBusLocations
import com.example.mayank.nfcapp.roomdatabase.BusLocations
import com.example.mayank.nfcapp.roomdatabase.Converters
import com.example.mayank.nfcapp.sample.LocationJobService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


/**
 * Created by Mayank on 15/03/2018.
 */
class LocationHelper(private val mContext: Context, roomDatabase: NfcDatabase, tokenService: TokenService) : GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    val roomDatabase = roomDatabase

    private val TAG = LocationHelper::class.java.simpleName

    // Google client to interact with Google API
    private var mGoogleApiClient: GoogleApiClient? = null

    private var mLocationRequest: LocationRequest? = null

    private var currentLocation : Location? = null

    val tokenService = tokenService






    init {
//        setLocationRequestParams()
        // Building the GoogleApi client
        buildGoogleApiClient()

        createLocationRequest()
    }

    //to be change this location request params to login page
//    private fun setLocationRequestParams() {
//        Constants.showLogDebug(TAG, "Setting Location request params")
//        val service = AnonymousApiService()
//        service.getService(ILocation::class.java).locationRequestParams.enqueue(object : Callback<UserLocation.LocationRequestParams> {
//            override fun onResponse(call: Call<UserLocation.LocationRequestParams>, response: Response<UserLocation.LocationRequestParams>) {
//                if (response.isSuccessful) {
//                    Constants.showLogDebug(TAG, "Response is Successful")
//                    val result = response.body()
//                    if (!result?.isSuccess!!) {
//                        Log.e(TAG, result.message)
//                    } else {
//                        Constants.showLogDebug(TAG, "Result is Success")
//                        UPDATE_INTERVAL = result.updateInterval
//                        FATEST_INTERVAL = result.fastestInterval
//                        DISPLACEMENT = result.displacement
//                    }
//                } else {
//                    val error = Globals.getErrorMessage(response)
//                    Log.e(TAG, error.messageWithCode)
//                }
//            }
//
//            override fun onFailure(call: Call<UserLocation.LocationRequestParams>, t: Throwable) {
//                Log.e(TAG, ProcessThrowable.getMessage(t))
//            }
//        })
//    }

    /**
     * Creating google api client object
     */
    @Synchronized private fun buildGoogleApiClient() {
        Constants.showLogDebug(TAG, "Building APIClient")
        mGoogleApiClient = GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build()

        mGoogleApiClient?.connect()
    }

    /**
     * Creating location request object
     */
    private fun createLocationRequest() {
        Constants.showLogDebug(TAG, "Create Location request")
        mLocationRequest = LocationRequest()
        mLocationRequest?.interval = UPDATE_INTERVAL
        mLocationRequest?.fastestInterval = FATEST_INTERVAL
        mLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//        mLocationRequest?.smallestDisplacement = DISPLACEMENT.toFloat()
    }

    /**
     * Starting the location updates
     */
    protected fun startLocationUpdates() {
        Constants.showLogDebug(TAG, "Starting Location Uopdates")

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(mContext, "Permission is not granted", Toast.LENGTH_SHORT).show()
            return
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this)

    }

    /**
     * Stopping location updates
     */
    fun stopLocationUpdates() {
        Constants.showLogDebug(TAG, "Stop Locations Updates")
//        if (realm != null) {
//            realm!!.removeAllChangeListeners()
//            realm!!.close()
//        }
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this)

        if (mGoogleApiClient?.isConnected!!) {
            mGoogleApiClient?.disconnect()
            showLogDebug(TAG, "Google Api Client disconnected")
        }

    }

    /**
     * Google api callback methods
     */
    override fun onConnectionFailed(result: ConnectionResult) {
        Log.e(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.errorCode)
    }

    override fun onConnected(arg0: Bundle?) {
        Constants.showLogDebug(TAG, "On Connected Called")
        startLocationUpdates()
    }


    override fun onConnectionSuspended(arg0: Int) {
        Constants.showLogDebug(TAG, "On Connection Suspended")
        mGoogleApiClient?.connect()
    }

    override fun onLocationChanged(location: Location) {
        currentLocation = location
        // Displaying the new location on UI
        Toast.makeText(mContext, "Latitude - " + location.latitude + " \n Longitude - " + location.longitude, Toast.LENGTH_LONG).show()
        showLogDebug(TAG, "Latitude - " + location.latitude + " \n Longitude - " + location.longitude)
//        if (mUserId == 0L) {
//            Log.e(TAG, "LoginUserData id is zero.")
//            return
//        }

        val busLocation = BusLocations()
        busLocation.busId = "1234"
        busLocation.latitude = location.latitude.toString()
        busLocation.longitude = location.longitude.toString()
        busLocation.trackDate = Converters.dateToTimestamp(Calendar.getInstance().time)


        if (checkInternetConnection(mContext)){
            val location = NfcBusLocations()

            location.busId = busLocation.busId
            location.latitude = busLocation.latitude
            location.longitude = busLocation.longitude
            location.trackDate = Converters.fromTimestamp(busLocation.trackDate)
            val iBusLocations : IBusLocation = tokenService.getService()
            val call = iBusLocations.addCurrentLocation(location)
            call.enqueue(object : Callback<Void>{
                override fun onResponse(call: Call<Void>?, response: Response<Void>?) {
                    showLogDebug(TAG, "Response :"+response)
                    showLogDebug(TAG, "Response Body : ${response?.body()}")
                    if (response?.isSuccessful!!){
                        showLogDebug(TAG, "Response is successful")
                        busLocation.syncState = 1
                        showLogDebug(TAG, "Bus Location Sync State : ${busLocation.syncState}")
                        NfcApplication.database.busLocationDao().insert(busLocation)
                        showLogDebug(TAG, "Data inserted to room successfully")

                    }else {
                        showLogDebug(TAG, "Response Failed")



                    }

                }

                override fun onFailure(call: Call<Void>?, t: Throwable?) {
                    showLogDebug(TAG, "Response Error : $t")
                    showLogDebug(TAG, "${t?.message}")
                    busLocation.syncState = DEFAULT_SYNC_STATE
                    NfcApplication.database.busLocationDao().insert(busLocation)
                    showLogDebug(TAG, "Data inserted to room successfully")


                }

            })

        }else{
            showLogDebug(TAG, "No Internet Connection")
            busLocation.syncState = DEFAULT_SYNC_STATE
            showLogDebug(TAG, "Bus Location Sync State : ${busLocation.syncState}")
            NfcApplication.database.busLocationDao().insert(busLocation)
            showLogDebug(TAG, "Data inserted to room successfully")

        }






//        val list = NfcApplication.database.busLocationDao().getAllLocation()
//
//
//        for (data in list){
//            showLogDebug(TAG, "Bus Id "+data.busId)
//            showLogDebug(TAG, "Latitude is : ${data.latitude}")
//            showLogDebug(TAG, "Longitude is : ${data.longitude}")
//            showLogDebug(TAG, "Track Date is : ${busLocation.trackDate}")
//            showLogDebug(TAG, "Sync State : ${busLocation.syncState}")
//        }




    }




    companion object {

        // Location updates intervals in sec
        private var UPDATE_INTERVAL: Long = 10000 // 10 sec
        private var FATEST_INTERVAL: Long = 5000 // 5 sec
        private var DISPLACEMENT = 10 // 10 meters
    }
}