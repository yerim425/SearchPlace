package com.yrlee.tpsearchplaceapp.ui.intro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yrlee.tpsearchplaceapp.R
import com.yrlee.tpsearchplaceapp.databinding.ActivityIntroBinding
import com.yrlee.tpsearchplaceapp.ui.main.MainActivity

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_intro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 서버나 DB에 저장된 좋아요 리스트 데이터들 불러오기, 로딩 완료시 인트로 화면 종료
        // 로그인 기능 [로그인 화면 or 메인화면]
//        if(username == null) 로그인 화면으로..
//        else 메인 화면 으로..

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1500)
    }
}