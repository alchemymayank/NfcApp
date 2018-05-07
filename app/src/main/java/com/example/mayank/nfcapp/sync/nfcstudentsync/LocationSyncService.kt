package com.example.mayank.nfcapp.sync.nfcstudentsync

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.mayank.nfcapp.network.TokenService

/**
 * Created by Mayank on 26/03/2018.
 */
class LocationSyncService : Service() {

//    @Inject lateinit var roomDatabase: MyDatabase
//
//    @Inject
//    lateinit var locationService : ILocation
private val tokenService: TokenService by lazy { TokenService() }




    override fun onCreate() {
        super.onCreate()

//        val depComponent = DaggerBaseActivityComponent.builder()
//                .applicationComponents(OLMSApplication.applicationComponent)
//                .build()
//
//        depComponent.injectLocationSyncService(this)

        synchronized(syncAdapterLocker) {
            if (syncAdapter == null) {
                syncAdapter = LocationSyncAdapter(applicationContext, true, false, tokenService)
                Log.d("sync", "Location Sync adapter created.")
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return syncAdapter?.syncAdapterBinder
    }

    companion object {
        private val syncAdapterLocker = Any()
        private var syncAdapter: LocationSyncAdapter? = null
    }
}