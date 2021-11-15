package com.ksolutions.whatNeed.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Product(
    val p_user_id: String = "",
    val p_image: String = "",
    val p_title: String = "",
    val p_description: String = "",
    var p_price: String = "0",
    var p_quantity: String = "0",
    val p_business_name: String = "",
    val p_business_id: String = "",
    val p_business_lat: Double = 0.0,
    val p_business_long: Double = 0.0,
    var p_postalCode:String = "",
    var product_id: String = ""
): Parcelable
