package com.yrlee.tpsearchplaceapp.viewmodel

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.yrlee.tpsearchplaceapp.model.DothomeResponse
import com.yrlee.tpsearchplaceapp.model.Place
import com.yrlee.tpsearchplaceapp.network.RetrofitHelper
import com.yrlee.tpsearchplaceapp.network.RetrofitService
import com.yrlee.tpsearchplaceapp.repository.LocationRepository
import com.yrlee.tpsearchplaceapp.repository.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@HiltViewModel
// 메인 화면 데이터를 관리하는 뷰모델
class MainViewModel  @Inject constructor(
    private val locationRepository: LocationRepository,
    private val placeRepository: PlaceRepository
) : ViewModel(){

    val placeList = MutableLiveData<List<Place>>() // 장소 리스트

    val myLocation = MutableLiveData<Location>() // 내 위치

    val searchQuery = MutableLiveData<String>("화장실") // 검색어
    val page = MutableLiveData<Int>(1) // 현재 페이지

    val loading = MutableLiveData(false) // 로딩 여부

    val count = MutableLiveData("") // 아이템개수/전체개수

    fun searchPlaces(){
        viewModelScope.launch {
            loading.value = true

            // query 데이터
            val searchQuery = searchQuery.value
            val lng = myLocation.value.longitude.toString()
            val lat = myLocation.value.latitude.toString()
            val page = page.value ?: 1

            // 카카오 장소 검색 API 호출
            val response = placeRepository.searchPlace(searchQuery, lng, lat, page)
            placeList.value = response?.documents //  장소 검색 결과

            // binding.tvCnt.text = "${(adapter.getPage()-1)*15+documents.size}/${meta?.total_count}"
            count.value = "${(page-1)*15+(response?.documents?.size?:0)}/${response?.meta?.total_count}"
            loading.value = false
        }
    }

    fun likePlace(place: Place) {



    }

    fun requestMyLocation(){

        viewModelScope.launch {
            myLocation.value = locationRepository.getCurrentLocation()
            searchPlaces() // 내 위치를 찾았으니 카카오 로컬(장소) 검색 시작
        }
    }


}