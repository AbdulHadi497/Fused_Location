package com.example.fusedlocationapi

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*


class MainActivity() : AppCompatActivity() {
//    lateinit var btGetLoc : Button
//    lateinit var addressTxt : TextView
//    lateinit var lonLat: TextView

    val PERMISSION_ID = 42
    private var address: String? = null
    private var city: String? = null
    private var country: String? = null

    //val applicationScope = CoroutineScope(SupervisorJob())
    var database: AppDatabase? = null
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        database = AppDatabase.getInstance(this)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        btGetLocation.setOnClickListener {
            getLatLong()
            basicAlert()
        }
//        getLastLocation()
//        selectLong()
//        addressTxt = addressText
//        lonLat = longLat
//        btGetLoc = btGetLocation
//        btGetLocation.setOnClickListener {
//            val editText = longEdit
//            val address = editText.text.toString()
//            val locationAddress = GeoCoder()
//            locationAddress.getAddressFromLocation(address, applicationContext,
//                GeoCoderHandler(this))
//        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled() && isExternalStorageAvailable()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        latitudeTextView.text = location.latitude.toString()
                        longitudeTextView.text = location.longitude.toString()

                        insertDb()
//                        btDel.setOnClickListener {
//                            deleteDb()
//                        onLocationChanged(location)
//                        }

                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation
            latitudeTextView.text = mLastLocation?.latitude.toString()
            latitudeTextView.text = mLastLocation?.longitude.toString()
//            insertDb()
            Toast.makeText(applicationContext, "Turn on location", Toast.LENGTH_LONG).show()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    fun isExternalStorageAvailable(): Boolean {
        val extStorageState = Environment.getExternalStorageState()
        return if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            true
        } else {
            false
        }
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            PERMISSION_ID
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }

    fun insertDb() {
        val loc = Location()
        val userDao = database?.userDao()
        GlobalScope.launch {
            loc.latitude = latitudeTextView.text.toString()
            loc.longitude = longitudeTextView.text.toString()
            userDao?.insertAll(loc)
        }
    }
//    fun deleteDb(){
//        val delLoc = Location()
//        GlobalScope.launch {
//            val delDao = database?.userDao()
//            delLoc.userIds = idEdit.id
//            delDao?.delete(delLoc)
//        }
//    }

    fun getLatLong() {
        val uDao = database?.userDao()
        GlobalScope.launch {
            val model = uDao?.get(locationId.text.toString())
            runOnUiThread {
                model?.latitude?.let { model?.longitude?.let { it1 -> onLocationChanged(it, it1) } }
                Toast.makeText(
                    this@MainActivity,
                    "Location : Latitude ${model?.latitude} Longitude : ${model?.longitude}",
                    Toast.LENGTH_LONG
                ).show()
            }

        }
    }


