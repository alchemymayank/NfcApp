package com.example.mayank.nfcapp.nfcbus

import com.example.mayank.nfcapp.network.CommonResult
import com.example.mayank.nfcapp.network.NfcStudentLocations
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by Mayank on 27/03/2018.
 */
interface IBusLocation {

    @POST("BusLocation/AddBusLocation")
    fun addLocation(@Body data : NfcBusLocations): Call<Void>

    @POST("BusLocation/AddBusLocation")
    fun addCurrentLocation(@Body data : NfcBusLocations): Call<Void>

    @GET("BusLocation/GetAllNfcLocations")
    fun getNfcLocations(): Call<List<NfcBusLocations>>


    @GET("BusLocation/LoadBusLocationById/{id}")
    fun getLocations(@Path("id") id: String): Call<NfcBusLocations>

    @GET("BusLocation/GetLastKnownLocation")
    fun getLastKnownLocations(@Query("busId") busId: String): Call<NfcBusLocations>
}