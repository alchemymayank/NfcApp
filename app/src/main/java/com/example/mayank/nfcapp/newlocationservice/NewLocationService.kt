package com.example.mayank.nfcapp.newlocationservice

import android.app.job.JobParameters
import android.app.job.JobService
import com.example.mayank.nfcapp.Constants.showLogDebug
import com.example.mayank.nfcapp.network.TokenService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices

/**
 * Created by Mayank on 4/6/2018.
 */
class NewLocationService : JobService() {

    private val TAG = NewLocationService::class.java.simpleName

    var jobParameters : JobParameters? = null

    var locationHelper: UpdatedLocationHelper? = null
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProvider : FusedLocationProviderClient

    private val tokenService: TokenService by lazy { TokenService() }


    override fun onCreate() {
        super.onCreate()
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = LocationCallback()

    }

    override fun onStopJob(jobParameters: JobParameters?): Boolean {
        showLogDebug(TAG, "On Stop Job Called")
        jobFinished(jobParameters,false)
        return false
    }

    override fun onStartJob(jobParameters: JobParameters?): Boolean {
        this.jobParameters = jobParameters
        showLogDebug(TAG, "On Start Job Called")
        locationHelper = UpdatedLocationHelper(this, jobParameters, tokenService)
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        showLogDebug(TAG, "On Destroy called")

    }


}