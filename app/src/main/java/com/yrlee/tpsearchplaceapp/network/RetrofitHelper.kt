package com.yrlee.tpsearchplaceapp.network

import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import com.yrlee.tpsearchplaceapp.BuildConfig
class RetrofitHelper {

    companion object{
        fun getKakaoRetrofitInstance(): Retrofit{

            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->

                    val request = chain.request()
                        .newBuilder()
                        .addHeader(
                            "Authorization",
                            "KakaoAK ${BuildConfig.KAKAO_REST_API_KEY}"
                        )
                        .build()
                    Log.d("HEADER", request.header("Authorization") ?: "null")

                    chain.proceed(request)
                }
                .build()


            val retrofit = Retrofit.Builder().run{
                baseUrl("https://dapi.kakao.com")
                client(client)
                addConverterFactory(ScalarsConverterFactory.create())
                addConverterFactory(GsonConverterFactory.create())

                build()
            }
            return retrofit
        }

        fun getMyRetrofitInstance(): Retrofit{
            val retrofit = Retrofit.Builder().run{
                baseUrl("http://yrlee2025.dothome.co.kr/search_place/")
                addConverterFactory(ScalarsConverterFactory.create())
                addConverterFactory(GsonConverterFactory.create())
                build()
            }
            return retrofit
        }
    }
}