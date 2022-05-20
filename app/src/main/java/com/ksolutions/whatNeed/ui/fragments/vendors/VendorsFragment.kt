package com.ksolutions.whatNeed.ui.fragments.vendors

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Insets.add
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.OneShotPreDrawListener.add
import butterknife.ButterKnife
import butterknife.Unbinder
import com.firebase.geofire.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import com.ksolutions.whatNeed.R
import com.ksolutions.whatNeed.models.AnimationModel
import com.ksolutions.whatNeed.models.GeoQueryModel
import com.ksolutions.whatNeed.models.VendorGeoModel
import com.ksolutions.whatNeed.models.VendorInfoModel
import com.ksolutions.whatNeed.remote.IGoogleAPI
import com.ksolutions.whatNeed.remote.RetrofitClient
import com.ksolutions.whatNeed.ui.fragments.BaseFragment
import com.ksolutions.whatNeed.utils.*
import io.reactivex.Scheduler
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.schedulers.Schedulers.io
import kotlinx.android.synthetic.main.app_bar_main.*
import java.io.IOException
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*
import org.json.JSONObject as JSONObject


class VendorsFragment : BaseFragment(), FirebaseVendorInfoListener, FirebaseFailedListner {

    val TAB_POSITION = 0
    private var mUnbinder: Unbinder? = null

    private var myMarker: Marker?=null
    private lateinit var userLocation:LatLng
    private var mLocation: Location? = null
    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment

    private lateinit var mLocationManager: LocationManager
    private lateinit var mLocationListener: LocationListener
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    var distance = 1.0
    val LIMIT_RANGE = 10.0
    var previousLocation:Location?=null
    var currentLocation:Location?=null

    var firstTime = true

    lateinit var  iFirebaseVendorInfoListener: FirebaseVendorInfoListener
    lateinit var  iFirebaseFailedListener: FirebaseFailedListner

    var cityName = ""

    //val compositeDisposable = CompositeDisposable()
    private val compositeDisposable = io.reactivex.disposables.CompositeDisposable()

    private lateinit var iGoogleAPI: IGoogleAPI




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
       userLocation = LatLng(PublicValues.userLatitude,PublicValues.userLongitude)
       showProgressDialog(getString(R.string.map_loading))
       getLastLocation()

        try{
            val style = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.maps_style))

            if(!style)
                Log.e("Style Error", "Style parsing Error")
        }
        catch (e: Resources.NotFoundException) {
            e.message?.let { Log.e("Style Exception", it) }
        }
    }

