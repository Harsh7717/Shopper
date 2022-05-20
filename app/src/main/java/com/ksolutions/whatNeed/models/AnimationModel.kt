package com.ksolutions.whatNeed.models

import android.os.Handler
import com.google.android.gms.maps.model.LatLng
import java.util.ArrayList

class AnimationModel(var isRun:Boolean, var geoQueryModel: GeoQueryModel)
{
    var polylineList = ArrayList<LatLng?>()
    var handler: Handler = Handler()
    var index: Int = 0
    var next: Int = 0
    var v: Float = 0.0f
    var lat: Double = 0.0
    var lng: Double = 0.0
    var start: LatLng? = null
    var end: LatLng? = null
}