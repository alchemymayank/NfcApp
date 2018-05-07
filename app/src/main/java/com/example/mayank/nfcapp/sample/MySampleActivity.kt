package com.example.mayank.nfcapp.sample

import android.Manifest
import android.accounts.Account
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.mayank.nfcapp.Constants
import com.example.mayank.nfcapp.Constants.ACCOUNT_AUTHORITY_BUS_LOCATION
import com.example.mayank.nfcapp.Constants.ACCOUNT_AUTHORITY_LOCATION
import com.example.mayank.nfcapp.Constants.ACCOUNT_NAME
import com.example.mayank.nfcapp.R
import com.example.mayank.nfcapp.Constants.showLogDebug
import com.example.mayank.nfcapp.NfcApplication
import com.example.mayank.nfcapp.network.*
import com.example.mayank.nfcapp.nfcbus.IBusLocation
import com.example.mayank.nfcapp.nfcbus.NfcBusLocations
import com.example.mayank.nfcapp.roomdatabase.Converters
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.util.*


/**
 * Created by Mayank on 19/03/2018.
 */
class MySampleActivity : AppCompatActivity() {

    private val TAG: String = MySampleActivity::class.java.simpleName

    private val tokenService: TokenService by lazy { TokenService() }

    var retrofit : Retrofit? = null

    private val JOB_ID = 100

    private var jobScheduler : JobScheduler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)
        jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler?
    }

    fun startService(view: View){
        showLogDebug(TAG, "Start Service button clicked")
        startJobService()
    }

    @SuppressLint("MissingPermission")
    private fun startJobService(){
        showLogDebug(TAG, "Inside start job service")

        jobScheduler?.schedule(JobInfo.Builder(JOB_ID,
                ComponentName(this, LocationJobService::class.java))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(10000)
                .setPersisted(true)
                .build())
    }

    fun stopService(view: View){
        showLogDebug(TAG, "Stop Service button clicked")
        stopJobService()
    }

    private fun stopJobService(){
        showLogDebug(TAG, "Inside stop job service")

        for (jobInfo in jobScheduler?.allPendingJobs!!) {
            if (jobInfo.id == JOB_ID) {
                jobScheduler!!.cancel(JOB_ID)
                showLogDebug(TAG, "Cancelled Job with ID:" + JOB_ID)
            }
        }
    }

    fun checkService(view: View){
        showLogDebug(TAG, "Stop service button clicked")
        val isRunning = isLocationTrackingServiceRunning
        if (isRunning){
            showLogDebug(TAG, "Service is running")
        }else{
            showLogDebug(TAG, "Service stopped")
        }
    }

    private val isLocationTrackingServiceRunning: Boolean
        get() = isServiceRunning(LocationJobService::class.java)

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        for (jobInfo in jobScheduler?.allPendingJobs!!) {
            if (jobInfo.id == JOB_ID) {
                return true
            }
        }
        return false
    }


//    fun getDataFromRoom(view: View){
////        showLogDebug(TAG, "Get data from room button clicked")
////        val list = NfcApplication.database.nfcLocationDao().getAllLocation()
////        for (data in list){
////            showLogDebug(TAG, "Id : ${data.uid}")
////            showLogDebug(TAG, "NFC Id : ${data.nfcId}")
////            showLogDebug(TAG, "Latitude : ${data.latitude}")
////            showLogDebug(TAG, "Longitude : ${data.longitude}")
////            showLogDebug(TAG, "Track Date : ${data.trackTime}")
////            showLogDebug(TAG, "Sync State : ${Converters.fromTimestamp(data.trackTime)}")
////        }
//
//        showLogDebug(TAG, "Get data from room button clicked")
//
//        getBusLocation()
//
//    }

//    private fun getBusLocation() {
//        showLogDebug(TAG, "Inside get bus location")
//        val service : IBusLocation = tokenService.getService()
//        val call = service.getLastKnownLocations("1234")
//        call.enqueue(object : Callback<NfcBusLocations>{
//            override fun onFailure(call: Call<NfcBusLocations>?, t: Throwable?) {
//                showLogDebug(TAG, "Error $t")
//            }
//
//            override fun onResponse(call: Call<NfcBusLocations>?, response: Response<NfcBusLocations>?) {
//                showLogDebug(TAG, "Response : $response")
//                showLogDebug(TAG, "Response body : ${response?.body()}")
//                showLogDebug(TAG, "Response Code : ${response?.code()}")
//                val list = response?.body()
//                showLogDebug(TAG, "Id : ${list?.id}")
//                showLogDebug(TAG, "Bus Id : ${list?.busId}")
//                showLogDebug(TAG, "Latitude : ${list?.latitude}")
//                showLogDebug(TAG, "Longitude ; ${list?.longitude}")
//                showLogDebug(TAG, "Track Date : ${list?.trackDate}")
//            }
//
//        })
//    }

