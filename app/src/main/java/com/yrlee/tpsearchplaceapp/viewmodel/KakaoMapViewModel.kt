package com.yrlee.tpsearchplaceapp.viewmodel

import android.R
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

    private val _myLocation = MutableLiveData<Location>()
    val myLocation: LiveData<Location> = _myLocation

//    private var selectedCategoryCode: String

    init{
        viewModelScope.launch {
            favoriteRepository.getFavoriteIds().collect { ids->
                favoriteIds.clear()
                favoriteIds.addAll(ids)
            }
        }
    }
    fun searchPlaces(){
//        fun searchPlaces(category: String){

//        selectedCategoryCode = getCategoryCode(category)

        viewModelScope.launch {
            loading.value = true

            if (_myLocation.value == null) {
                _myLocation.value = locationRepository.getCurrentLocation()
            }

            val location = _myLocation.value ?: return@launch

            val query = searchQuery.value ?: ""
//            val categoryCode = selectedCategoryCode ?: ""
            val lng = location.longitude.toString()
            val lat = location.latitude.toString()

            val totalList = mutableListOf<PlaceUiModel>()

            // 카카오 최대 3페이지
            for (page in 1..3) {

                val response = placeRepository.searchPlace(
                    query,
                    lng,
                    lat,
                    page
                )

                val documents = response?.documents ?: emptyList()

                documents.forEach {
                    totalList.add(
                        PlaceUiModel(
                            it,
                            favoriteIds.contains(it.id),
                            0
                        )
                    )
                }

                // 마지막 페이지면 종료
                if (response?.meta?.is_end == true) {
                    break
                }
            }

            _placeList.value = totalList

            loading.value = false
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _myLocation.value = locationRepository.getCurrentLocation()
//            searchPlaces(selectedCategoryCode)
            searchPlaces()
        }
    }

//    private fun getCategoryCode(categoryName: String)
//    = when(categoryName){
//        "화장실" -> ""
//        "편의점" -> "CS2"
//        "주차장" -> "PK6"
//        "주유소" -> "OL7"
//        "맛집" -> "FD6"
//        "약국" -> "PM9"
//        else -> {}
//    }



}