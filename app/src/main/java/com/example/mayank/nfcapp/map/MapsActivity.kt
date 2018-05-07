package com.example.mayank.nfcapp.map

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.mayank.nfcapp.Constants.checkInternetConnection
import com.example.mayank.nfcapp.Constants.showLogDebug
import com.example.mayank.nfcapp.NfcApplication
import com.example.mayank.nfcapp.R
import com.example.mayank.nfcapp.locationservice.LocationService
import com.example.mayank.nfcapp.network.TokenService
import com.example.mayank.nfcapp.nfcbus.IBusLocation
import com.example.mayank.nfcapp.nfcbus.NfcBusLocations
import com.example.mayank.nfcapp.roomdatabase.BusLocations

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import org.jetbrains.anko.contentView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG: String = MapsActivity::class.java.simpleName

    private lateinit var mMap: GoogleMap
    private val tokenService: TokenService by lazy { TokenService() }

    var handler = Handler()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        handler = Handler()

//        getDatabaseList()
    }

    private val runnableCode = Runnable {
        // Do something here on the main thread
        Log.d("Handlers", "Called on main thread")
        createList()

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.setAllGesturesEnabled(true)
        mMap.isMyLocationEnabled = true

        //createList()
        handler.post(runnableCode)

        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(11F))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun onResume() {
        super.onResume()
        showLogDebug(TAG, "On resume called")
    }

    override fun onPause() {
        super.onPause()
        showLogDebug(TAG, "On Pause Called")
        handler.removeCallbacks(runnableCode)
    }

    private var moveCamera : Boolean = false


    private fun createList() {
        if (checkInternetConnection(this)) {
            val myArr = ArrayList<LatLng>()
            showLogDebug(TAG, "Inside get database list")
            val iBusLocation: IBusLocation = tokenService.getService()
            val call = iBusLocation.getNfcLocations()
            call.enqueue(object : Callback<List<NfcBusLocations>> {
                override fun onFailure(call: Call<List<NfcBusLocations>>?, t: Throwable?) {
                    showLogDebug(TAG, "Error $t")
                }

                override fun onResponse(call: Call<List<NfcBusLocations>>?, response: Response<List<NfcBusLocations>>?) {
                    showLogDebug(TAG, "" + response)
                    showLogDebug(TAG, "" + response?.body())
                    val list = response?.body()
                    for (data in list!!) {
//                    showLogDebug(TAG, "Id : ${data.id}")
//                    showLogDebug(TAG, "Bus Id : ${data.busId}")
//                    showLogDebug(TAG, "Latitude : ${data.latitude}")
//                    showLogDebug(TAG, "Longitude : ${data.longitude}")
//                    showLogDebug(TAG, "Track Date : ${data.trackDate}")
                        val latLng = LatLng(data.latitude?.toDouble()!!, data.longitude?.toDouble()!!)
                        myArr.add(latLng)
                    }
                    showLogDebug(TAG, "Array List Added")
                    drawPolyLines(myArr)
                    if (!myArr.isEmpty()){
                        addMarker(myArr[0], "Bus Starting Point")
//                        addMarker(myArr.last(), "Last Known Location")
//                        if (!moveCamera){
////                            moveCamera(myArr.last())
//                            moveCamera = true
//
//                        }
                        getBusLastLocation()
                    }else{
                        Toast.makeText(applicationContext, "No Coordinates in database to show", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        } else {
            val view = findViewById<View>(android.R.id.content)
            Snackbar.make(view, "No Internet!", Snackbar.LENGTH_LONG).show()
        }



    }

    private fun getBusLastLocation() {
        showLogDebug(TAG, "Inside get bus last known location")
        val service : IBusLocation = tokenService.getService()
        val call = service.getLastKnownLocations("1234")
        call.enqueue(object : Callback<NfcBusLocations>{
            override fun onFailure(call: Call<NfcBusLocations>?, t: Throwable?) {
                showLogDebug(TAG, "Error $t")
            }

            override fun onResponse(call: Call<NfcBusLocations>?, response: Response<NfcBusLocations>?) {
                showLogDebug(TAG, "Response : $response")
                showLogDebug(TAG, "Response body : ${response?.body()}")
                showLogDebug(TAG, "Response Code : ${response?.code()}")
                val list = response?.body()
                val latLng = LatLng(list?.latitude?.toDouble()!!, list.longitude?.toDouble()!!)
                drawPolyLines(latLng)
                addMarker(latLng, "Bus Last Known Location")
                if (!moveCamera){
                    moveCamera(latLng)
                    moveCamera = true
                }
            }

        })

        handler.postDelayed(runnableCode, 10000)


    }

    fun drawPolyLines(latLng: LatLng){
        val polyLineOptions = PolylineOptions()
        polyLineOptions.color(Color.RED)
        polyLineOptions.width(15F)
        polyLineOptions.geodesic(true)
        polyLineOptions.add(latLng)
        mMap.addPolyline(polyLineOptions)
    }

    fun drawPolyLines(list: List<LatLng>) {
        val polyLineOptions = PolylineOptions()
        polyLineOptions.color(Color.BLUE)
        polyLineOptions.width(15F)
        polyLineOptions.geodesic(true)
        polyLineOptions.addAll(list)
        mMap.clear()
        mMap.addPolyline(polyLineOptions)
    }

    fun drawLines(list: List<LatLng>) {
        val polyLineOptions = PolylineOptions()
        polyLineOptions.color(Color.BLUE);
        polyLineOptions.width(15F);
        polyLineOptions.geodesic(true)

        polyLineOptions.addAll(list);

        mMap.clear();
        mMap.addPolyline(polyLineOptions);
//        mMap.addMarker(MarkerOptions().position(list[0]).title("Marker Start"))
        addMarker(list[0], "Marker Start")
//        addMarker(list.last(), "Marker End")
        moveCamera(list.last())
        animateCamera(15F)
//        mMap.addMarker(MarkerOptions().position(list.last()).title("Marker End"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(list[0]))
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(15F))
        handler.postDelayed(runnableCode, 10000)


    }


    fun getDatabaseList() {
        showLogDebug(TAG, "Inside get database list")
        val iBusLocation: IBusLocation = tokenService.getService()
        val call = iBusLocation.getNfcLocations()
        call.enqueue(object : Callback<List<NfcBusLocations>> {
            override fun onFailure(call: Call<List<NfcBusLocations>>?, t: Throwable?) {
                showLogDebug(TAG, "Error $t")
            }

            override fun onResponse(call: Call<List<NfcBusLocations>>?, response: Response<List<NfcBusLocations>>?) {
                showLogDebug(TAG, "" + response)
                showLogDebug(TAG, "" + response?.body())
                val list = response?.body()
                for (data in list!!) {
//                    showLogDebug(TAG, "Id : ${data.id}")
                    showLogDebug(TAG, "Bus Id : ${data.busId}")
                    showLogDebug(TAG, "Latitude : ${data.latitude}")
                    showLogDebug(TAG, "Longitude : ${data.longitude}")
                    showLogDebug(TAG, "Track Date : ${data.trackDate}")


                }
            }

        })
    }

    fun addMarker(latLng: LatLng, title: String) {
        mMap.addMarker(MarkerOptions().position(latLng).title(title))
    }

    fun moveCamera(latLng: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        animateCamera(12F)
    }

    fun animateCamera(zoomTo: Float) {
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomTo))
    }


}
