package com.yrlee.tpsearchplaceapp.data.remote

import com.yrlee.tpsearchplaceapp.model.DothomeResponse
import com.yrlee.tpsearchplaceapp.model.Place
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MyApiService {


    /// DB에 좋아요 등록한 Place 데이터 저장
    @POST("insertLikePlace.php")
    fun insertLikePlace(
        @Body data: Place
    ): Call<DothomeResponse>

    @GET("loadLikePlace.php")
    fun loadLikePlaces(): Call<DothomeResponse>
}