package com.yrlee.tpsearchplaceapp

import android.app.Application
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        // kakao native app key 등록 및 초기화
        KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
    }
}