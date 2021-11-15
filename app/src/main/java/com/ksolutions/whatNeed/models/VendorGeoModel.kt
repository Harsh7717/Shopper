package com.ksolutions.whatNeed.models

import com.firebase.geofire.GeoLocation

class VendorGeoModel {
    var key:String? = null
    var geoLocation: GeoLocation?=null
    var vendorInfoModel: VendorInfoModel?=null

    constructor(key: String?, geoLocation: GeoLocation)
    {
        this.key = key
        this.geoLocation = geoLocation
    }
}
