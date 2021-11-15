package com.ksolutions.whatNeed.utils

import com.ksolutions.whatNeed.models.VendorGeoModel

interface FirebaseVendorInfoListener {
    fun onVendorInfoLoadSuccess(vendorGeoModel: VendorGeoModel?)
}