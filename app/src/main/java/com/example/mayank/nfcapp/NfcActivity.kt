package com.example.mayank.nfcapp

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.accounts.Account
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.nfc.*
import android.nfc.tech.Ndef
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.mayank.nfcapp.Constants.ACCOUNT_AUTHORITY_BUS_LOCATION
import com.example.mayank.nfcapp.Constants.DEFAULT_SYNC_STATE
import com.example.mayank.nfcapp.Constants.showLogDebug
import com.example.mayank.nfcapp.locationservice.LocationHelper
import com.example.mayank.nfcapp.locationservice.StartBusLocationActivity
import com.example.mayank.nfcapp.map.MapsActivity
import com.example.mayank.nfcapp.newlocationservice.StartBusLocationServiceActivity
import com.example.mayank.nfcapp.roomdatabase.Converters
import com.example.mayank.nfcapp.roomdatabase.NfcLocations
import com.example.mayank.nfcapp.sample.SampleMapActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.Api
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util.*
import kotlin.experimental.and

class NfcActivity : AppCompatActivity() {


    private val TAG = NfcActivity::class.java.simpleName

    internal lateinit var nfcAdapter: NfcAdapter
    internal lateinit var infoContent: TextView
    internal var myTag: Tag? = null
    internal var inputTag: EditText? = null
    private var textViewInfo: TextView? = null

    private var textViewLat: TextView? = null
    private var textViewLng: TextView? = null
    private var textViewTrackDate: TextView? = null


    private val MIME_TEXT_PLAIN = "text/plain"
    private lateinit var detectedTag: Tag;


    val RequestPermissionCode = 1
    private var googleApiClient: GoogleApiClient? = null

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocationRequest: LocationRequest? = null

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    var tagId: String? = null
    private val PERMISSIONS_REQUESTS = 0x1

    private val SYNC_INTERVAL = 10L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc)

        val mAccount = CreateSyncAccount(this)
        ContentResolver.setSyncAutomatically(mAccount, Constants.ACCOUNT_AUTHORITY_LOCATION, true)
