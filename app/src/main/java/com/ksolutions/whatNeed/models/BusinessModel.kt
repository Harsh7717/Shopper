package com.ksolutions.whatNeed.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * A Data Model Class for Happy Place details. We will you this data class in all over the project even when
 * dealing with local SQLite database.
 */
@Parcelize
data class BusinessModel(
        val user_id: String = "",
        val title: String = "",
        val image: String = "",
        val owner_name: String = "",
        val address: String = "",
        val owner_contact: String = "",
        val reg_no:String = "",
        val location: String = "",
        val postal_code: String = "",
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        var category: String = "",
        var business_id: String = ""
) : Parcelable