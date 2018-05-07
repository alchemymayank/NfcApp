package com.example.mayank.nfcapp.sample

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import com.example.mayank.nfcapp.Constants
import com.example.mayank.nfcapp.Constants.locationService
import com.example.mayank.nfcapp.Constants.showLogDebug
import com.example.mayank.nfcapp.NfcApplication
import com.example.mayank.nfcapp.locationservice.LocationHelper
import com.example.mayank.nfcapp.network.TokenService

/**
 * Created by Mayank on 04/04/2018.
 */
class LocationJobService : JobService() {

    private val TAG : String = LocationJobService::class.java.simpleName
    var jobParameters : JobParameters? = null

    var locationHelper: LocationHelper? = null
    private val tokenService: TokenService by lazy { TokenService() }

//    var myTask : MyTask? = null
    var context : Context? = null



//    override fun onCreate() {
//        super.onCreate()
//        showLogDebug(TAG, "On Create Called")
//
//
//    }

    override fun onStartJob(jobParameters: JobParameters?): Boolean {
        showLogDebug(TAG, "On start job called")
        Constants.jobParameters = jobParameters
        locationService = LocationJobService()
        locationHelper = LocationHelper(applicationContext, NfcApplication.database, tokenService)
//        myTask = MyTask()
//        myTask?.execute()
//        jobFinished(jobParameters, false)

        return false
    }

    override fun onStopJob(jobParameters: JobParameters?): Boolean {
        return false
        showLogDebug(TAG, "On Stop job called")
        jobFinished(jobParameters,false)
    }

    override fun onDestroy() {
        super.onDestroy()
        showLogDebug(TAG, "On destroy called")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        showLogDebug(TAG, "On Low memory called")
    }

//    class MyTask : AsyncTask<JobParameters, Void, Void>(){
//
//        var context : Context? = null
//        val TAG = MyTask::class.java.simpleName
//        var locationService : LocationJobService? = null
//        var locationHelper: LocationHelper? = null
//        private val tokenService: TokenService by lazy { TokenService() }
//
//        override fun doInBackground(vararg jobParameters: JobParameters?): Void? {
//            locationService = LocationJobService()
//            context = locationService?.context
//            locationHelper = LocationHelper(context!!, NfcApplication.database, tokenService)
//            locationService?.jobFinished(locationService?.jobParameters, false)
//            return null
//        }
//
//        override fun onPostExecute(result: Void?) {
//            super.onPostExecute(result)
//            locationService?.jobFinished(locationService?.jobParameters, false);
//        }
//
//    }

}