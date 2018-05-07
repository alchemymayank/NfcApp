package com.example.mayank.nfcapp.locationservice

import android.app.Service
import android.content.Intent
import android.os.*
import com.example.mayank.nfcapp.Constants.showLogDebug
import com.example.mayank.nfcapp.NfcApplication
import com.example.mayank.nfcapp.network.TokenService

/**
 * Created by Mayank on 15/03/2018.
 */
class LocationService : Service() {

    private val TAG = LocationService::class.java.simpleName

    private var mServiceLooper: Looper? = null
    private var mServiceHandler: ServiceHandler? = null
    private var locationHelper: LocationHelper? = null
    private val tokenService: TokenService by lazy { TokenService() }


    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {

            locationHelper = LocationHelper(applicationContext, NfcApplication.database, tokenService)

//            locationHelper = LocationHelper(applicationContext, userId, roomDatabase)
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            //stopSelf(msg.arg1);
        }
    }

    override fun onCreate() {
        super.onCreate()
        showLogDebug(TAG, "On Create Called")

        val thread = HandlerThread("LocationServiceStartArgs", Process.THREAD_PRIORITY_BACKGROUND)
        thread.start()

        mServiceLooper = thread.looper
        mServiceHandler = ServiceHandler(mServiceLooper!!)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showLogDebug(TAG, "On Start Command Called")
//        return super.onStartCommand(intent, flags, startId)
        val msg = mServiceHandler?.obtainMessage()
        msg?.arg1 = startId
        mServiceHandler?.sendMessage(msg)

        // If we get killed, after returning from here, restart
        return Service.START_STICKY
    }


    override fun onBind(p0: Intent?): IBinder? {
        showLogDebug(TAG, "On Bind Called")
        return null
    }

    override fun onDestroy() {
        showLogDebug(TAG, "On Destroy Called")
        locationHelper?.stopLocationUpdates()
    }




}