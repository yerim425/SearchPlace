package com.yrlee.tpsearchplaceapp.repository

import com.yrlee.tpsearchplaceapp.data.remote.KakaoApiService
import com.yrlee.tpsearchplaceapp.data.remote.MyApiService
import com.yrlee.tpsearchplaceapp.model.DothomeResponse
import com.yrlee.tpsearchplaceapp.model.KakaoSearchPlaceResponse
import com.yrlee.tpsearchplaceapp.model.Place
import jakarta.inject.Inject
import jakarta.inject.Named
import retrofit2.Call

// 카카오 API와 내 서버 API를 실제로 호출하는 클래스
class PlaceRepository @Inject constructor(
    private val kakaoApi: KakaoApiService,
    private val myApi: MyApiService
){

    // 카카오 API - 장소 검색
    suspend fun searchPlace(query: String, longitude: String, latitude: String, page:Int): KakaoSearchPlaceResponse?{
        return kakaoApi.searchPlaces(query, longitude, latitude, page)
    }

    // 닷홈 서버 API - 좋아요 등록
    suspend fun likePlace(place: Place): Call<DothomeResponse> {
        return myApi.insertLikePlace(place)
    }

}