package com.example.mayank.nfcapp

import android.app.Application
import android.arch.persistence.room.Room
import com.example.mayank.nfcapp.network.TokenService
import com.example.mayank.nfcapp.roomdatabase.NfcDatabase
import retrofit2.Retrofit

/**
 * Created by Mayank on 15/03/2018.
 */
class NfcApplication : Application() {

    private var mRetrofit: Retrofit? = null

    companion object {
        lateinit var database: NfcDatabase

    }

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(this, NfcDatabase::class.java, "nfc_database")
                .allowMainThreadQueries().build()


    }




}