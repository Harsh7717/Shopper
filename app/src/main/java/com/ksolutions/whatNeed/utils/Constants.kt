package com.ksolutions.whatNeed.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.ksolutions.whatNeed.models.AnimationModel
import com.ksolutions.whatNeed.models.VendorGeoModel
import kotlin.math.abs
import kotlin.math.atan


object Constants {
    val vendorsSubscribe: MutableMap<String, AnimationModel> = HashMap<String, AnimationModel>()
    val markerList: MutableMap<String, Marker> = HashMap<String, Marker>()
    const val VENDOR_INFO_REF: String = "VendorsInfo"

    const val VENDOR_LOCATION_REF = "VendorLocation"
    val vendorsFound: MutableSet<VendorGeoModel> = HashSet<VendorGeoModel>()

    const val SEARCH_AUTH: String = "com.ksolutions.MySuggestionProvider"
    const val BUSINESS: String = "business"
    const val TITLE: String = "title"
    const val ADDRESS: String = "address"
    const val OWNER_NAME: String = "owner_name"
    const val OWNER_CONTACT: String = "owner_contact"
    const val REG_NO: String = "reg_no"
    const val LOCATION: String = "location"
    const val POSTAL_CODE: String = "postal_code"
    const val LATITUDE: String = "latitude"
    const val LONGITUDE: String = "longitude"
    const val BUSINESS_CAT: String = "Business Categories"

    const val USERS: String = "users"
    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val EMAIL: String = "email"
    const val DOB: String = "dob"
    const val COMPLETE_PROFILE: String = "profileCompleted"

    const val PRODUCTS: String = "products"
    const val P_USER_ID: String = "p_user_id"
    const val P_BUSINESS_ID: String = "p_business_id"
    const val P_BUSINESS_LAT: String = "p_business_lat"
    const val P_BUSINESS_LONG: String = "p_business_long"
    const val P_BUSINESS_NAME: String = "p_business_name"
    const val P_DESC: String = "p_description"
    const val P_IMAGE: String = "p_image"
    const val P_QUANTITY: String = "p_quantity"
    const val P_TITLE: String = "p_title"
    const val PRODUCT_ID: String = "product_id"
    const val P_POSTAL_CODE: String = "p_postalCode"


    const val USER_ID: String = "user_id"

    const val SHOPPER_PREFERENCES: String = "ShopperPrefs"
    const val LOGGED_IN_USERNAME: String = "logged_in_username"

    const val EXTRA_USER_ID: String = "extra_user_id"
    const val EXTRA_PRODUCT_ID: String = "extra_product_id"
    const val EXTRA_BUSINESS_ID: String = "extra_business_id"

    //A unique code for asking the Read Storage Permission using this we will be check and identify in the method onRequestPermissionsResult in the Base Activity.
    const val READ_STORAGE_PERMISSION_CODE = 2

    // A unique code of image selection from Phone Storage.
    const val PICK_IMAGE_REQUEST_CODE = 2

    const val USER_PROFILE_IMAGE: String = "USER_PROFILE_IMAGE"
    const val BUSINESS_IMAGE: String = "BUSINESS_IMAGE"
    const val PRODUCT_IMAGE: String = "PRODUCT_IMAGE"

    const val CATEGORY: String = "business_category"
    const val BUSINESS_NAME: String = "business_name"

    var userPostalCode:String = ""

    fun showImageChooser(activity: Activity) {
        // An intent for launching the image selection of phone storage.
        val galleryIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        // Launches the image selection of phone storage using the constant code.
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(activity: Activity, uri: Uri?): String? {
        return MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(activity.contentResolver.getType(uri!!))
    }

    fun getBitmapFromVectorDrawable(context: Context?, drawableId: Int): Bitmap? {
        var drawable = ContextCompat.getDrawable(context!!, drawableId)
        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    //GET BEARING
    fun getBearing(begin: LatLng, end: LatLng): Float
    {
        val lat: Double = abs(begin.latitude - end.latitude)
        val lng: Double = abs(begin.longitude - end.longitude)

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return Math.toDegrees(atan(lng / lat)).toFloat()

        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (90 - Math.toDegrees(atan(lng / lat)) + 90).toFloat()

        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (Math.toDegrees(atan(lng / lat)) + 180).toFloat()

        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (90 - Math.toDegrees(atan(lng / lat)) + 270).toFloat()

        return (-1).toFloat()
    }

    //DECODE POLY
    fun decodePoly(encoded: String): ArrayList<LatLng?>
    {
        val poly = ArrayList<LatLng?>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len)
        {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)

            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0

            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)

            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val p = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)

            poly.add(p)
        }
        return poly
    }
}