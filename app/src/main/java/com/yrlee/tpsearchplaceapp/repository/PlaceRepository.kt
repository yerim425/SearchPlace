package com.yrlee.tpsearchplaceapp.repository

import com.yrlee.tpsearchplaceapp.model.DothomeResponse
import com.yrlee.tpsearchplaceapp.model.KakaoSearchPlaceResponse
import com.yrlee.tpsearchplaceapp.model.Place
import com.yrlee.tpsearchplaceapp.network.RetrofitHelper
import com.yrlee.tpsearchplaceapp.network.RetrofitService
import jakarta.inject.Inject
import retrofit2.Call

// 카카오 API와 내 서버 API를 실제로 호출하는 클래스
class PlaceRepository @Inject constructor(){

    private val service = RetrofitHelper.getKakaoRetrofitInstance().create(RetrofitService::class.java)

    // 카카오 API - 장소 검색
    suspend fun searchPlace(query: String, longitude: String, latitude: String, page:Int): KakaoSearchPlaceResponse?{
        return service.searchPlaces(query, longitude, latitude, page)
    }

    // 닷홈 서버 API - 좋아요 등록
    suspend fun likePlace(place: Place): Call<DothomeResponse> {
        return service.insertPlace(place)
    }

}