//    fun getAllLocations(view: View){
//        showLogDebug(TAG, "Get All Location Button Clicked")
//        val service : ApiService = tokenService.getService()
//        val call = service.getNfcLocations()
//        call.enqueue(object : Callback<List<NfcStudentLocations>>{
//            override fun onFailure(call: Call<List<NfcStudentLocations>>?, t: Throwable?) {
//                showLogDebug(TAG, "Error : $t")
//            }
//
//            override fun onResponse(call: Call<List<NfcStudentLocations>>?, response: Response<List<NfcStudentLocations>>?) {
//                showLogDebug(TAG, "Response $response")
//                val list = response?.body()
//                for(data in list!!){
//                    showLogDebug(TAG, "Id : ${data.id}")
//                    showLogDebug(TAG, "Student Id : ${data.studentId}")
//                    showLogDebug(TAG, "Latitude : ${data.latitude}")
//                    showLogDebug(TAG, "Longitude : ${data.longitude}")
//                    showLogDebug(TAG, "Track Date : ${data.trackDate}")
//                }
//            }
//
//        })
//    }

//    fun addLocation(view: View){
//        val location = NfcStudentLocations()
////        location.id = 36
//        location.studentId = "h1300165"
//        location.latitude = "23.123456"
//        location.longitude = "78.123456"
//        location.trackDate = Calendar.getInstance().time
//        showLogDebug(TAG, "Add Location Button Clicked")
//        val apiService : ApiService = tokenService.getService()
//        val call = apiService.addLocation(location)
//        call.enqueue(object : Callback<Void>{
//            override fun onFailure(call: Call<Void>, t: Throwable?) {
//                showLogDebug(TAG, "Failed Error : $t")
//            }
//
//            override fun onResponse(call: Call<Void>, response: Response<Void>) {
//                showLogDebug(TAG, "Response $response")
//                showLogDebug(TAG, "Response body : ${response.body()}")
//                showLogDebug(TAG, "Response code : ${response.code()}")
//            }
//
//        })
//    }






//    fun pushArrayList(view: View){
//        showLogDebug(TAG, "Push array list button clicked")
//        val myArr = ArrayList<StudentLocations>()
//        myArr.add(StudentLocations(6,"h123456","23.123456","78.123456", Calendar.getInstance().time))
//        myArr.add(StudentLocations(7,"h123456","23.123456","78.123456", Calendar.getInstance().time))
//        myArr.add(StudentLocations(8,"h123456","23.123456","78.123456", Calendar.getInstance().time))
//        myArr.add(StudentLocations(9,"h123456","23.123456","78.123456", Calendar.getInstance().time))
//        myArr.add(StudentLocations(10,"h123456","23.123456","78.123456", Calendar.getInstance().time))
//
//
//        val apiService: ApiService = tokenService.getService()
//
//        for (item in myArr){
//            showLogDebug(TAG, "Id: ${item.id}")
//            showLogDebug(TAG, "Student Id : ${item.studentId}")
//            showLogDebug(TAG, "Latitude : ${item.latitude}")
//            showLogDebug(TAG, "Longitude : ${item.longitude}")
//            showLogDebug(TAG, "Track Date : ${item.trackDate}")
//            val location = NfcStudentLocations()
//            location.id = item.id
//            location.studentId = item.studentId
//            location.latitude = item.latitude
//            location.longitude = item.longitude
//            location.trackDate = item.trackDate
//            val call = apiService.addLocation(location).enqueue(object :retrofit2.Callback<CommonResult>{
//                override fun onResponse(call: Call<CommonResult>?, response: Response<CommonResult>?) {
//                    showLogDebug(TAG, "Response $response")
//                    showLogDebug(TAG, "Response Message : ${response?.message()}")
//                    showLogDebug(TAG, "Response body : ${response?.body()}")
//                }
//
//                override fun onFailure(call: Call<CommonResult>?, t: Throwable?) {
//                    showLogDebug(TAG, "Error : $t")
//                }
//
//            })
//
//        }
//    }

//    fun getBusLocation(view: View){
//        showLogDebug(TAG, "Bus Location Button clicked")
//        val list = NfcApplication.database.busLocationDao().getAllLocation()
//
//        for (data in list){
//            showLogDebug(TAG, "Id : ${data.uid}")
//            showLogDebug(TAG, "Bus Id : ${data.busId}")
//            showLogDebug(TAG, "Latitude : ${data.latitude}")
//            showLogDebug(TAG, "Longitude : ${data.longitude}")
//            showLogDebug(TAG, "Track Date : ${Converters.fromTimestamp(data.trackDate)}")
//        }
//    }
}