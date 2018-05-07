package com.example.mayank.nfcapp.sample

import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.example.mayank.nfcapp.Constants
import com.example.mayank.nfcapp.Constants.showLogDebug
import com.example.mayank.nfcapp.NfcApplication
import com.example.mayank.nfcapp.R
import com.example.mayank.nfcapp.network.TokenService
import com.example.mayank.nfcapp.nfcbus.IBusLocation
import com.example.mayank.nfcapp.nfcbus.NfcBusLocations
import com.example.mayank.nfcapp.roomdatabase.BusLocations
import com.example.mayank.nfcapp.roomdatabase.Converters
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * Created by Mayank on 4/9/2018.
 */
class SampleMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = SampleMapActivity::class.java.simpleName
    private lateinit var mMap: GoogleMap
    private val tokenService: TokenService by lazy { TokenService() }
    var markerPoints: ArrayList<LatLng>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        markerPoints = ArrayList<LatLng>()
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.setAllGesturesEnabled(true)
        mMap.isMyLocationEnabled = true

//        getLastLocation()

        getBusLocations()
    }

    private fun getBusLocations() {
        showLogDebug(TAG, "Inside Getting bus locations")
        val iBusLocation :IBusLocation = tokenService.getService()
        val call = iBusLocation.getNfcLocations()
        if (Constants.checkInternetConnection(this)){
            call.enqueue(object : Callback<List<NfcBusLocations>>{
                override fun onFailure(call: Call<List<NfcBusLocations>>?, t: Throwable?) {
                    showLogDebug(TAG, "Error : $t")
                }

                override fun onResponse(call: Call<List<NfcBusLocations>>?, response: Response<List<NfcBusLocations>>?) {
                    showLogDebug(TAG, "Response : $response")
                    showLogDebug(TAG, "Response Code : ${response?.code()}")
                    val list = response?.body()
                    if (list != null){

                        val latitude = list[0].latitude
                        val longitude = list[0].longitude
                        val sourceLatLng = LatLng(latitude?.toDouble()!!, longitude?.toDouble()!!)
                        mMap.addMarker(MarkerOptions().position(sourceLatLng).title("Source Location"))
                        for (data in list.withIndex()){
                            val sourceIndex = data.index
                            var destinationIndex : Int ? = null
                            if (data.index < list.size - 1){
                                destinationIndex = data.index + 1

                                val sourceLatitude = list[sourceIndex].latitude
                                val sourceLongitude = list[sourceIndex].longitude
                                val sourceLatLng = LatLng(sourceLatitude?.toDouble()!!, sourceLongitude?.toDouble()!!)

                                val destinationLatitude = list[destinationIndex].latitude
                                val destinationLongitude = list[destinationIndex].longitude
                                val destLatLng = LatLng(destinationLatitude?.toDouble()!!, destinationLongitude?.toDouble()!!)

                                val url = getUrl(sourceLatLng, destLatLng)
                                val fetchUrl = FetchUrl()
                                fetchUrl.execute(url)
                            }
                            showLogDebug(TAG, "Source Index : $sourceIndex Destination Index : $destinationIndex")
                        }

                        val destLat = list.last().latitude
                        val destLng = list.last().longitude
                        val destLatLng = LatLng(destLat?.toDouble()!!, destLng?.toDouble()!!)
                        mMap.addMarker(MarkerOptions().position(destLatLng).title("Destination Location"))
//                        for (data in list){
//                            val trackDate = data.trackDate
//                            val latitude = data.latitude
//                            val longitude = data.longitude
//                            showLogDebug(TAG, "Latitude : $latitude Longitude ; $longitude")
//                            val latLng = LatLng(latitude?.toDouble()!!, longitude?.toDouble()!!)
//                            markerPoints?.add(latLng)
//
//                        }
//
//                        mMap.addMarker(MarkerOptions().position(markerPoints!![0]).title("Source Location"))
//
//                        for (data in markerPoints?.withIndex()!!){
////                    showLogDebug(TAG, "Data :${data.index}")
//                            val source = data.index
//                            var destination : Int? = null
//                            if (data.index < markerPoints!!.size - 1){
//                                showLogDebug(TAG, "Destination size is greater than list size")
//                                destination = data.index + 1
//                            }
//                            showLogDebug(TAG, "Source : $source")
//                            showLogDebug(TAG, "Destination : $destination")
//                            if (destination!= null){
//
//                                val url = getUrl(markerPoints!![source], markerPoints!![destination])
//                                showLogDebug(TAG, "OnMapClick : Url : $url")
//
//                                val fetchUrl = FetchUrl()
//                                fetchUrl.execute(url)
//
//                            }else{
//                                showLogDebug(TAG, "Destination is null")
//                            }
//                        }
//
//                        mMap.addMarker(MarkerOptions().position(markerPoints!!.last()).title("Destination Location"))
//                        moveCamera(markerPoints!!.last())
                    }else {
                        val view = findViewById<View>(android.R.id.content)
                        Snackbar.make(view, "No location in database!!", Snackbar.LENGTH_LONG).show()
                    }
                }

            })
        }else{
            val view = findViewById<View>(android.R.id.content)
            Snackbar.make(view, "No Internet!", Snackbar.LENGTH_LONG).show()
        }

    }

    fun moveCamera(latLng: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        animateCamera(12F)
    }

    private fun animateCamera(zoomTo: Float) {
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomTo))
    }




    private fun getUrl(origin: LatLng, destination: LatLng): String {
        val waypoints = ("waypoints=optimize:true|"
                + origin.latitude + "," + origin.longitude
                + "|" + "|" + destination.latitude + ","
                + destination.longitude)
        val OriDest = "origin=" + origin.latitude + "," + origin.longitude + "&destination=" + destination.latitude + "," + destination.longitude

        val sensor = "sensor=false"
        val params = "$OriDest&%20$waypoints&$sensor"
        val output = "json"
        return ("https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params)
    }

    private inner class FetchUrl : AsyncTask<String, Void, String>(){
        override fun doInBackground(vararg url: String): String {
            showLogDebug(TAG, "Url Fetch ${url[0]}")
            var data = ""

            try {
                data = downloadUrl(url[0])
                showLogDebug(TAG, "FetchUrl : $data")
            }catch (e : Exception){
                showLogDebug(TAG, "FRetch Url Error : $e")
            }
            return data
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            showLogDebug(TAG, "Post Execute result $result")
            val parserTask = ParserTask()
            parserTask.execute(result)
        }
    }

    private inner class ParserTask : AsyncTask<String, Int, List<List<HashMap<String, String>>>>() {

        override fun doInBackground(vararg jsonData: String?): List<List<HashMap<String, String>>> {
            val jsonObject : JSONObject
            try {
                jsonObject = JSONObject(jsonData[0])
                showLogDebug(TAG, "Parser Task Json Data : ${jsonData[0]}")
                val parser = DataParser()
                showLogDebug(TAG, "ParserTask DataParser : $parser")

                var routes : List<List<HashMap<String, String>>> = parser.parse(jsonObject)
                showLogDebug(TAG, "Routes : $routes")
                return routes
            }catch (e : Exception){
                showLogDebug(TAG, "Parser Task Error : $e")
            }
            var r : List<List<HashMap<String, String>>> = ArrayList<ArrayList<HashMap<String, String>>>()
            return r

        }

        override fun onPostExecute(result: List<List<HashMap<String, String>>>?) {
            super.onPostExecute(result)
            var points : ArrayList<LatLng>
            var polyLineOptions : PolylineOptions? = null
            for (i in result?.indices!!){
                points = ArrayList<LatLng>()
                polyLineOptions = PolylineOptions()

                val path = result[i]
                for(j in path.indices){
                    val point = path[j]
                    val lat = java.lang.Double.parseDouble(point["lat"])
                    val lng = java.lang.Double.parseDouble(point["lng"])
                    val position = LatLng(lat, lng)
                    points.add(position)
                }

                polyLineOptions.addAll(points)
                polyLineOptions.width(15F)
                polyLineOptions.color(Color.BLUE)

                showLogDebug(TAG, "Polyline options decodes")
            }
            if (polyLineOptions!=null){
                mMap.addPolyline(polyLineOptions)
                showLogDebug(TAG, "Polyline added")
            }else{
                showLogDebug(TAG, "Without Polylines")
            }
        }

    }

    @Throws(IOException::class)
    private fun downloadUrl(url : String): String {
        var data =""
        var inputStream : InputStream? = null
        var httpUrlConnection : HttpURLConnection? =  null
        val stringBuffer = StringBuffer()
        try {
            val url = URL(url)

            httpUrlConnection = url.openConnection() as HttpURLConnection?
            httpUrlConnection?.connect()
            inputStream = httpUrlConnection?.inputStream
            val bufferReader = BufferedReader(InputStreamReader(inputStream))
            var line:String? = ""

            var read: String? = bufferReader.readLine()
            while (read != null) {
                stringBuffer.append(read)
                read = bufferReader.readLine()
            }

            data = stringBuffer.toString()
            showLogDebug(TAG, "Download Url : ${data}")
            bufferReader.close()
        }catch (e : Exception){
            showLogDebug(TAG, "DownloadUrl Error : $e")
        }finally {
            inputStream?.close()
            httpUrlConnection?.disconnect()
        }
        return data
    }
}