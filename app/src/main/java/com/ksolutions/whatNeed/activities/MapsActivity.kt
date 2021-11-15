package com.ksolutions.whatNeed.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.ksolutions.whatNeed.R
import com.ksolutions.whatNeed.utils.Constants
import com.ksolutions.whatNeed.utils.DirectionsJSONParser
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

@Suppress("DEPRECATION")
class MapsActivity : BaseActivity(), OnMapReadyCallback {

    private var businessLatitude: Double = 0.0
    private var businessLongitude: Double = 0.0
    private var businessTitle: String = ""

    private lateinit var mLocationManager: LocationManager
    private lateinit var mLocationListener: LocationListener
    private lateinit var mMarkerOptions: MarkerOptions
    private var mOrigin: LatLng? = null
    private var mDestination: LatLng? = null
    private var mPolyline: Polyline? = null

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map_vendor_home) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (intent.hasExtra(Constants.LATITUDE)) {
            businessLatitude = intent.getDoubleExtra(Constants.LATITUDE,0.0)!!
        }
        if (intent.hasExtra(Constants.LONGITUDE)) {
            businessLongitude = intent.getDoubleExtra(Constants.LONGITUDE,0.0)!!
        }
        if(intent.hasExtra(Constants.BUSINESS_NAME)){
            businessTitle = intent.getStringExtra(Constants.BUSINESS_NAME)!!
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        getMyLocation()
    }

    private fun getMyLocation()
    {
        mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        mLocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                mOrigin = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mOrigin!!, 12f))

                mDestination = LatLng(businessLatitude,businessLongitude)

                if (mOrigin != null && mDestination != null) {
                    //showErrorSnackBar(mDestination.toString(),false)
                    mMap.addMarker(MarkerOptions().position(mOrigin!!).title("You are here"))
                    mMap.addMarker(MarkerOptions().position(mDestination!!).title(businessTitle))
                    drawRoute()
                }
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }
        val currentApiVersion = Build.VERSION.SDK_INT

        if (currentApiVersion >= Build.VERSION_CODES.M)
        {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_DENIED) {
                mMap.isMyLocationEnabled = true
                mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000,
                    0f,
                    mLocationListener
                )
                /*mMap.setOnMapLongClickListener { latLng ->
                    mDestination = latLng
                    mMap.clear()
                    mMarkerOptions = MarkerOptions().position(mDestination!!).title("Destination")
                    mMap.addMarker(mMarkerOptions)
                    if (mOrigin != null && mDestination != null) {
                        mMap.addMarker(MarkerOptions().position(mOrigin!!).title("Origin"))
                        mMap.addMarker(MarkerOptions().position(mDestination!!).title("Destination"))
                        //route()
                        drawRoute()
                    }
                }*/
            } else {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), 100
                )
            }
        }
    }

    private fun drawRoute()
    {
        val url: String = getDirectionsUrl(mOrigin, mDestination)

        val downloadTask = DownloadTask()

        downloadTask.execute(url)
    }

    private fun getDirectionsUrl(origin: LatLng?, dest: LatLng?): String
    {
        // Origin of route
        val str_origin = "origin=" + origin?.latitude + "," + origin?.longitude

        // Destination of route
        val str_dest = "destination=" + dest?.latitude + "," + dest?.longitude

        val mode = "mode=driving"

        // Key
        val key = "key=" + getString(R.string.google_maps_key)

        // Building the parameters to the web service
        val parameters = "$str_origin&$str_dest&$mode&$key"

        // Output format
        val output = "json"

        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters"
    }

    @Throws(IOException::class)
    private fun downloadUrl(strUrl: String?): String {
        var data = ""
        var iStream: InputStream? = null
        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(strUrl)

            // Creating an http connection to communicate with url
            urlConnection = url.openConnection() as HttpURLConnection

            // Connecting to url
            urlConnection.connect()

            // Reading data from url
            iStream = urlConnection!!.inputStream
            val br = BufferedReader(InputStreamReader(iStream))
            val sb = StringBuffer()
            var line: String? = ""
            while (br.readLine().also { line = it } != null) {
                sb.append(line)
            }
            data = sb.toString()
            br.close()
        } catch (e: Exception) {
            Log.d("Exception on download", e.toString())
        } finally {
            iStream!!.close()
            urlConnection!!.disconnect()
        }
        return data
    }

    /** A class to download data from Google Directions URL  */
    inner class DownloadTask : AsyncTask<String?, Void?, String>() {

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            val parserTask = ParserTask()

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result)
        }

        override fun doInBackground(vararg url: String?): String {
            var data = ""
            try {
                // Fetching the data from web service
                data = downloadUrl(url[0].toString())
                Log.d("DownloadTask", "DownloadTask : $data")
            } catch (e: java.lang.Exception) {
                Log.d("Background Task", e.toString())
            }
            return data
        }
    }

    /** A class to parse the Google Directions in JSON format  */
    inner class ParserTask :
        AsyncTask<String?, Int?, List<List<HashMap<String, String>>>?>() {

        override fun onPostExecute(result: List<List<HashMap<String, String>>>?) {
            val points = ArrayList<LatLng?>()
            val lineOptions = PolylineOptions()

            // Traversing through all the routes
            for (i in result!!.indices) {
                // Fetching i-th route
                val path = result[i]

                // Fetching all the points in i-th route
                for (j in path.indices) {
                    val point = path[j]
                    val lat = point["lat"]!!.toDouble()
                    val lng = point["lng"]!!.toDouble()
                    val position = LatLng(lat, lng)
                    points.add(position)
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points)
                lineOptions.width(8f)
                lineOptions.color(Color.RED)
                lineOptions.geodesic(true)
            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null)
            {
                mPolyline?.remove()
                mPolyline = mMap.addPolyline(lineOptions)
            }
            else {
                showErrorSnackBar("No Route Found", true)
            }
        }

        override fun doInBackground(vararg jsonData: String?): List<List<HashMap<String, String>>>? {
            val jObject: JSONObject
            var routes: List<List<HashMap<String, String>>>? = null
            try {
                jObject = JSONObject(jsonData[0])
                val parser = DirectionsJSONParser()

                // Starts parsing data
                routes = parser.parse(jObject)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return routes
        }
    }
}