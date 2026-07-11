package com.yrlee.tpsearchplaceapp

import android.app.Application
import com.kakao.vectormap.KakaoMapSdk

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        // kakao native app key 등록 및 초기화
        KakaoMapSdk.init(this, "c3997999bab77d92b7bd0d525cdc967c")
    }
}