//    adding comments

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_vendors, container, false)
        iFirebaseVendorInfoListener = this
        iFirebaseFailedListener = this

        iGoogleAPI = RetrofitClient.instance!!.create(IGoogleAPI::class.java)

        mUnbinder = ButterKnife.bind(this, rootView)
        return rootView
    }

    override fun onStop() {
        compositeDisposable.clear()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mUnbinder != null) {
            mUnbinder!!.unbind()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapFragment = (childFragmentManager.findFragmentById(R.id.vendors_map_view) as SupportMapFragment?)!!
        mapFragment.getMapAsync(callback)
    }

    companion object {
        fun newInstance(): VendorsFragment = VendorsFragment()
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation()
    {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        mFusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->

            if(location == null) {
                val newLocation = Location("flp")
                newLocation.latitude = 37.377166
                newLocation.longitude = -122.086966
                newLocation.accuracy = 3.0f

                mLocation = newLocation
            }
            else
                mLocation = location

            userLocation = LatLng(mLocation!!.latitude, mLocation!!.longitude)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))

            if(myMarker==null)
                myMarker = mMap.addMarker(MarkerOptions().position(userLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.myicon)).title("Your are Here"))
            else
                myMarker!!.position = userLocation

            hideProgressDialog()

            previousLocation = location
            currentLocation = location

            loadAllVendors(mLocation!!)
            updateLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocation()
    {
        mLocationManager = requireContext().getSystemService(LOCATION_SERVICE) as LocationManager

        mLocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                mLocation = location
                userLocation = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))

                if(myMarker==null)
                    myMarker = mMap.addMarker(MarkerOptions().position(userLocation).icon(BitmapDescriptorFactory.fromResource(R.drawable.myicon)).title("Your are Here"))
                else
                    myMarker!!.position = userLocation

                if(isAdded && isVisible)
                {
                    previousLocation = currentLocation
                    currentLocation = location

                    if((previousLocation?.distanceTo(currentLocation))!!/1000 <= LIMIT_RANGE) {
                        loadAllVendors(location)
                    }
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
                mMap.isMyLocationEnabled = true

                mMap.uiSettings.isMyLocationButtonEnabled = true

                mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000,
                    0f,
                    mLocationListener
                )

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15f))
                val cameraPosition = CameraPosition.Builder().target(userLocation).zoom(15f).build()
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

                mMap.setOnMyLocationClickListener {
                    loadAllVendors(mLocation!!)
                }

                val view = mapFragment.requireView().findViewById<View>("1".toInt())!!.parent as View
                val locationButton = view.findViewById<View>("2".toInt())

                val rlp = locationButton.layoutParams as RelativeLayout.LayoutParams
                rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
                rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                rlp.setMargins(0, 180, 180, 0)

                locationButton.setOnClickListener(){
                    loadAllVendors(mLocation!!)
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

    private fun loadAllVendors(location: Location) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

        try{
            if(addresses[0].locality == null)
                cityName = addresses[0].postalCode
            else
                cityName = addresses[0].locality
        } catch (e:IOException){
            showErrorSnackBar(e.toString(),true)
        }

        val vendorLocationRef = FirebaseDatabase.getInstance()
            .getReference(Constants.VENDOR_LOCATION_REF)
            .child(cityName)

        val geoFire = GeoFire(vendorLocationRef)
        val geoQuery = geoFire.queryAtLocation(GeoLocation(location.latitude,location.longitude),distance)
        geoQuery.removeAllListeners()

        geoQuery.addGeoQueryEventListener(object:GeoQueryEventListener{
            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                Constants.vendorsFound.add(VendorGeoModel(key!!,location!!))
            }

            override fun onKeyExited(key: String?) { }

            override fun onKeyMoved(key: String?, location: GeoLocation?) { }

            override fun onGeoQueryReady() {
                if(distance <= LIMIT_RANGE)
                {
                    distance++
                    loadAllVendors(location)
                }
                else
                {
                    distance = 0.0
                    addVendorMarker()
                }
            }

            override fun onGeoQueryError(error: DatabaseError?) {
                showErrorSnackBar(error!!.message,true)
            }
        })

        vendorLocationRef.addChildEventListener(object:ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val geoQueryModel = snapshot.getValue(GeoQueryModel::class.java)
                val geoLocation = GeoLocation(geoQueryModel!!.l!![0], geoQueryModel.l!![1])
                val vendorGeoModel = VendorGeoModel(snapshot.key, geoLocation)
                val newVendorLocation = Location("")

                newVendorLocation.latitude = geoLocation.latitude
                newVendorLocation.longitude = geoLocation.longitude

                val newDistance = location.distanceTo(newVendorLocation)/1000

                if(newDistance <= LIMIT_RANGE)
                {
                    findVendorByKey(vendorGeoModel)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) { }

            override fun onChildRemoved(snapshot: DataSnapshot) { }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) { }

            override fun onCancelled(error: DatabaseError) { }
        })
    }

    private fun addVendorMarker() {
        if(Constants.vendorsFound.size > 0)
        {
            io.reactivex.rxjava3.core.Observable.fromIterable(Constants.vendorsFound)
                .subscribeOn(Schedulers.newThread())
                .observeOn(io.reactivex.rxjava3.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        vendorGeoModel: VendorGeoModel? ->
                        findVendorByKey(vendorGeoModel)
                    },
                    {
                        t: Throwable? ->
                    }
                )
        }
        else
        {
            showErrorSnackBar(getString(R.string.no_vendor_found),true)
        }
    }

    private fun findVendorByKey(vendorGeoModel: VendorGeoModel?) {
        FirebaseDatabase.getInstance()
            .getReference(Constants.VENDOR_INFO_REF)
            .child(vendorGeoModel!!.key!!)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.hasChildren())
                    {
                        vendorGeoModel.vendorInfoModel = (snapshot.getValue(VendorInfoModel::class.java))
                        iFirebaseVendorInfoListener.onVendorInfoLoadSuccess(vendorGeoModel)
                    }
                    else
                        iFirebaseFailedListener.onFirebaseFailed(getString(R.string.key_not_found)+vendorGeoModel.key)
                }

                override fun onCancelled(error: DatabaseError) {
                    iFirebaseFailedListener.onFirebaseFailed(error.message)
                }
            })
    }

    override fun onVendorInfoLoadSuccess(vendorGeoModel: VendorGeoModel?) {
        if(!Constants.markerList.containsKey(vendorGeoModel!!.key))
        {
            Constants.markerList[vendorGeoModel.key!!] = mMap.addMarker(MarkerOptions()
                .position(LatLng(vendorGeoModel.geoLocation!!.latitude, vendorGeoModel.geoLocation!!.longitude))
                .flat(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.vendorsicon))
                .title(vendorGeoModel.vendorInfoModel!!.firstName + ":" + vendorGeoModel.vendorInfoModel!!.sellingItem)
                .snippet(vendorGeoModel.vendorInfoModel!!.phoneNumber)
            )!!
        }

        if(!TextUtils.isEmpty(cityName))
        {
            val vendorLocation = FirebaseDatabase.getInstance()
                .getReference(Constants.VENDOR_LOCATION_REF)
                .child(cityName)
                .child(vendorGeoModel.key!!)

            vendorLocation.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(!snapshot.hasChildren())
                    {
                        if(Constants.markerList[vendorGeoModel.key!!] !=null)
                        {
                            val marker = Constants.markerList[vendorGeoModel.key!!]
                            marker!!.remove()
                            Constants.markerList.remove(vendorGeoModel.key!!)
                            Constants.vendorsSubscribe.remove(vendorGeoModel.key)
                            vendorLocation.removeEventListener(this)
                        }
                    }
                    else
                    {
                        if(Constants.markerList[vendorGeoModel.key!!] !=null)
                        {
                            val geoQueryModel = snapshot.getValue(GeoQueryModel::class.java)
                            val animationModel = AnimationModel(false,geoQueryModel!!)

                            if(Constants.vendorsSubscribe[vendorGeoModel.key!!] != null)
                            {
                                val marker = Constants.markerList[vendorGeoModel.key!!]
                                val oldPosition = Constants.vendorsSubscribe[vendorGeoModel.key!!]

                                val from = StringBuilder()
                                    .append(oldPosition!!.geoQueryModel!!.l?.get(0))
                                    .append(",")
                                    .append(oldPosition.geoQueryModel.l?.get(1))
                                    .toString()

                                val to = StringBuilder()
                                    .append(animationModel.geoQueryModel!!.l?.get(0))
                                    .append(",")
                                    .append(animationModel.geoQueryModel.l?.get(1))
                                    .toString()

                                moveMarkerAnimation(vendorGeoModel.key!!, animationModel,marker,from,to)
                            }
                            else
                            {
                                Constants.vendorsSubscribe[vendorGeoModel.key!!] = animationModel
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showErrorSnackBar(error.message,true)
                }
            })
        }
    }

    private fun moveMarkerAnimation(key: String, newData: AnimationModel,marker: Marker?, from: String, to: String)
    {
        if(!newData.isRun)
        {
            compositeDisposable.add(iGoogleAPI.getDirections("driving",
                "less_driving",
                from, to,
                getString(R.string.google_maps_api_key))!!
                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe { returnResult ->
                    Log.d("API_RETURN", returnResult!!)

                    try{
                        val jsonObject = JSONObject(returnResult)
                        val jsonArray = jsonObject.getJSONArray("routes")

                        for(i in 0 until jsonArray.length())
                        {
                            val route = jsonArray.getJSONObject(i)
                            val poly = route.getJSONObject("overview_polyline")
                            val polyline = poly.getString("points")
                            //polylineList = Constants.decodePoly(polyline)
                            newData.polylineList = Constants.decodePoly(polyline)
                        }

                        //index = -1
                        //next = 1
                        newData.index = -1
                        newData.next = 1

                        val runnable = object: Runnable{
                            override fun run(){
                                if(newData.polylineList.size > 1)
                                {
                                    if(newData.index < newData.polylineList.size - 2)
                                    {
                                        newData.index++
                                        newData.next = newData.index+1

                                        newData.start = newData.polylineList[newData.index]!!
                                        newData.end = newData.polylineList[newData.next]!!
                                    }
                                    val valueAnimator = ValueAnimator.ofInt(0,1)
                                    valueAnimator.duration = 3000
                                    valueAnimator.interpolator = LinearInterpolator()
                                    valueAnimator.addUpdateListener { value ->
                                        newData.v = value.animatedFraction
                                        newData.lat = newData.v * newData.end!!.latitude + (1-newData.v) * newData.start!!.latitude
                                        newData.lng = newData.v * newData.end!!.longitude + (1-newData.v) * newData.start!!.longitude

                                        val newPos = LatLng(newData.lat, newData.lng)
                                        marker!!.position = newPos
                                        marker.setAnchor(0.5f,0.5f)
                                        marker.rotation = Constants.getBearing(newData.start!!,newPos)
                                    }
                                    valueAnimator.start()
                                    if(newData.index < newData.polylineList!!.size - 2)
                                        newData.handler!!.postDelayed(this,1500)
                                    else if(newData.index < newData.polylineList!!.size - 1)
                                    {
                                        newData.isRun = false
                                        Constants.vendorsSubscribe[key] = newData
                                    }
                                }
                            }
                        }
                        newData.handler!!.postDelayed(runnable,1500)

                    } catch (e: Exception)
                    {
                        showErrorSnackBar(e.message!!,true)
                    }
                }
            )
        }
    }

    override fun onFirebaseFailed(message: String) {
        showErrorSnackBar(message,true)
    }
}