//        ContentResolver.addPeriodicSync(mAccount, ACCOUNT_AUTHORITY_BUS_LOCATION, Bundle.EMPTY, SYNC_INTERVAL)
        ContentResolver.setSyncAutomatically(mAccount, Constants.ACCOUNT_AUTHORITY_BUS_LOCATION, true)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        //locationHelper = LocationHelper(this, NfcApplication.database)

        textViewInfo = findViewById<TextView>(R.id.text_view_tag);
        textViewLat = findViewById<TextView>(R.id.text_view_latitude)
        textViewLng = findViewById<TextView>(R.id.text_view_longitude)
        textViewTrackDate = findViewById<TextView>(R.id.text_view_track_date)

        textViewLat?.visibility = View.GONE
        textViewLng?.visibility = View.GONE
        textViewTrackDate?.visibility = View.GONE
        inputTag = findViewById<android.support.design.widget.TextInputEditText>(R.id.edit_text_tag) as EditText

        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is disable", Toast.LENGTH_SHORT).show()
        } else {
            showLogDebug(TAG, "NFC is enable")
//            checkLocationPermissions()

        }
    }

    @SuppressLint("MissingPermission")
    fun CreateSyncAccount(context: Context): Account? {
        showLogDebug(TAG, "Inside Create account")
        // Create the account type and default account
        val newAccount = Account(Constants.ACCOUNT_NAME, Constants.ACCOUNT_TYPE)
        val accountManager = context.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            showLogDebug(TAG, "Inside permission granted")
            showLogDebug(TAG, "New Account : $newAccount")
            return newAccount
        }
        val accounts = accountManager.getAccountsByType(Constants.ACCOUNT_TYPE)
        if (accounts.isNotEmpty()) {
            Log.d(TAG, "Account ${Constants.ACCOUNT_NAME} already exists.")
            return newAccount
        }
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            Log.d(TAG, "Account ${Constants.ACCOUNT_NAME} added successfully.")
        }

        return newAccount
    }


    private fun checkLocationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS, Manifest.permission.GET_ACCOUNTS),
                        PERMISSIONS_REQUESTS)

            } else {
                checkLocationSettings()
            }
        } else {
            checkLocationSettings()
        }
    }


    @SuppressLint("RestrictedApi")
    private fun checkLocationSettings() {
        try {
            if (!checkPlayServices()) {
                return
            }
            // Building the GoogleApi client
            mGoogleApiClient = GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API).build()
            mGoogleApiClient?.connect()

            mLocationRequest = LocationRequest()
            mLocationRequest?.interval = NfcActivity.UPDATE_INTERVAL.toLong()
            mLocationRequest?.fastestInterval = NfcActivity.FATEST_INTERVAL.toLong()
            mLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            val builder = LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest!!)
            builder.setAlwaysShow(true)
            val result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                    builder.build())

            result.setResultCallback { locationSettingsResult ->
                val status = locationSettingsResult.status
                //final LocationSettingsStates locationSettingsStates = locationSettingsResult.getLocationSettingsStates();
                when (status.statusCode) {
                    LocationSettingsStatusCodes.SUCCESS ->
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
//                        startLocationService(true)

                        showLogDebug(TAG, "Location enabled!")


//                        if (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED){
//                            showLogDebug(TAG, "Inside success")
//                            ndefReader(intent)
//                        }
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    this@NfcActivity,
                                    NfcActivity.REQUEST_CHECK_SETTINGS)
                        } catch (e: IntentSender.SendIntentException) {
                            // Ignore the error.
                        }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        Toast.makeText(this@NfcActivity, "Couldn't find the location services.", Toast.LENGTH_LONG).show()
                }
            }
        } finally {
            if (mGoogleApiClient != null && mGoogleApiClient?.isConnected!!) {
                mGoogleApiClient?.disconnect()
            }
            mLocationRequest = null
        }
    }


    private fun checkPlayServices(): Boolean {
        val resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        NfcActivity.PLAY_SERVICES_RESOLUTION_REQUEST).show()
            } else {
                Toast.makeText(applicationContext,
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show()
            }
            return false
        }
        return true
    }

    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
        // Check for the integer request code originally supplied to startResolutionForResult().
            REQUEST_CHECK_SETTINGS -> when (resultCode) {
                Activity.RESULT_OK ->
                    showLogDebug(TAG, "All permission enabled!")
//                    if (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED){
//                    showLogDebug(TAG, "Inside success")
////                    ndefReader(intent)
//                }
                Activity.RESULT_CANCELED -> checkLocationSettings()//keep asking
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showLogDebug(TAG, "On resume called")
        if (nfcAdapter!=null){
            showLogDebug(TAG, "Ngc adapter is not null")
        }else{
            showLogDebug(TAG, "Nfc adapter is null")
        }
        setupForegroundDispatch(this, nfcAdapter)
        checkLocationPermissions()
    }

    fun setupForegroundDispatch(activity: Activity, adapter: NfcAdapter?) {
        val intent = Intent(activity.applicationContext, activity.javaClass)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

        val pendingIntent = PendingIntent.getActivity(activity.applicationContext, 0, intent, 0)

        val filters = arrayOfNulls<IntentFilter>(2)
        val techList = arrayOf<Array<String>>()

        // Notice that this is the same filter as in our manifest.
        filters[0] = IntentFilter()
        filters[0]!!.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED)
        filters[0]!!.addCategory(Intent.CATEGORY_DEFAULT)
        filters[1] = IntentFilter()
        filters[1]!!.addAction(NfcAdapter.ACTION_TECH_DISCOVERED)
        try {
            filters[0]!!.addDataType(MIME_TEXT_PLAIN)
        } catch (e: IntentFilter.MalformedMimeTypeException) {
            throw RuntimeException("Check your mime type.")
        }

        adapter!!.enableForegroundDispatch(activity, pendingIntent, filters, techList)
    }

    override fun onStart() {
        super.onStart()
        showLogDebug(TAG, "On Start Called")
    }

    override fun onPause() {
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this)
        }
        super.onPause()
    }


    override fun onNewIntent(intent: Intent) {
        showLogDebug(TAG, "On New Intent")
        ndefReader(intent)
    }

    private fun ndefReader(intent: Intent) {
        detectedTag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        val techList = detectedTag.techList
        val searchedTech = Ndef::class.java.name

        for (tech in techList) {
            if (searchedTech == tech) {
                NdefReaderTask().execute(detectedTag)
                break
            }
        }
    }

    private inner class NdefReaderTask : AsyncTask<Tag, Void, String>() {

        override fun doInBackground(vararg params: Tag): String? {
            val tag = params[0]

            val ndef = Ndef.get(tag)
                    ?: // NDEF is not supported by this Tag.
                    return null

            val ndefMessage = ndef.cachedNdefMessage ?: return null

            val records = ndefMessage.records
            for (ndefRecord in records) {
                if (ndefRecord.tnf == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.type, NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord)
                    } catch (e: UnsupportedEncodingException) {
                        Log.e("LOG", "Unsupported Encoding", e)
                    }

                }
            }

            return null
        }

        @Throws(UnsupportedEncodingException::class)
        private fun readText(record: NdefRecord): String {
            /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

            val payload = record.payload

            // Get the Text Encoding
            //val textEncoding = if (payload[0] and 128) == 0) "UTF-8" else "UTF-16"

            // Get the Language Code
            val languageCodeLength = payload[0] and 51

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, Charsets.UTF_8)
        }

        override fun onPostExecute(result: String?) {
            if (result != null) {
                tagId = result
                textViewInfo!!.text = "NFC Id : $tagId"
                showLogDebug(TAG, "Content : $tagId")

                getLocations()
            }
        }
    }


    fun writeTag(view: View) {
        Constants.showLogDebug(TAG, "Write tag button created")
        val tag = inputTag?.text.toString().trim({ it <= ' ' })
        writeNfcTag(tag)
        showLogDebug(TAG, "Tag write successfully")
        Toast.makeText(this, "Id Tag write successfully", Toast.LENGTH_SHORT)
    }

    private fun writeNfcTag(text: String) {
        val intent = intent
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            try {
                ndef.connect()
                val mimeRecord = createRecord(text)//NdefRecord.createMime("text/plain", text.getBytes(Charset.forName("US-ASCII")));
                ndef.writeNdefMessage(NdefMessage(mimeRecord))
                ndef.close()
            } catch (e: IOException) {
                Constants.showLogDebug(TAG, "IOException $e")
                Toast.makeText(this, "Attach Id card and try again!", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } catch (e: FormatException) {
                Constants.showLogDebug(TAG, "Format Exception $e")
                e.printStackTrace()
            }

        } else {
            showLogDebug(TAG, "Ndef is null")
        }

    }

    @Throws(UnsupportedEncodingException::class)
    private fun createRecord(text: String): NdefRecord {
        val lang = "en"
        val textBytes = text.toByteArray()
        val langBytes = lang.toByteArray(charset("US-ASCII"))
        val langLength = langBytes.size
        val textLength = textBytes.size
        val payload = ByteArray(1 + langLength + textLength)

        // set status byte (see NDEF spec for actual bits)
        payload[0] = langLength.toByte()

        // copy langbytes and textbytes into payload
        System.arraycopy(langBytes, 0, payload, 1, langLength)
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength)

        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, ByteArray(0), payload)
    }


    @SuppressLint("MissingPermission")
    private fun getLocations() {
        showLogDebug(TAG, "Inside get location...")
        fusedLocationProviderClient.lastLocation!!
                .addOnSuccessListener(this) { location ->
                    showLogDebug(TAG, "Inside on success")

                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        showLogDebug(TAG, "User Id Tag : $tagId")
                        showLogDebug(TAG, "Latitude is : " + location.latitude)
                        showLogDebug(TAG, "Longitude is : " + location.longitude)
                        showLogDebug(TAG, "Location time : " + Calendar.getInstance().time)
                        textViewLat?.text = "Latitude : ${location.latitude}"
                        textViewLng?.text = "Longitude : ${location.longitude}"
                        textViewTrackDate?.text = "Track Date : ${Calendar.getInstance().time}"
                        textViewLat?.visibility = View.VISIBLE
                        textViewLng?.visibility = View.VISIBLE
                        textViewTrackDate?.visibility = View.VISIBLE

                        val nfcData = NfcLocations()
                        nfcData.nfcId = tagId
                        nfcData.latitude = location.latitude.toString()
                        nfcData.longitude = location.longitude.toString()
                        nfcData.trackTime = Converters.dateToTimestamp(Calendar.getInstance().time)
                        nfcData.syncState = DEFAULT_SYNC_STATE
                        showLogDebug(TAG, "Sync state : ${nfcData.syncState}")
                        try {
                            NfcApplication.database.nfcLocationDao().insert(nfcData)
                            showLogDebug(TAG, "Data inserted successfully")
                        } catch (e: Exception) {
                            showLogDebug(TAG, "Exception : $e")
                        }


//                        val list  = NfcApplication.database.nfcLocationDao().getAllLocation()
//
//                        for (data in list){
//                            showLogDebug(TAG, "Tag Id : "+ data.nfcId)
//                            showLogDebug(TAG, "Latitude : "+ data.latitude)
//                            showLogDebug(TAG, "Longitude :"+ data.longitude)
//                            showLogDebug(TAG, "Time : "+ Converters.fromTimestamp(data.trackTime))
//                        }
                    } else {
                        showLogDebug(TAG, "Location is null")
                        Toast.makeText(this, "Location is Null", Toast.LENGTH_SHORT).show()
                    }
                }
    }


    fun startLocationService(view: View) {
        showLogDebug(TAG, "Start Location service button clicked")
        val intent = Intent(this, StartBusLocationServiceActivity::class.java)
        startActivity(intent)
    }

    fun openMap(view: View) {
        showLogDebug(TAG, "Open map button clicked")
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }

    fun openMapWithRoutes(view: View){
        showLogDebug(TAG, "Open mapo with routes button clicked")
        val intent = Intent(this, SampleMapActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private val PLAY_SERVICES_RESOLUTION_REQUEST = 1000
        private val REQUEST_CHECK_SETTINGS = 209

        // Location updates intervals in sec
        private val UPDATE_INTERVAL = 10000 // 10 sec
        private val FATEST_INTERVAL = 5000 // 5 sec
    }
}
