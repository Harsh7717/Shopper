package com.ksolutions.whatNeed.models

import android.os.Parcel
import android.os.Parcelable

data class VendorInfoModel(
    var firstName:String = "",
    var lastName:String = "",
    var phoneNumber:String = "",
    var sellingItem:String = "",
    var rating: Double = 0.0,
    var totalRates: Int = 0
): Parcelable {
constructor(source: Parcel) : this(
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readString()!!,
        source.readDouble()!!,
        source.readInt()
)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(phoneNumber)
        parcel.writeString(sellingItem)
        parcel.writeDouble(rating)
        parcel.writeInt(totalRates)
    }

    override fun describeContents() = 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<VendorInfoModel> = object : Parcelable.Creator<VendorInfoModel> {
            override fun createFromParcel(source: Parcel): VendorInfoModel = VendorInfoModel(source)
            override fun newArray(size: Int): Array<VendorInfoModel?> = arrayOfNulls(size)
        }
    }
}
