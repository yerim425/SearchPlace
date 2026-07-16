package com.yrlee.tpsearchplaceapp.viewmodel

import android.content.Intent
import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yrlee.tpsearchplaceapp.data.local.FavoriteRepository
import com.yrlee.tpsearchplaceapp.model.Place
import com.yrlee.tpsearchplaceapp.model.PlaceUiModel
import com.yrlee.tpsearchplaceapp.repository.LocationRepository
import com.yrlee.tpsearchplaceapp.repository.PlaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import retrofit2.HttpException

@HiltViewModel
// 메인 화면 데이터를 관리하는 뷰모델
class MainViewModel  @Inject constructor(
    private val locationRepository: LocationRepository,
    private val placeRepository: PlaceRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel(){

    val placeList = MutableLiveData<MutableList<PlaceUiModel>>(mutableListOf()) // 장소 리스트

    private val favoriteIds = mutableSetOf<String>()

    val myLocation = MutableLiveData<Location>() // 내 위치

    val searchQuery = MutableLiveData<String>("화장실") // 검색어
    val page = MutableLiveData<Int>(1) // 현재 페이지

    val loading = MutableLiveData(false) // 로딩 여부

    val count = MutableLiveData("") // 아이템개수/전체개수

    init {
        // 내 좋아요 장소 id 목록 가져오기
        viewModelScope.launch {
            favoriteRepository.getFavoriteIds().collect { ids ->
                favoriteIds.clear()
                favoriteIds.addAll(ids)

//                placeList.value?.forEach { item ->
//                    item.isFavorite = favoriteIds.contains(item.place.id)
//                }
//
//                placeList.value = placeList.value
            }
        }
    }

//    fun searchPlaces(){
//
//        viewModelScope.launch {
//            loading.value = true
//
//            if (myLocation.value == null) {
//                myLocation.value = locationRepository.getCurrentLocation()
//            }
//
//            val location = myLocation.value ?: return@launch
//
//            // query 데이터
//            val searchQuery = searchQuery.value  ?: ""
//            val lng = location.longitude.toString()
//            val lat = location.latitude.toString()
//            val page = page.value ?: 1
//
//            // 카카오 장소 검색 API 호출
//            val response = placeRepository.searchPlace(searchQuery, lng, lat, page)
//            val documents = response?.documents ?: emptyList() //  장소 검색 결과
//
//            val list = placeList.value ?: mutableListOf<PlaceUiModel>()
//            if (page == 1) {
//                list.clear()
//            }
//
//            documents.forEach {
//                val place = PlaceUiModel(
//                    it,
//                    favoriteIds.contains(it.id),
//                    0 // myServerDB에서 좋아요 개수 읽어온 값
//                )
//                list.add(place)
//            }
//
//            placeList.value = list
//
//            count.value = "${list.size}/${response?.meta?.total_count ?: 0}"
//
//            loading.value = false
//        }
//    }

//    import android.util.Log
//    import retrofit2.HttpException

    fun searchPlaces() {

        viewModelScope.launch {

            try {
                loading.value = true

                if (myLocation.value == null) {
                    myLocation.value = locationRepository.getCurrentLocation()
                }

                val location = myLocation.value ?: return@launch

                val searchQuery = searchQuery.value ?: ""
                val lng = location.longitude.toString()
                val lat = location.latitude.toString()
                val page = page.value ?: 1

                val response = placeRepository.searchPlace(searchQuery, lng, lat, page)
                val documents = response?.documents ?: emptyList()

                val list = placeList.value ?: mutableListOf()

                if (page == 1) {
                    list.clear()
                }

                documents.forEach {
                    list.add(
                        PlaceUiModel(
                            it,
                            favoriteIds.contains(it.id),
                            0
                        )
                    )
                }

                placeList.value = list
                count.value = "${list.size}/${response?.meta?.total_count ?: 0}"

            } catch (e: HttpException) {

                Log.e("HTTP_CODE", e.code().toString())
                Log.e("HTTP_BODY", e.response()?.errorBody()?.string() ?: "no body")

            } catch (e: Exception) {

                Log.e("ERROR", Log.getStackTraceString(e))

            } finally {

                loading.value = false

            }
        }
    }

    fun likePlace(item: PlaceUiModel) {


        viewModelScope.launch {

            if (favoriteIds.contains(item.place.id)) {

                favoriteRepository.delete(item.place.id)

                favoriteIds.remove(item.place.id)

                item.isFavorite = false

            } else {

                favoriteRepository.insert(item.place)

                favoriteIds.add(item.place.id)

                item.isFavorite = true
            }

            placeList.value = placeList.value
        }
    }

    // 다음 페이지
    fun nextPage() {

        page.value = (page.value ?: 1) + 1

        searchPlaces()
    }

    // 새로 검색
    fun searchNewPlace() {

        page.value = 1

        placeList.value = mutableListOf()

        searchPlaces()
    }

    fun getMyLocation() = myLocation.value

}