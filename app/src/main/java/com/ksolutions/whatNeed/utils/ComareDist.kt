package com.ksolutions.whatNeed.utils

import com.google.android.gms.maps.model.LatLng
import com.ksolutions.whatNeed.models.Product
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

class CompareDist {

    companion object : Comparator<Product> {

        override fun compare(a: Product, b: Product): Int = when {

            dist(a.p_business_lat, a.p_business_long, PublicValues.userLatitude, PublicValues.userLongitude) >
                    dist(b.p_business_lat, b.p_business_long, PublicValues.userLatitude, PublicValues.userLongitude) -> 1

            dist(a.p_business_lat, a.p_business_long, PublicValues.userLatitude, PublicValues.userLongitude) <
                    dist(b.p_business_lat, b.p_business_long, PublicValues.userLatitude, PublicValues.userLongitude) -> -1

            else -> 0
        }

        private fun dist(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
            val theta = lon1 - lon2
            var dis = (sin(deg2rad(lat1))
                    * sin(deg2rad(lat2))
                    + (cos(deg2rad(lat1))
                    * cos(deg2rad(lat2))
                    * cos(deg2rad(theta))))
            dis = acos(dis)
            dis = rad2deg(dis)
            dis *= 60 * 1.1515
            return dis.toFloat()
        }
        internal fun deg2rad(deg: Double): Double {
            return deg * Math.PI / 180.0
        }

        internal fun rad2deg(rad: Double): Double {
            return rad * 180.0 / Math.PI
        }
    }
}