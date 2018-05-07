package com.example.mayank.nfcapp.sync.nfcbusync

import android.accounts.Account
import android.accounts.AccountManager
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Context
import android.content.SyncResult
import android.os.Bundle
import android.util.Log
import com.example.mayank.nfcapp.Constants
import com.example.mayank.nfcapp.NfcApplication
import com.example.mayank.nfcapp.framework.NotificationHelper
import com.example.mayank.nfcapp.network.ApiService
import com.example.mayank.nfcapp.network.CommonResult
import com.example.mayank.nfcapp.network.NfcStudentLocations
import com.example.mayank.nfcapp.network.TokenService
import com.example.mayank.nfcapp.nfcbus.IBusLocation
import com.example.mayank.nfcapp.nfcbus.NfcBusLocations
import com.example.mayank.nfcapp.roomdatabase.Converters
import com.example.mayank.nfcapp.sync.nfcstudentsync.LocationSyncAdapter
import java.io.IOException
import com.example.mayank.nfcapp.Constants.checkInternetConnection
import com.example.mayank.nfcapp.Constants.showLogDebug

/**
 * Created by Mayank on 27/03/2018.
 */
class BusLocationSyncAdapter(context: Context, autoInitialize: Boolean, allowParallelSyncs: Boolean, tokenService: TokenService) : AbstractThreadedSyncAdapter(context, autoInitialize, allowParallelSyncs) {
    private val accountManager: AccountManager
    val TAG = BusLocationSyncAdapter::class.java.simpleName
    //    val roomDatabase = roomDatabase
//    val locationService =locationService
    val ctx = context
    val tokenService =tokenService


    init {
        accountManager = AccountManager.get(ctx)
    }

    override fun onPerformSync(account: Account, extras: Bundle, authority: String, provider: ContentProviderClient, syncResult: SyncResult) {
//        var realm: Realm? = null
        try {
            Log.d("sync", "Location onPerformSync for account[" + account.name + "]")
            Log.d("sync", "Location Sync started and running.")

//            val globals = Globals()
//            if (!globals.checkUserInfo(context)) {
//                NotificationHelper.notify(context, "Login is required to sync data.", NotificationHelper.ERROR)
//                return
//            }
//
//            val authState = SettingsUtils.restoreAuthState(context)
//            if (authState == null || authState.accessToken == null) {
//                NotificationHelper.notify(context, "Login is required to sync data.", NotificationHelper.ERROR)
//                return
//            }

//            val message = syncLocation(authState.accessToken, roomDatabase)
            val message = syncNfcLocation()
            if (Constants.isEmptyString(message)) {
                Log.d("sync", "Location Sync success.")
            } else {
                Log.d("sync", "Location Sync failed.")
                NotificationHelper.notify(context, "Location data sync failed : " + message, NotificationHelper.ERROR)
            }
        } finally {
            Constants.showLogDebug("LocationSyncAdapter", "Finally Called")
//            if (realm != null) {
//                realm!!.close()
//            }
        }
    }


    private fun syncNfcLocation(): String {
        val message = ""

        val list = NfcApplication.database.busLocationDao().getAllLocation()
        if (list == null || list.isEmpty()){
            Constants.showLogDebug(TAG, "Room database Bus list is null")
            return message
        }

        val sync : Byte = 1
        val location = NfcBusLocations()
        val iBusLocation : IBusLocation = tokenService.getService()
        var response: retrofit2.Response<Void>? = null

        for (loc in list){
            val syncState = loc.syncState
            showLogDebug(TAG, "room sync state : $syncState")
            if (syncState?.equals(sync)!!){
                Constants.showLogDebug(TAG, "Sync State is equal")
            }else{
                Constants.showLogDebug(TAG, "Sync state is not equal")
//                location.id = loc.uid
                location.busId = loc.busId
                location.latitude = loc.latitude
                location.longitude = loc.longitude
                location.trackDate = Converters.fromTimestamp(loc.trackDate)

                try {
                    response = iBusLocation.addLocation(location).execute()
                }catch (e: IOException) {
                    Constants.showLogDebug(TAG, "IO Exception")
                    e.printStackTrace()
                    return e.message!!
                }

                Constants.showLogDebug(TAG, "Response is $response")
                Constants.showLogDebug(TAG, "Response body is ${response.body()}")
                Constants.showLogDebug(TAG, "Response code : ${response?.code()}")
                if (response?.isSuccessful!!) {
                    Constants.showLogDebug(TAG, "Response is successful")
                    loc.syncState = 1
//                        roomDatabase.locationDao().updateLocation(location)
                    NfcApplication.database.busLocationDao().updateLocation(loc)
                    Constants.showLogDebug(TAG, "Save Location Successfully")
                    val result = response.body()
                    Constants.showLogDebug(TAG, "Response body : $result")

//                    if (result?.isSuccess!!) {
//                        showLogDebug(TAG, "Result is Success")
//                        //update sync state
////                    realm!!.beginTransaction()
//                        loc.syncState = 1
////                        roomDatabase.locationDao().updateLocation(location)
//                        NfcApplication.database.nfcLocationDao().updateLocation(loc)
//
//                        showLogDebug(TAG,"Save Location Successfully")
////                    realm!!.commitTransaction()
//                    } else {
//                        showLogDebug(TAG, "Result is not success")
//                        return result.message
//                    }
                } else {
                    Constants.showLogDebug(TAG, "Response is not success")
                    val error = Constants.getErrorMessage(response)
                    return error.messageWithCode

                }

            }
        }
        return message
    }
}