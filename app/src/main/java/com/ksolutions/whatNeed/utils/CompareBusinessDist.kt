package com.ksolutions.whatNeed.utils

import com.ksolutions.whatNeed.models.BusinessModel
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

class CompareBusinessDist {
    companion object : Comparator<BusinessModel> {

        override fun compare(a: BusinessModel, b: BusinessModel): Int = when {

            dist(a.latitude, a.longitude, PublicValues.userLatitude, PublicValues.userLongitude) >
                    dist(b.latitude, b.longitude, PublicValues.userLatitude, PublicValues.userLongitude) -> 1

            dist(a.latitude, a.longitude, PublicValues.userLatitude, PublicValues.userLongitude) <
                    dist(b.latitude, b.longitude, PublicValues.userLatitude, PublicValues.userLongitude) -> -1

            else -> 0
        }

        internal fun dist(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
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
        private fun deg2rad(deg: Double): Double {
            return deg * Math.PI / 180.0
        }

        private fun rad2deg(rad: Double): Double {
            return rad * 180.0 / Math.PI
        }
    }
}