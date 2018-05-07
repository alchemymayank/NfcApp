package com.example.mayank.nfcapp.sync

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Created by Mayank on 26/03/2018.
 */
class AuthenticatorService : Service() {
    private var authenticator: Authenticator? = null
    override fun onCreate() {
        super.onCreate()
        authenticator = Authenticator(this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return authenticator!!.iBinder
    }
}