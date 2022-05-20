package com.ksolutions.whatNeed.ui.fragments.vendorHome

import android.Manifest
import com.ksolutions.whatNeed.R
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import butterknife.ButterKnife
import butterknife.Unbinder
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ksolutions.whatNeed.ui.fragments.BaseFragment
import com.ksolutions.whatNeed.utils.Constants
import com.ksolutions.whatNeed.utils.PublicValues
import java.util.*

class VendorHomeFragment : BaseFragment(){

    private var currentLat = 0.0
    private var currentLong = 0.0
    private var currentCity: String? = PublicValues.userCurrentCity
    private var currentPostalCode: String? = PublicValues.userPostalCode

    private lateinit var userLocation:LatLng
    private var mLocation: Location? = null
    private  var mMap: GoogleMap?=null
    private var marker: Marker?=null

    private lateinit var mLocationManager: LocationManager
    private lateinit var mLocationListener: LocationListener

    private  var onlineRef: DatabaseReference?=null
    private var currentUserRef: DatabaseReference?=null
    private lateinit var vendorLocationRef: DatabaseReference
    private var geoFire: GeoFire?=null

    val TAB_POSITION = 0
    private var mUnbinder: Unbinder? = null


    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        /*val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/
        mMap = googleMap
        if(currentCity!=null)
            registerOnlineSystem()

        applyMapStyle()
        moveToLastLocation()
    }

    private fun  applyMapStyle()
    {
        try{
            val style = mMap!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.maps_style))

            if(!style)
                Log.e("Style Error", "Style parsing Error")
        }
        catch (e: Resources.NotFoundException) {
            e.message?.let { Log.e("Style Exception", it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_vendor_home, container, false)

        mUnbinder = ButterKnife.bind(this, rootView)

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mUnbinder != null) {
            mUnbinder!!.unbind()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_vendor_home) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private val onlineValueEventListener = object: ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
            if(snapshot.exists())
            {
                currentUserRef!!.onDisconnect().removeValue()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            showErrorSnackBar(error.toString(),true)
        }
    }

    private fun registerOnlineSystem() {
        if(onlineRef != null)
            onlineRef!!.addValueEventListener(onlineValueEventListener)
    }


    companion object {
        fun newInstance(): VendorHomeFragment = VendorHomeFragment()
    }

    private fun moveToLastLocation()
    {
        currentLat = PublicValues.userLatitude
        currentLong = PublicValues.userLongitude
        userLocation = LatLng(PublicValues.userLatitude, PublicValues.userLongitude)
        val cameraPosition = CameraPosition.Builder().target(userLocation).zoom(15f).build()
        mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        marker = mMap!!.addMarker(MarkerOptions().position(userLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.myicon)).title("You are here"))

        if (isAdded && isVisible) {
            updateAddress()
        }
        else {
            showErrorSnackBar("Not Added and Not Visible",false)
        }

        updateLocation()
    }

    @SuppressLint("MissingPermission")
    private fun updateLocation()
    {
        mLocationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        mLocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                mLocation = location
                userLocation = LatLng(mLocation!!.latitude, mLocation!!.longitude)
                currentLat = mLocation!!.latitude
                currentLong = mLocation!!.longitude
                
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))

                if(marker==null)
                    marker = mMap!!.addMarker(MarkerOptions().position(userLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.myicon)).title("You are here"))
                else
                    marker!!.position = userLocation

                if (isAdded && isVisible) {
                    updateAddress()
                }
                else {
                    showErrorSnackBar("Not Added and Not Visible",false)
                }
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }


        val currentApiVersion = Build.VERSION.SDK_INT
        if (currentApiVersion >= Build.VERSION_CODES.M)
        {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_DENIED)
            {
                //mMap.isMyLocationEnabled = true

                //mMap.uiSettings.isMyLocationButtonEnabled = true

                mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000,
                    0f,
                    mLocationListener
                )

                mMap!!.setOnMyLocationClickListener {

                }
            }
            else {
                Log.e("No Permission", "Permission Denied")
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ), 100
                )
            }
        }
    }

    override fun onStop() {
        showErrorSnackBar("Stopped",false)

        if(currentCity!=null)
            GeoFire(FirebaseDatabase.getInstance().getReference(Constants.VENDOR_LOCATION_REF).child(currentCity!!))
                .removeLocation(FirebaseAuth.getInstance().currentUser!!.uid)
        if(currentPostalCode!=null)
            GeoFire(FirebaseDatabase.getInstance().getReference(Constants.VENDOR_LOCATION_REF).child(currentPostalCode!!))
                .removeLocation(FirebaseAuth.getInstance().currentUser!!.uid)

        if(onlineRef!=null)
            onlineRef!!.removeEventListener(onlineValueEventListener)
        super.onStop()
    }

    override fun onResume() {
        super.onResume()

        if(currentLat!=0.0 && currentLong!=0.0)
            updateLatLong(currentCity)

        if(onlineRef!=null)
            registerOnlineSystem()
    }

    override fun onDestroy(){

        if(currentCity!=null)
            GeoFire(FirebaseDatabase.getInstance().getReference(Constants.VENDOR_LOCATION_REF).child(currentCity!!))
            .removeLocation(FirebaseAuth.getInstance().currentUser!!.uid)

        if(currentPostalCode!=null)
            GeoFire(FirebaseDatabase.getInstance().getReference(Constants.VENDOR_LOCATION_REF).child(currentPostalCode!!))
            .removeLocation(FirebaseAuth.getInstance().currentUser!!.uid)

        if(onlineRef!=null)
            onlineRef!!.removeEventListener(onlineValueEventListener)
        super.onDestroy()
    }


    private fun updateAddress()
    {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(currentLat, currentLong, 1)

        if(addresses[0].locality!=null)
            currentCity = addresses[0].locality
        else
            currentCity = addresses[0].postalCode

        updateLatLong(currentCity)
    }

    private fun updateLatLong(city:String?)
    {
        onlineRef = FirebaseDatabase.getInstance().reference.child("info/connected")
        vendorLocationRef = FirebaseDatabase.getInstance().getReference(Constants.VENDOR_LOCATION_REF).child(city!!)
        currentUserRef = vendorLocationRef.child(FirebaseAuth.getInstance().currentUser!!.uid)

        geoFire = GeoFire(vendorLocationRef)


        geoFire!!.setLocation(
            FirebaseAuth.getInstance().currentUser!!.uid,
            GeoLocation(currentLat,currentLong)
        ){key: String?, error:DatabaseError? ->
            if(error != null)
                showErrorSnackBar(error.toString(),true)
            else
                showErrorSnackBar("Your Are Online Now",false)
        }
    }
}