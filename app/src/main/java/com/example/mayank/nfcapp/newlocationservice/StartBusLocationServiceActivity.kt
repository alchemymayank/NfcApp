package com.example.mayank.nfcapp.newlocationservice

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.example.mayank.nfcapp.Constants.showLogDebug
import com.example.mayank.nfcapp.NfcApplication
import com.example.mayank.nfcapp.R
import com.example.mayank.nfcapp.roomdatabase.Converters

class StartBusLocationServiceActivity : AppCompatActivity() {

    private val TAG = StartBusLocationServiceActivity::class.java.simpleName
    private val JOB_ID = 100
    private var jobScheduler : JobScheduler? = null
    internal lateinit var startService: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_bus_location_service)
        startService = findViewById<Button>(R.id.startService) as Button
        jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler?
    }

    fun getData(view: View){
        showLogDebug(TAG, "Get data button clicked")
        val list = NfcApplication.database.busLocationDao().getAllLocation()
        for (data in list){
            showLogDebug(TAG, "Bus Id : ${data.busId}")
            showLogDebug(TAG, "Latitude : ${data.latitude}")
            showLogDebug(TAG, "Longitude : ${data.longitude}")
            showLogDebug(TAG, "Track Date : ${Converters.fromTimestamp(data.trackDate)}")
        }
    }

    override fun onResume() {
        super.onResume()
        val isRunning = isLocationTrackingServiceRunning
        if (isRunning){
            setButtonStatus(isRunning)
        }
    }

    private fun setButtonStatus(isRunning : Boolean){
        if (!isRunning){
            startService.text = "Start Location Service"
        }else {
            startService.text = "Stop Location Service"
        }
    }

    fun startService(view: View){
        showLogDebug(TAG, "Start Service button clicked")
        val isRunning = isLocationTrackingServiceRunning
        if (!isRunning){
            startJobService()
            setButtonStatus(true)
        }else{
            stopJobService()
            setButtonStatus(false)
        }

    }

    private fun startJobService(){
        showLogDebug(TAG, "Inside start job service")
        jobScheduler?.schedule(JobInfo.Builder(JOB_ID,
                ComponentName(this, NewLocationService::class.java))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(10000)
                .setPersisted(true)
                .build())
    }

    private fun stopJobService(){
        showLogDebug(TAG, "Job Scheduler Stopping...")
        for (jobInfo in jobScheduler?.allPendingJobs!!) {
            if (jobInfo.id == JOB_ID) {
                jobScheduler!!.cancel(JOB_ID)
                showLogDebug(TAG, "Cancelled Job with ID:" + JOB_ID)
            }
        }
    }

    private val isLocationTrackingServiceRunning: Boolean
        get() = isServiceRunning(NewLocationService::class.java)

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        for (jobInfo in jobScheduler?.allPendingJobs!!) {
            if (jobInfo.id == JOB_ID) {
                return true
            }
        }
        return false
    }


}
