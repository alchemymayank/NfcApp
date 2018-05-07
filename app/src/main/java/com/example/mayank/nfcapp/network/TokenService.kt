package com.example.mayank.nfcapp.network

import com.example.mayank.nfcapp.Constants
import com.example.mayank.nfcapp.Constants.API_BASE_ADDRESS
import com.example.mayank.nfcapp.Constants.CONNECTION_TIMEOUT
import com.example.mayank.nfcapp.Constants.READ_TIMEOUT
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.google.gson.Gson



/**
 * Created by Mayank on 17/03/2018.
 */
class TokenService {

    @PublishedApi
    internal var retrofit: Retrofit

    init {

        val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create()

        val httpClient = OkHttpClient.Builder()
                .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(Constants.CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .build()

        retrofit = Retrofit.Builder()
                .baseUrl(API_BASE_ADDRESS)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient)
                .build()
    }

    inline fun <reified T> getService(): T {
        return retrofit.create(T::class.java)
    }

    fun <T> getService(service: Class<T>): T {
        return retrofit.create(service)
    }
}