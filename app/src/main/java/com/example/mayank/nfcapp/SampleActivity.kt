package com.example.mayank.nfcapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.example.mayank.nfcapp.Constants.showLogDebug
import com.example.mayank.nfcapp.network.ApiService
import com.example.mayank.nfcapp.network.NfcStudentLocations
import com.example.mayank.nfcapp.network.StudentLocations
import com.example.mayank.nfcapp.network.TokenService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject


/**
 * Created by Mayank on 15/03/2018.
 */
class SampleActivity : AppCompatActivity() {


    private val TAG = SampleActivity::class.java.simpleName
    private val tokenService: TokenService by lazy { TokenService() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)
    }

    fun startService(view: View){
        showLogDebug(TAG, "Start service button clicked")
        getDetails()
    }

    fun stopService(view: View){
        showLogDebug(TAG, "Stop service button clicked")
//        postDetails()

    }

//    private fun postDetails(){
//        val location = NfcStudentLocations()
//        location.studentId = "h1234567890"
//        location.latitude = "23.456789"
//        location.longitude = "78.123456"
//        location.trackDate = Calendar.getInstance().time
//        val apiService : ApiService = tokenService.getService()
//        val call = apiService.insertToken(location.studentId!!, location.latitude!!, location.longitude!!, location.trackDate!!)
//        call.enqueue(object : Callback<NfcStudentLocations>{
//            override fun onFailure(call: Call<NfcStudentLocations>?, t: Throwable?) {
//                showLogDebug(TAG, "Response failure $t")
//            }
//
//            override fun onResponse(call: Call<NfcStudentLocations>?, response: Response<NfcStudentLocations>?) {
//                showLogDebug(TAG, "Response successful $response")
//            }
//
//        })
//    }

    private fun getDetails(){
        showLogDebug(TAG, "Inside get service")

        val apiService : ApiService = tokenService.getService()
        val call = apiService.getLocations("1")
//        call.enqueue(object : Callback<List<NfcStudentLocations>> {
//            override fun onResponse(call: Call<List<NfcStudentLocations>>, response: Response<List<NfcStudentLocations>>) {
//
//                //In this point we got our hero list
//                //thats damn easy right ;)
//                val locationList = response.body()
//                val index = locationList?.get(1)
//
//                showLogDebug(TAG, "Student Id : ${index?.studentId}")
//                showLogDebug(TAG, "Latitude is : ${index?.latitude}")
//                showLogDebug(TAG, "Longitude is : ${index?.longitude}")
//                showLogDebug(TAG, "Track Date is : ${index?.trackDate}")
//
//                showLogDebug(TAG, "Response is : $response")
//                showLogDebug(TAG, "Response message : ${response.message()}")
//                showLogDebug(TAG, "Response Body : $locationList")
//
//                //now we can do whatever we want with this list
//
//            }
//
//            override fun onFailure(call: Call<List<NfcStudentLocations>>, t: Throwable) {
//                Toast.makeText(applicationContext, t.message, Toast.LENGTH_SHORT).show()
//                showLogDebug(TAG, "Error : $t")
//            }
//        })
//        call.enqueue(object : Callback<NfcStudentLocations> {
//            override fun onFailure(call: Call<NfcStudentLocations>?, t: Throwable?) {
//                showLogDebug(TAG, "On Failure called")
//                showLogDebug(TAG, "Exception : $t")
//
//            }
//
//            override fun onResponse(call: Call<NfcStudentLocations>?, response: Response<NfcStudentLocations>?) {
//                showLogDebug(TAG, "On response called")
//                showLogDebug(TAG, response.toString())
//                showLogDebug(TAG, "Message " +response?.message()!!)
//
//                if (response?.isSuccessful!!){
//                    showLogDebug(TAG, "Response is successful")
//                }else {
//                    showLogDebug(TAG, "Response is not successful")
//                }
//            }
//
//        })


    }




}