//    companion object {
//        private class GeoCoderHandler(private val mainActivity: MainActivity) : Handler() {
//            override fun handleMessage(message: Message) {
//                val locationAddress: String?
//                locationAddress = when (message.what) {
//                    1 -> {
//                        val bundle = message.data
//                        bundle.getString("address")
//                    }
//                    else -> null
//                }
//                mainActivity.longLat.text = locationAddress
//            }
//        }
//    }

    fun onLocationChanged(latitude: String, longitude: String) {
        val geocoder: Geocoder
        val addresses: List<Address>?
        geocoder = Geocoder(this, Locale.getDefault())
//        var latitude = location.latitude
//        var longitude = location.longitude
        var latitude = latitude.toDouble()
        var longitude = longitude.toDouble()
        Log.e("latitude", "latitude--$latitude")
        try {
            Log.e("latitude", "inside latitude--$latitude")
            addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.size > 0) {
                address = addresses[0].getAddressLine(0)
                city = addresses[0].getLocality()
                country = addresses[0].getCountryName()
//                Toast.makeText(
//                    this,
//                    "locationTxt.setText(\"$address $city $country\")",
//                    Toast.LENGTH_SHORT
//                ).show()
                val builder = AlertDialog.Builder(this)

                with(builder)
                {
                    setTitle("Androidly Alert")
                    setMessage("\"$address $city $country\\\"")
                    //setPositiveButton("OK", DialogInterface.OnClickListener(function = positiveButtonClick))
                    // setNegativeButton(android.R.string.no, negativeButtonClick)
                    //setNeutralButton("Maybe", neutralButtonClick)
                    show()
                }

            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    fun basicAlert() {

//        val builder = AlertDialog.Builder(this)
//
//        with(builder)
//        {
//            setTitle("Androidly Alert")
//            setMessage("")
//            //setPositiveButton("OK", DialogInterface.OnClickListener(function = positiveButtonClick))
//           // setNegativeButton(android.R.string.no, negativeButtonClick)
//            //setNeutralButton("Maybe", neutralButtonClick)
//            show()
//        }

    }

    override fun onResume() {
        super.onResume()
        getLastLocation()
    }

    override fun onPause() {
        super.onPause()
    }
}


//12/08/2022
//package com.example.fusedlocationapi
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.content.pm.PackageManager
//import android.location.LocationManager
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationRequest
//import com.google.android.gms.location.LocationServices
//import kotlinx.android.synthetic.main.activity_main.*
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.SupervisorJob
//import kotlinx.coroutines.launch
//
//class MainActivity : AppCompatActivity() {
//
//    val database by lazy { userDb.getDatabase(this) }
//
//
//    var db = UserDb
//    //var GlobalScope = CoroutineScope(SupervisorJob())
//    val permissionId = 55
//
//    //lateinit var mFusedLocationClient : FusedLocationProviderClient
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        //  mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        //getLastLocation()
//        btGetLocation.setOnClickListener {
//            // getLastLocation()
//            insertDB()
//        }
//    }
//
//
//    ////Will CHECK USER ALLOWED OR DENIED COARSE AND FINE LOCATION
//
//    private fun permissionChecker():
//            Boolean {
//        if (
//            ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//            &&
//            ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            return true
//        }
//        return false
//    }
//
//
//    ////WILL ASK USER FOR ALLOW TO APP FINE AND COARSE LOCATION
//
//
//    private fun requestPermission() {
//        ActivityCompat.requestPermissions(
//            this,
//            arrayOf(
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ),
//            permissionId
//        )
//    }
//
//
//    ////OVERRIDDEN FUNCTION TO HELP US MOVE FORWARD AFTER USER ACCEPTION OR DENIAL
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permission: Array<String>,
//        grantResult: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permission, grantResult)
//        if (
//            requestCode == permissionId
//        )
//            if ((grantResult.isNotEmpty() && grantResult[0] == PackageManager.PERMISSION_GRANTED)) {
//            }
//    }
//
//    private fun locationEnabled(): Boolean {
//        val locationManager: LocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
//        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
//            LocationManager.NETWORK_PROVIDER
//        )
//    }
//
//
//    ////LOOK AT API BEING USED AND LOOK AT PERMISSION AGAINST IT IN THE CODE TO ACCESS THIS API
////    @SuppressLint("MissingPermission")
////    private fun requestNewLocationData(){
////        val mLocationRequest = LocationRequest.create()
////        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
////        mLocationRequest.interval = 0
////        mLocationRequest.fastestInterval = 0
////        mLocationRequest.numUpdates = 1
////
////        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//    // mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper() )
//
//
//    ////OBJECT FOR CALLBACK
////    private val mLocationCallback = object : LocationCallback(){
////        override fun onLocationResult(locationResult: LocationResult) {
////            val mLastLocation: Location? = locationResult.lastLocation
////            if (mLastLocation != null) {
////                latitudeTextView.text = mLastLocation.latitude.toString()
////                longitudeTextView.text = mLastLocation.longitude.toString()
////                insertDB(mLastLocation)
////
////
////
////            }
////
////        }
////
////    }
//
//
//    ////LOOK AT API BEING USED AND LOOK AT PERMISSION AGAINST IT IN THE CODE TO ACCESS THIS API
//    // @SuppressLint("MissingPermission")
//
//    ////FOR ACCESSING THE LAST LOCATION OF USER
//
////    private fun getLastLocation(){
////        if (permissionChecker()){
////            if (locationEnabled())
////            {
////                mFusedLocationClient.lastLocation.addOnCompleteListener(this) {
////                    task -> val location : Location? = task.result
////                    if (location == null){
////                        requestNewLocationData()
////                    }
////                    else {
////                        location.longitude = longitudeTextView.text.toString().toDouble()
////                        location.latitude = latitudeTextView.text.toString().toDouble()
////                    }
////                }
////            }
////            else{
////                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
////                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
////                startActivity(intent)
////            }
////        }
////        else{
////            requestPermission()
////        }
////    }
//
//    fun insertDB() {
//        // val loc = Location()
//        GlobalScope.launch {
//            val userDao = database?.userDao()
//            val location = Location()
//
//            location.latitude = longitudeTextView.text.toString()
//            location.longitude = latitudeTextView.text.toString()
//            // db.instance?.user()?.insertAll(loc)
//            userDao?.insertAll(location)
//        }
//
//    }
//
//}