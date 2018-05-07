package com.example.mayank.nfcapp.locationservice

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.mayank.nfcapp.Constants.checkInternetConnection
import com.example.mayank.nfcapp.R
import com.example.mayank.nfcapp.Constants.showLogDebug
import com.example.mayank.nfcapp.NfcApplication
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes

/**
 * Created by Mayank on 16/03/2018.
 */
class StartBusLocationActivity : AppCompatActivity() {

    private val TAG = StartBusLocationActivity::class.java.simpleName

    private val PERMISSIONS_REQUESTS = 0x1

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest? = null

    var connectivityManager : ConnectivityManager? = null

    internal lateinit var startService: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_bus_location_service)

        startService = findViewById<Button>(R.id.startService) as Button

    }

    fun testButton(view: View){
        showLogDebug(TAG, "Test Button Clicked")
        val list = NfcApplication.database.busLocationDao().getAllLocation()

        for (data in list){
            showLogDebug(TAG, "Id : ${data.uid}")
            showLogDebug(TAG, "Bus id : ${data.busId}")
            showLogDebug(TAG, "Latitide : ${data.latitude}")
            showLogDebug(TAG, "Longitude : ${data.longitude}")
            showLogDebug(TAG, "Track Date : ${data.trackDate}")
            showLogDebug(TAG, "Sync State : "+ data.syncState)

        }
    }



    fun startService(view: View){
        checkAndStartLocationService()
//        if (checkInternetConnection(baseContext)){
//            checkAndStartLocationService()
//        }else {
//            val snackbar = Snackbar.make(view, "No Internet Connection!", Snackbar.LENGTH_LONG)
//                    .setAction("RETRY", View.OnClickListener {
////                        checkAndStartLocationService()
//                    })
//            snackbar.setActionTextColor(Color.RED)
//            val snackBarView: View = snackbar.view
//            val text : TextView = snackBarView.findViewById<TextView>(android.support.design.R.id.snackbar_text)
//            text.setTextColor(Color.YELLOW)
//            snackbar.show()
//        }

    }

    override fun onResume() {
        super.onResume()
        val isRunning = isLocationTrackingServiceRunning
        setButtonStatus(isRunning)
    }



    private fun checkAndStartLocationService() {
        val isRunning = isLocationTrackingServiceRunning
        if (isRunning) {
            showLogDebug(TAG, "Service is running")
            startLocationService(false)
        } else {
            showLogDebug(TAG, "Checking for permission")
            checkLocationPermissions()
        }
    }

    private fun checkLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                        PERMISSIONS_REQUESTS)

            } else {
                checkLocationSettings()
            }
        } else {
            checkLocationSettings()
        }
    }

    private fun checkLocationSettings() {
        try {
            if (!checkPlayServices()) {
                return
            }
            // Building the GoogleApi client
            showLogDebug(TAG, "Building google api client")
            mGoogleApiClient = GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API).build()
            mGoogleApiClient?.connect()

            mLocationRequest = LocationRequest()
            mLocationRequest?.interval = UPDATE_INTERVAL.toLong()
            mLocationRequest?.fastestInterval = FATEST_INTERVAL.toLong()
            mLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            val builder = LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest!!)
            builder.setAlwaysShow(true)
            val result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                    builder.build())

            result.setResultCallback { locationSettingsResult ->
                val status = locationSettingsResult.status
                //final LocationSettingsStates locationSettingsStates = locationSettingsResult.getLocationSettingsStates();
                when (status.statusCode) {
                    LocationSettingsStatusCodes.SUCCESS ->
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        startLocationService(true)
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    this@StartBusLocationActivity,
                                    REQUEST_CHECK_SETTINGS)
                        } catch (e: IntentSender.SendIntentException) {
                            // Ignore the error.
                        }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        Toast.makeText(this@StartBusLocationActivity, "Couldn't find the location services.", Toast.LENGTH_LONG).show()
                }
            }
        } finally {
            if (mGoogleApiClient != null && mGoogleApiClient?.isConnected!!) {
                mGoogleApiClient?.disconnect()
            }
            mLocationRequest = null
        }
    }

    private fun startLocationService(start: Boolean) {
        showLogDebug(TAG, "Start Location Service")
        val intent = Intent(this, LocationService::class.java)
        if (start) {
            startService(intent)
        }else{
            stopService()
        }
        setButtonStatus(start)
        showSnackbar(startService, if (start) "Started" else "Finished")
    }


    @SuppressLint("ResourceAsColor")
    private fun setButtonStatus(isRunning : Boolean){
        if (!isRunning){
            startService.text = "Start Location Service"
        }else {
            startService.text = "Stop Location Service"
        }
    }

    protected fun showSnackbar(view: View?, message: String) {
        Snackbar.make(view!!, message, Snackbar.LENGTH_LONG).show()
    }

    private fun stopService(){
        showLogDebug(TAG, "Stopping Service...")
        val intent = Intent(this, LocationService::class.java)
        val isRunning = isLocationTrackingServiceRunning
        if (isRunning){
            stopService(intent)
            showLogDebug(TAG, "Service Stopped")
        }
    }

    fun stopService(view: View){
        showLogDebug(TAG, "Stop service button clicked")
        val intent = Intent(this, LocationService::class.java)
        val isRunning = isLocationTrackingServiceRunning
        if (isRunning){
            showLogDebug(TAG, "Service is running")
            stopService(intent)
        }else {
            showLogDebug(TAG, "Service is not running")
        }
    }

    private val isLocationTrackingServiceRunning: Boolean
        get() = isServiceRunning(LocationService::class.java)

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
        // Check for the integer request code originally supplied to startResolutionForResult().
            REQUEST_CHECK_SETTINGS -> when (resultCode) {
                Activity.RESULT_OK -> startLocationService(true)
                Activity.RESULT_CANCELED -> checkLocationSettings()//keep asking
            }
        }
    }

    private fun checkPlayServices(): Boolean {
        val resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show()
            } else {
                Toast.makeText(applicationContext,
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show()
            }
            return false
        }
        return true
    }

    companion object {
        private val PLAY_SERVICES_RESOLUTION_REQUEST = 1000
        private val REQUEST_CHECK_SETTINGS = 209

        // Location updates intervals in sec
        private val UPDATE_INTERVAL = 10000 // 10 sec
        private val FATEST_INTERVAL = 5000 // 5 sec
    }
}