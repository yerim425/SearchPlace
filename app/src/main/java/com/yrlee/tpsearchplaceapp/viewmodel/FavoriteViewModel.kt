package com.yrlee.tpsearchplaceapp.viewmodel

import android.location.Location
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yrlee.tpsearchplaceapp.data.local.FavoritePlace
import com.yrlee.tpsearchplaceapp.data.local.FavoriteRepository
import com.yrlee.tpsearchplaceapp.model.FavoriteUiModel
import com.yrlee.tpsearchplaceapp.model.PlaceUiModel
import com.yrlee.tpsearchplaceapp.repository.LocationRepository
import com.yrlee.tpsearchplaceapp.util.LocationUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val favoriteRepository: FavoriteRepository
): ViewModel() {

    // 좋아요 개수
    val count = MutableLiveData("0")

    // 좋아요 장소 리스트
    val placeList = MutableLiveData<MutableList<FavoriteUiModel>>(mutableListOf())

    // 로딩
    val loading = MutableLiveData(false)

    private val myLocation = MutableLiveData<Location>()

    fun loadFavoritePlaces() {
        loading.value = true

        viewModelScope.launch {

            val location = locationRepository.getCurrentLocation()
            myLocation.value = location

            val favorites = favoriteRepository.getAll().first()

            val list = favorites.map { place ->
                val distance = LocationUtil.calculateDistance(
                    location.latitude,
                    location.longitude,
                    place.latitude.toDouble(),
                    place.longitude.toDouble()
                )

                FavoriteUiModel(
                    place,
                    distance
                )
            }

            placeList.value = list.toMutableList()
            count.value = list.size.toString()

            loading.value = false
        }
    }

    fun likePlace(placeId: String) {

        viewModelScope.launch {
            favoriteRepository.delete(placeId)

            placeList.value = placeList.value
                ?.filter { it.place.id != placeId }
                ?.toMutableList()

            count.value = placeList.value?.size.toString()
        }
    }


}