package com.yrlee.tpsearchplaceapp.ui.map

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.label.LabelLayer
import com.kakao.vectormap.label.LabelManager
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.yrlee.tpsearchplaceapp.R
import com.yrlee.tpsearchplaceapp.databinding.ActivityKakaoMapBinding
import com.yrlee.tpsearchplaceapp.model.PlaceUiModel
import com.yrlee.tpsearchplaceapp.viewmodel.KakaoMapViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception

@AndroidEntryPoint
class KakaoMapActivity : AppCompatActivity() {

    lateinit var binding : ActivityKakaoMapBinding
    private val viewModel: KakaoMapViewModel by viewModels()

    // kakao map
    private lateinit var kakaoMap : KakaoMap

    // cluster
    lateinit var labelManager: LabelManager
    lateinit var labelLayer: LabelLayer
    lateinit var labelStyles: LabelStyles

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_kakao_map)
        binding.vm = viewModel
        binding.lifecycleOwner = this

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

//        binding.kakaoMapView.start(object : MapLifeCycleCallback(){
//            override fun onMapDestroy() {
//                TODO("Not yet implemented")
//            }
//
//            override fun onMapError(p0: Exception?) {
//                Log.e("KakaoMap", "Map Error", p0)
//            }
//        }, object: KakaoMapReadyCallback(){
//            override fun onMapReady(p0: KakaoMap) {
//                kakaoMap = p0
//                initLabel() // 클러스터
//                observerPlace()
//                viewModel.searchPlaces()
//            }
//
//        })

        // 검색어 입력
        binding.etSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.searchPlaces() // 검색 장소명 키워드로 장소들 검색

                // 키보드 숨기기
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
                true
            } else {
                false // 액션 버튼이 클릭되었을때, 여기서 모든 처리를 소비하지 않겠다.
            }
        }
    }

    private fun initLabel() {

        labelManager = kakaoMap.labelManager!!
        labelLayer = labelManager.layer!!

        val style = LabelStyle.from(R.drawable.ic_pin)

        labelStyles = labelManager.addLabelStyles(
            LabelStyles.from(style)
        )!!
    }

    private fun observerPlace(){
        viewModel.placeList.observe(this@KakaoMapActivity){
            drawMarker(it)
        }
    }

    private fun drawMarker(placeList: MutableList<PlaceUiModel>){
        labelLayer.removeAll()

        placeList.forEach {
            val option = LabelOptions.from(
                LatLng.from(
                    it.place.latitude.toDouble(),
                    it.place.longitude.toDouble()
                )
            ).setStyles(labelStyles)

            labelLayer.addLabel(option)
        }
    }
}