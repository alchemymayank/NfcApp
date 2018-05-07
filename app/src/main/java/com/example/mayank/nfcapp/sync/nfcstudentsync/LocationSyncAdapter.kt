package com.example.mayank.nfcapp.sync.nfcstudentsync

import android.accounts.Account
import android.accounts.AccountManager
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Context
import android.content.SyncResult
import android.os.Bundle
import android.util.Log
import com.example.mayank.nfcapp.Constants.getErrorMessage
import com.example.mayank.nfcapp.Constants.isEmptyString
import com.example.mayank.nfcapp.Constants.showLogDebug
import com.example.mayank.nfcapp.NfcApplication
import com.example.mayank.nfcapp.framework.NotificationHelper
import com.example.mayank.nfcapp.network.ApiService
import com.example.mayank.nfcapp.network.CommonResult
import com.example.mayank.nfcapp.network.NfcStudentLocations
import com.example.mayank.nfcapp.network.TokenService
import com.example.mayank.nfcapp.roomdatabase.Converters
import java.io.IOException

/**
 * Created by Mayank on 26/03/2018.
 */
class LocationSyncAdapter(context: Context, autoInitialize: Boolean, allowParallelSyncs: Boolean, tokenService: TokenService) : AbstractThreadedSyncAdapter(context, autoInitialize, allowParallelSyncs) {
    private val accountManager: AccountManager
    val TAG = LocationSyncAdapter::class.java.simpleName
//    val roomDatabase = roomDatabase
//    val locationService =locationService
    val tokenService =tokenService


    init {
        accountManager = AccountManager.get(context)
    }

    override fun onPerformSync(account: Account, extras: Bundle, authority: String, provider: ContentProviderClient, syncResult: SyncResult) {
//        var realm: Realm? = null
        try {
            Log.d("sync", "Location onPerformSync for account[" + account.name + "]")
            Log.d("sync", "Location Sync started and running.")

//            realm = Realm.getDefaultInstance()
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
            if (isEmptyString(message)) {
                Log.d("sync", "Location Sync success.")
            } else {
                Log.d("sync", "Location Sync failed.")
                NotificationHelper.notify(context, "Location data sync failed : " + message, NotificationHelper.ERROR)
            }
        } finally {
            showLogDebug("LocationSyncAdapter","Finally Called")
//            if (realm != null) {
//                realm!!.close()
//            }
        }
    }


    private fun syncNfcLocation(): String {
        val message = ""

        val list = NfcApplication.database.nfcLocationDao().getAllLocation()
        if (list == null || list.isEmpty()){
            showLogDebug(TAG, "Room database list is null")
            return message
        }

        val sync : Byte = 1
        val location = NfcStudentLocations()
        val apiService : ApiService = tokenService.getService()
        var response: retrofit2.Response<Void>? = null

        for (loc in list){
            val syncState = loc.syncState
            if (syncState?.equals(sync)!!){
                showLogDebug(TAG, "Sync State is equal")
            }else{
                showLogDebug(TAG, "Sync state is not equal")
                location.id = loc.uid
                location.studentId = loc.nfcId
                location.latitude = loc.latitude
                location.longitude = loc.longitude
                location.trackDate = Converters.fromTimestamp(loc.trackTime)

                try {
                    response = apiService.addLocation(location).execute()
                }catch (e: IOException) {
                    showLogDebug(TAG, "IO Exception")
                    e.printStackTrace()
                    return e.message!!
                }

                showLogDebug(TAG, "Response is $response")
                showLogDebug(TAG, "Response body is ${response.body()}")
                showLogDebug(TAG, "Response code : ${response.code()}")
                if (response?.isSuccessful!!) {
                    showLogDebug(TAG, "Response is successful")
                    loc.syncState = 1
//                        roomDatabase.locationDao().updateLocation(location)
                    NfcApplication.database.nfcLocationDao().updateLocation(loc)
                    showLogDebug(TAG,"Save Location Successfully")
                    val result = response.body()
                    showLogDebug(TAG, "Response body : $result")

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
                    showLogDebug(TAG, "Response is not success")
                    val error = getErrorMessage(response)
                    return error.messageWithCode

                }
            }
        }
        return message
    }
}