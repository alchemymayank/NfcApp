package com.example.mayank.nfcapp.network

import retrofit2.Call
import retrofit2.http.*
import retrofit2.http.GET
import java.util.*


/**
 * Created by Mayank on 17/03/2018.
 */
interface ApiService {

//    @FormUrlEncoded
    @POST("StudentNfcLocation/AddStudentLocation")
    fun addStudentLocation(@Body data : NfcStudentLocations): Call<CommonResult>

    @POST("StudentNfcLocation/AddLocation")
    fun addLocation(@Body data : NfcStudentLocations): Call<Void>




    @GET("StudentNfcLocation/LoadStudentLocationById/{id}")
    fun getLocations(@Path("id") id: String): Call<NfcStudentLocations>


}