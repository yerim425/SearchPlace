package com.yrlee.tpsearchplaceapp.network

import com.yrlee.tpsearchplaceapp.model.DothomeResponse
import com.yrlee.tpsearchplaceapp.model.KakaoSearchPlaceResponse
import com.yrlee.tpsearchplaceapp.model.Place
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RetrofitService {

    // 인증키는 헤더정보로
    // 내 위치와 검색키워드를 GET 방식으로 요청하여 장소들에 대한 정보를 json으로 주되 파싱하여 KakaoSearchPlaceResponse 객체로 만들어주는 코드를 줘
    @GET("v2/local/search/keyword.json?sort=distance")
    suspend fun searchPlaces(
        @Query("query") query: String,
        @Query("x") longitude: String,
        @Query("y") latitude: String,
        @Query("page") page: Int,
    ): KakaoSearchPlaceResponse?


    // 인증키는 헤더정보로
    // 내 위치와 검색키워드를 GET 방식으로 요청하여 장소들에 대한 정보를 json으로 주되 파싱하지 말고 일단 문자열로 받음
    @GET("v2/local/search/keyword.json?sort=distance")
    fun searchPlacesToString(
        @Query("query") query: String,
        @Query("x") longitude: String,
        @Query("y") latitude: String
    ): Call<String>

    /// DB에 즐겨찾기 추가한 Place 데이터 저장
    @POST("insertPlace.php")
    fun insertPlace(
        @Body data: Place
    ): Call<DothomeResponse>

    @GET("loadPlace.php")
    fun loadPlace(): Call<DothomeResponse>

}