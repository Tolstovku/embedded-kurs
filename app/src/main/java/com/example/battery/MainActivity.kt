package com.example.battery

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.battery.models.HeaterStatus
import com.example.battery.ui.map.MapsFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    var mLocationPermissionGranted: Boolean = false;
    lateinit var mMap: GoogleMap;
    lateinit var locationManager: LocationManager;
    var mLastKnownLocation: Location? = null;
    lateinit var mFusedLocationProviderClient: FusedLocationProviderClient;
    var heaterStatus: HeaterStatus = HeaterStatus.OFF;
    var heaterLocation: Location = Location("")
    lateinit var circleOptions: CircleOptions
    var circleRadius: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getHeaterLocation()

        //show geopos from start application
        supportFragmentManager.beginTransaction().add(R.id.map, MapsFragment(), "map").commit()

        // Construct a FusedLocationProviderClient DON'T KNOW WHAT IT DOES
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        setContentView(R.layout.activity_main)

        // Set up controller for Navigation
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_map, R.id.navigation_settings, R.id.navigation_about
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    // --- requests to server ----
    private fun getHeaterLocation() {
        val queue = Volley.newRequestQueue(applicationContext)
        val urlBase = "http://fly.sytes.net:8080"
        val urlAPI = "/GetHeaterLocation"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            "$urlBase$urlAPI",
            null,
            { response ->
                heaterLocation.latitude = response.get("x") as Double
                heaterLocation.longitude = response.get("y") as Double
            },
            { error ->
            }
        )
        queue.add(jsonObjectRequest)
    }

    private fun sendStop() {
        val queue = Volley.newRequestQueue(applicationContext)
        val urlBase = "http://fly.sytes.net:8080"
        val urlAPI = "/temperature/stop"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            "$urlBase$urlAPI",
            null,
            { response ->
                heaterStatus = HeaterStatus.OFF
            },
            { error ->
            }
        )
        queue.add(jsonObjectRequest)
    }

    private fun sendHold() {
        val queue = Volley.newRequestQueue(applicationContext)
        val urlBase = "http://fly.sytes.net:8080"
        val urlAPI = "/temperature/hold"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            "$urlBase$urlAPI",
            null,
            { response ->
                heaterStatus = HeaterStatus.ON
            },
            { error ->
            }
        )
        queue.add(jsonObjectRequest)
    }

    // --- Working with map ---
    override fun onMapReady(map: GoogleMap) {
        mMap = map
        initMap()
    }

    fun initMap() {
        updateLocationUI()
        // Get the current location of the device and set the position of the map.
        getDeviceLocation()
    }


    fun drawCircle(heaterLocation: Location): Unit {
        // Instantiating CircleOptions to draw a circle around the marker
        circleOptions = CircleOptions()

        // Specifying the center of the circle
        circleOptions.center(LatLng(heaterLocation.latitude, heaterLocation.longitude))
        println(heaterLocation.latitude)
        println(heaterLocation.longitude)

        // Radius of the circle
        circleOptions.radius(circleRadius)

        // Border color of the circle
        circleOptions.strokeColor(Color.BLACK)

        // Fill color of the circle
        circleOptions.fillColor(0x30ff0000)

        // Border width of the circle
        circleOptions.strokeWidth(2f)

        // Adding the circle to the GoogleMap
        mMap.addCircle(circleOptions)
    }

    // ------ COPY from devs.android from THIS to END -------
    private fun getLocationPermission() {
        /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    fun initLocationUpdate() {
        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //---------------Boiler plate code to supress SecutiyException --------------
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        // -----------------------------------------------
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            20,
            3f, fun(location: Location) {
                val distance = FloatArray(2)
                println("EVEXOXOXO")
                var prevHeaterStatus = heaterStatus;
                Location.distanceBetween(
                    location.latitude,
                    location.longitude,
                    circleOptions.center.latitude,
                    circleOptions.center.longitude,
                    distance
                );

                if (distance[0] > circleOptions.getRadius()) {
                    heaterStatus = HeaterStatus.OFF
                    // to server
                    sendStop()
                } else {
                    heaterStatus = HeaterStatus.ON
                    // to server
                    sendHold()
                }

                if (heaterStatus != prevHeaterStatus) {
                    val toast = Toast.makeText(
                        this,
                        "Status changed to %s".format(heaterStatus.toString()),
                        Toast.LENGTH_SHORT
                    )
                    toast.show()
                }
            }
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    mLocationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }

    private fun updateLocationUI() {
        try {
            if (mLocationPermissionGranted) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mMap.isMyLocationEnabled = false
                mMap.uiSettings.isMyLocationButtonEnabled = false
                mLastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            println(e)
        }
    }

    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                val locationResult = mFusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task: Task<Location> ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = task.result
                        if (mLastKnownLocation != null) {
                            mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        mLastKnownLocation!!.latitude,
                                        mLastKnownLocation!!.longitude
                                    ), 12.toFloat()
                                )
                            )
                        }
                    } else {
                        Log.d("XOXO", "Current location is null. Using defaults.")
                        Log.e("XOXO", "Exception: %s", task.exception)
                        mMap?.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(LatLng(45.0, 45.0), 1.toFloat())
                        )
                        mMap?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

}