package com.yrlee.tpsearchplaceapp.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException


class LocationRepository @Inject constructor(
    @ApplicationContext
    private val context: Context
){
    // 내 위치 정보를 얻어오기 위한 클래스의 참조변수 [위치정보제공자(gps, network, passive)를 사용하는 객체]
    val client : FusedLocationProviderClient  = LocationServices.getFusedLocationProviderClient(context)

    // 현재 위치를 가져와서 리턴
    suspend fun getCurrentLocation(): Location = suspendCancellableCoroutine{ cont ->

        // 요청 객체 생성 [정확도 우선, 5초마다 갱신]
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()

        // 내 위치가 갱신될 때 마다 반응하는 콜백 객체
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {

                client.removeLocationUpdates(this) // 위치 탐색이 종료되었으니, 위치 업데이트 멈추기

                val location = result.lastLocation
                if (location != null) { //null 체크 후 resume
                    cont.resume(location) {} // {}는 onCancellation
                } else {
                    cont.resumeWithException(Exception("위치 정보를 가져오지 못했습니다."))
                }
            }
        }

        // 명시적으로 퍼미션 체크 코드가 이 메소드(requestLocationUpdate())와 같은 영역에 있어야 함.
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            cont.resumeWithException(SecurityException("Location permission denied"))
            return@suspendCancellableCoroutine
        }
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
    }
}