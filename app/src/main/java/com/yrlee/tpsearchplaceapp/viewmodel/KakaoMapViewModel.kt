package com.yrlee.tpsearchplaceapp.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yrlee.tpsearchplaceapp.data.local.FavoriteRepository
import com.yrlee.tpsearchplaceapp.model.PlaceUiModel
import com.yrlee.tpsearchplaceapp.repository.LocationRepository
import com.yrlee.tpsearchplaceapp.repository.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class KakaoMapViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val locationRepository: LocationRepository,
    private val favoriteRepository: FavoriteRepository
): ViewModel() {

    var _placeList = MutableLiveData<MutableList<PlaceUiModel>>()
    val placeList: LiveData<MutableList<PlaceUiModel>> = _placeList

    private val favoriteIds = mutableSetOf<String>()
    val searchQuery = MutableLiveData("화장실")
    val loading = MutableLiveData(false)

    private val myLocation = MutableLiveData<Location>()

    init{
        viewModelScope.launch {
            favoriteRepository.getFavoriteIds().collect { ids->
                favoriteIds.clear()
                favoriteIds.addAll(ids)
            }
        }
    }
    fun searchPlaces(){

        viewModelScope.launch {
            loading.value = true

            if (myLocation.value == null) {
                myLocation.value = locationRepository.getCurrentLocation()
            }

            val location = myLocation.value ?: return@launch

            // query 데이터
            val searchQuery = searchQuery.value  ?: ""
            val lng = location.longitude.toString()
            val lat = location.latitude.toString()
            val page = 1

            // 카카오 장소 검색 API 호출
            val response = placeRepository.searchPlace(searchQuery, lng, lat, page)
            val documents = response?.documents ?: emptyList() //  장소 검색 결과

            val list = placeList.value ?: mutableListOf<PlaceUiModel>()
            if (page == 1) {
                list.clear()
            }

            documents.forEach {
                val place = PlaceUiModel(
                    it,
                    favoriteIds.contains(it.id),
                    0 // myServerDB에서 좋아요 개수 읽어온 값
                )
                list.add(place)
            }

            _placeList.value = list

            loading.value = false
        }
    }

    fun refresh() {
        viewModelScope.launch {
            myLocation.value = locationRepository.getCurrentLocation()
            searchPlaces()
        }
    }

}