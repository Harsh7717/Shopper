package com.ksolutions.whatNeed.utils

import android.location.Location

interface OnLocationUpdateListener {
    fun onLocationChange(location: Location?)
}