package com.yrlee.tpsearchplaceapp.util

import android.location.Location
import java.util.Locale

object LocationUtil {

    // 거리 계산
    fun calculateDistance(
        myLatitude: Double,
        myLongitude: Double,
        targetLatitude: Double,
        targetLongitude: Double
    ): Float {

        val result = FloatArray(1)

        Location.distanceBetween(
            myLatitude,
            myLongitude,
            targetLatitude,
            targetLongitude,
            result
        )

        return result[0] // meter 단위
    }

    // 거리 단위 포켓 -> 1000m->1k
    fun formatDistance(distance: Float): String {
        return if (distance < 1000) {
            "${distance.toInt()}m"
        } else {
            String.format(Locale.getDefault(), "%.1fkm", distance / 1000)
        }
    }
}