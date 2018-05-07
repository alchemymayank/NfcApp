package com.example.mayank.nfcapp

import android.app.job.JobParameters
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import com.example.mayank.nfcapp.sample.LocationJobService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Response
import java.util.*

/**
 * Created by Mayank on 14/03/2018.
 */
object Constants {

    private val TAG : String = Constants::class.java.simpleName

    val MIME_TEXT_PLAIN = "text/plain"

//    val API_BASE_ADDRESS = "http://10.0.2.2:54929/api/"

    val API_BASE_ADDRESS ="http://nfcapi.technoholicdeveloper.com/api/"

//    val API_BASE_ADDRESS = "http://192.168.1.115:54929/api/"

//    val API_BASE_ADDRESS = "http://10.0.2.2:54929/api/"

    const val CONNECTION_TIMEOUT: Long = 60
    const val READ_TIMEOUT: Long = 60

    var jobParameters : JobParameters? = null
    var locationService : LocationJobService? = null

    val ACCOUNT_NAME = "sync.test@rmitsolutions.net"
    var ACCOUNT_TYPE: String? = "com.example.mayank.nfcapp.sync"
    var ACCOUNT_AUTHORITY_LOCATION: String? = "com.example.mayank.nfcapp.sync.locationsyncprovider"
    var ACCOUNT_AUTHORITY_BUS_LOCATION: String? = "com.example.mayank.nfcapp.sync.buslocationsyncprovider"

    val UNAUTHORIZED_CODE = 401

    val DEFAULT_SYNC_STATE: Byte = 0

    fun showLogDebug(tag : String,message : String){
        Log.d(tag, message)
    }

    fun isEmptyString(s: String?): Boolean {
        return s == null || s.trim { it <= ' ' }.isEmpty()
    }

    fun getErrorMessage(response: Response<*>): Error {
        try {
            var message = response.message()
            if (response.code() == UNAUTHORIZED_CODE) {
                message = "Session expired."
            } else {
                val body = response.errorBody()
                if (body != null) {
                    val gson = Gson()
                    try {
                        val json = body.string()
                        val type = object : TypeToken<Map<String, ArrayList<String>>>() {

                        }.type
                        val map = gson.fromJson<Map<String, ArrayList<String>>>(json, type)
                        for (key in map.keys) {
                            message += "\n" + map[key]!!.get(0)
                        }
                    } catch (e: Exception) {
                        //ignore
                        Log.e("Globals.getErrorMessage", e.message)
                    }

                }
            }
            return Error(response.code(), message)
        } catch (e: Exception) {
            Log.e("Globals.getErrorMessage", e.message)
            return Error(0, e.message!!)
        }

    }

    fun checkInternetConnection(baseContext: Context): Boolean {
        val connectivityManager = baseContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val networkInfo = connectivityManager?.activeNetworkInfo
        if (networkInfo!=null && networkInfo.isConnected){
            // You are connected to internet.
            if (networkInfo.type == ConnectivityManager.TYPE_WIFI){
                showLogDebug(TAG, "You are connected to wifi internet service")
            }

            if (networkInfo.type == ConnectivityManager.TYPE_WIFI){
                showLogDebug(TAG, "You are connected to mobile internet service")
            }
            return true
        }else {
            showLogDebug(TAG, "No Internet! Please check your internet connection!")
            return false
        }

    }

}