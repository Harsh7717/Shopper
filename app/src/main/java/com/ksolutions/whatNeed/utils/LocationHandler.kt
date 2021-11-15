package com.ksolutions.whatNeed.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.location.*
import com.ksolutions.whatNeed.ui.fragments.vendors.VendorsFragment
import kotlinx.android.synthetic.main.app_bar_main.*


open class LocationHandler(private val mContext: Context) {

    var latitude: Double = 0.0
    var longitude: Double = 0.0

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    fun startLocationUpdates(mContext: Context?) {


    }

    private fun stopLocationUpdate(mContext: Context) {

    }


    private fun createLocationRequest() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)

        if (ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
            latitude = location!!.latitude
            longitude = location.longitude

            PublicValues.userLatitude = latitude
            PublicValues.userLongitude = longitude
            Log.e("Location", latitude.toString())
        }
    }

    init {
        createLocationRequest()
    }
}