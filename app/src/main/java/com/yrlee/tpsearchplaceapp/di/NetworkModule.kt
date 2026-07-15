package com.yrlee.tpsearchplaceapp.di

import android.util.Log
import com.yrlee.tpsearchplaceapp.BuildConfig
import com.yrlee.tpsearchplaceapp.data.remote.KakaoApiService
import com.yrlee.tpsearchplaceapp.data.remote.MyApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // OkHttpClient 제공
    @Provides
    @Singleton
    @Named("kakao")
    fun provideKakaoClient(): OkHttpClient {

        return OkHttpClient.Builder()
            .addInterceptor { chain ->

                val request = chain.request()
                    .newBuilder()
                    .addHeader(
                        "Authorization",
                        "KakaoAK ${BuildConfig.KAKAO_REST_API_KEY}"
                    )
                    .build()
                Log.d("HEADER", request.header("Authorization") ?: "null")
                Log.d("REST_KEY", BuildConfig.KAKAO_REST_API_KEY)
                Log.d("NATIVE_KEY", BuildConfig.KAKAO_NATIVE_APP_KEY) // 있다면
                chain.proceed(request)
            }
            .build()
    }

    // 카카오 Retrofit 제공
    @Provides
    @Singleton
    @Named("kakao")
    fun provideKakaoRetrofit(
        @Named("kakao") client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://dapi.kakao.com")
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // PHP Retrofit 제공
    @Provides
    @Singleton
    @Named("my")
    fun provideMyRetrofit(): Retrofit {

        return Retrofit.Builder()
            .baseUrl("http://yerim425.dothome.co.kr/search_place/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    @Provides
    @Singleton
    fun provideKakaoService(
        @Named("kakao") retrofit: Retrofit
    ): KakaoApiService {
        return retrofit.create(KakaoApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMyService(
        @Named("my") retrofit: Retrofit
    ): MyApiService {
        return retrofit.create(MyApiService::class.java)
    }
}
