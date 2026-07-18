package com.yrlee.tpsearchplaceapp.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
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
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.Label
import com.kakao.vectormap.label.LabelLayer
import com.kakao.vectormap.label.LabelLayerOptions
import com.kakao.vectormap.label.LabelManager
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextBuilder
import com.yrlee.tpsearchplaceapp.R
import com.yrlee.tpsearchplaceapp.databinding.ActivityKakaoMapBinding
import com.yrlee.tpsearchplaceapp.model.Cluster
import com.yrlee.tpsearchplaceapp.model.PlaceUiModel
import com.yrlee.tpsearchplaceapp.viewmodel.KakaoMapViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import androidx.core.view.GravityCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior

@AndroidEntryPoint
class KakaoMapActivity : AppCompatActivity() {

    lateinit var binding: ActivityKakaoMapBinding
    private val viewModel: KakaoMapViewModel by viewModels()

    // kakao map
    private lateinit var kakaoMap: KakaoMap

    // cluster
    lateinit var labelManager: LabelManager // 마커 관리자

    lateinit var placeLayer: LabelLayer //  장소 마커 Layer
    lateinit var myLocationLayer: LabelLayer // 내 위치 마커 Layer


    private val placeLabels = HashMap<Label, PlaceUiModel>() // 장소 실제 마커들
    private val clusterLabels = HashMap<Label, Cluster>() // 클러스터 실제 마커들
    private var myLocationLabel: Label? = null // 내 위치 실제 마커


    // 카테고리 별 마커 스타일
    lateinit var defaultStyles: LabelStyles // 기본
    lateinit var bigMartStyles: LabelStyles // 대형마트
    lateinit var kindergartenStyles: LabelStyles      // 유치원
    lateinit var schoolStyles: LabelStyles      // 학교
    lateinit var academyStyles: LabelStyles      // 학원
    lateinit var subwayStyles: LabelStyles      // 지하철역
    lateinit var bankStyles: LabelStyles      // 은행
    lateinit var cultureStyles: LabelStyles      // 문화시설
    lateinit var brokerageHouseStyles: LabelStyles // 중개업소
    lateinit var publicStyles: LabelStyles // 공공기관
    lateinit var touristStyles: LabelStyles // 관광명소
    lateinit var accommodationStyles: LabelStyles // 숙박
    lateinit var cafeStyles: LabelStyles // 카페
    lateinit var hospitalStyles: LabelStyles // 병원
    lateinit var toiletStyles: LabelStyles      // 화장실
    lateinit var gasStationStyles: LabelStyles  // 주유소
    lateinit var evStyles: LabelStyles       // 전기충전소
    lateinit var pharmacyStyles: LabelStyles    // 약국
    lateinit var parkStyles: LabelStyles        // 공원
    lateinit var convenienceStyles: LabelStyles // 편의점
    lateinit var restaurantStyles: LabelStyles  // 레스토랑
    lateinit var parkingStyles: LabelStyles        // 레스토랑
    lateinit var retiringStyles: LabelStyles  // 레스토랑
    lateinit var womanStyles: LabelStyles      // 레스토랑

    lateinit var myLocationStyles: LabelStyles // 내 위치 마커 스타일

    private var isFirstMove = true

    var choiceID = R.id.choice01 // 선택한 카테고리 id
    var searchQuery = "화장실" //  검색어

    // bottom sheet behavior
    lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

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

        setChoiceButtonListener() // 카테고리 리스너

        // 카카오 지도 시작
        binding.kakaoMapView.start(object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
                Log.d("KakaoMap", "Map Destroy")
            }

            override fun onMapError(p0: Exception?) {
                Log.e("KakaoMap", "Map Error", p0)
            }
        }, object : KakaoMapReadyCallback() {
            override fun onMapReady(p0: KakaoMap) {
                kakaoMap = p0

                initLabel()

                // 클러스터 마커 클릭 이벤트 -> 줌 인
                kakaoMap.setOnLabelClickListener { _, _, label ->

                    when (label.tag) {
                        "cluster" -> {
                            kakaoMap.moveCamera(
                                CameraUpdateFactory.newCenterPosition(
                                    label.position,
                                    kakaoMap.zoomLevel + 1
                                )
                            )
                            true
                        }
                        "place" -> {
                            val place = placeLabels[label] ?: return@setOnLabelClickListener false
                            // drawer 열기
                            viewModel.selectPlace(place)

                            true
                        }
                        else -> false
                    }
                }

                observeMyLocation() // 내 위치 그리기
                observerPlace() // 검색 장소 그리기

                // 카메라 이동 종료 시 다시 클러스터링
                kakaoMap.setOnCameraMoveEndListener { _, _, _ ->
                    viewModel.placeList.value?.let {
                        drawMarker(it)
                    }
                }

                viewModel.searchPlaces() // 장소 검색
            }

        })

        // 검색어 입력
        binding.etSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.searchPlaces() // 검색 장소명 키워드로 장소들 검색

                findViewById<ImageView>(choiceID).setBackgroundResource(R.drawable.bg_choice)

                // 키보드 숨기기
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
                true
            } else {
                false // 액션 버튼이 클릭되었을때, 여기서 모든 처리를 소비하지 않겠다.
            }
        }

        binding.ivBack.setOnClickListener { finish() }

        // webview 설정
//        binding.wv.apply {
//            settings.javaScriptEnabled = true
//            settings.domStorageEnabled = true
//            settings.loadWithOverviewMode = true
//            settings.useWideViewPort = true
//
//            webViewClient = WebViewClient()
//        }


        // bottom sheet
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet)
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.skipCollapsed = true

        // BottomShhet 열기
        viewModel.selectedPlace.observe(this) {
            binding.selectedPlace = it
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }


    }

    private fun observerPlace() {
        viewModel.placeList.observe(this@KakaoMapActivity) {
            drawMarker(it)

        }
    }

    private fun observeMyLocation() {
        viewModel.myLocation.observe(this) { location ->
            drawMyLocation(location)
        }
    }

    // label 초기화
    private fun initLabel() {

        labelManager = kakaoMap.labelManager!!

        placeLayer = labelManager.addLayer(
            LabelLayerOptions.from("placeLayer")
        )!!

        myLocationLayer = labelManager.addLayer(
            LabelLayerOptions.from("myLocationLayer")
        )!!

        // 내 위치 핀
        myLocationStyles = createStyle(R.drawable.ic_mypin)


        bigMartStyles = createStyle(R.drawable.ic_big_mart_marker)
        kindergartenStyles = createStyle(R.drawable.ic_kindergarden_marker)
        schoolStyles = createStyle(R.drawable.ic_school_marker)
        academyStyles = createStyle(R.drawable.ic_academy_marker)
        subwayStyles = createStyle(R.drawable.ic_subway_marker)
        bankStyles = createStyle(R.drawable.ic_bank_marker)
        cultureStyles = createStyle(R.drawable.ic_culture_marker)
        brokerageHouseStyles = createStyle(R.drawable.ic_broker_marker)
        publicStyles = createStyle(R.drawable.ic_public_marker)
        touristStyles = createStyle(R.drawable.ic_default_marker)
        accommodationStyles = createStyle(R.drawable.ic_accommodation_marker)
        cafeStyles = createStyle(R.drawable.ic_cafe_marker)
        hospitalStyles = createStyle(R.drawable.ic_hospital_marker)

        toiletStyles = createStyle(R.drawable.ic_toilet_marker)
        gasStationStyles = createStyle(R.drawable.ic_gas_station_marker)
        evStyles = createStyle(R.drawable.ic_ev_marker)
        pharmacyStyles = createStyle(R.drawable.ic_pharmarcy_marker)
        parkStyles = createStyle(R.drawable.ic_park_marker)
        convenienceStyles = createStyle(R.drawable.ic_convenient_marker)
        restaurantStyles = createStyle(R.drawable.ic_restaurant_marker)
        parkingStyles = createStyle(R.drawable.ic_parking_marker)
        retiringStyles = createStyle(R.drawable.ic_retiring_marker)
        womanStyles = createStyle(R.drawable.ic_woman_marker)
        defaultStyles = createStyle(R.drawable.ic_default_marker)

    }

    // marker style 생성 - 중복 코드 방지
    private fun createStyle(drawable: Int): LabelStyles {

        return labelManager.addLabelStyles(
            LabelStyles.from(
                LabelStyle.from(drawable)
            )
        )!!
    }

    // 내 위치에 미커 그리기
    private fun drawMyLocation(location: Location) {

        myLocationLabel?.remove()

        val latLng = LatLng.from(
            location.latitude,
            location.longitude
        )

        val option = LabelOptions.from(latLng)
            .setStyles(myLocationStyles)

        myLocationLabel = myLocationLayer.addLabel(option)

        if (isFirstMove) {
            kakaoMap.moveCamera(
                CameraUpdateFactory.newCenterPosition(latLng)
            )
            isFirstMove = false
        }
    }

    // 검색된 장소 마커 그리기
    private fun drawMarker(placeList: MutableList<PlaceUiModel>) {

        // 마커 다시 그리기 위해 초기화
        placeLayer.removeAll()
        clusterLabels.clear()
        placeLabels.clear()

        val zoom = kakaoMap.zoomLevel
        val clusters = makeClusters(placeList, zoom)

        clusters.forEach { cluster ->

            val latLng = LatLng.from(
                cluster.latitude,
                cluster.longitude
            )

            if (cluster.places.size == 1) { // 1개일 경우 장소 마커 생성

                val place = cluster.places.first()

                val style = getPlaceStyle(place.place.category_name)

                val label = placeLayer.addLabel(
                    LabelOptions.from(latLng)
                        .setStyles(style)
                        .setTag("place")
                )

                if (label != null) {
                    placeLabels[label] = place // 장소 마커를 map으로 관리
                }

            } else { // 클러스터 마커 생성

                val label = placeLayer.addLabel(
                    LabelOptions.from(latLng)
                        .setStyles(getClusterStyle(cluster.places.size))
                        .setTag("cluster")   // 클러스터 표시
                )

                if (label != null) {
                    clusterLabels[label] = cluster // 클러스터 마커를 map으로 관리
                }
            }
        }
    }

    // 클러스터 숫자별로 캐시를 만들어 재사용 - 메모리 부하 방지
    private val clusterStyleCache = HashMap<Int, LabelStyles>()

    private fun getClusterStyle(count: Int): LabelStyles {

        return clusterStyleCache.getOrPut(count) {

            labelManager.addLabelStyles(
                LabelStyles.from(
                    LabelStyle.from(
                        createClusterBitmap(count) // bitmap으로 클러스터를 그림
                    )
                )
            )!!
        }
    }


    private fun getPlaceStyle(category: String): LabelStyles {

        return when {
            category.contains("마트") -> bigMartStyles
            category.contains("화장실") -> toiletStyles
            category.contains("공원") -> parkStyles
            category.contains("편의점") -> convenienceStyles
            category.contains("유치원") -> kindergartenStyles
            category.contains("어린이집") -> kindergartenStyles
            category.contains("학교") -> schoolStyles
            category.contains("학원") -> academyStyles
            category.contains("주차") -> parkingStyles
            category.contains("주유소") -> gasStationStyles
            category.contains("전기차 충전소") -> evStyles
            category.contains("지하철") -> subwayStyles
            category.contains("은행") -> bankStyles
            category.contains("문화시설") -> cultureStyles
            category.contains("중개") -> brokerageHouseStyles
            category.contains("공공기관") -> publicStyles
            category.contains("관광") -> touristStyles
            category.contains("숙박") -> accommodationStyles
            category.contains("음식점") -> restaurantStyles
            category.contains("카페") -> cafeStyles
            category.contains("병원") -> hospitalStyles
            category.contains("약국") -> pharmacyStyles


            else -> defaultStyles
        }
    }

    // 검색어 단축 버튼들의 리스너를 설정하는 작업 메소드
    fun setChoiceButtonListener() {
        binding.choiceLayout.choice01.setOnClickListener { clickChoice(it) }
        binding.choiceLayout.choice02.setOnClickListener { clickChoice(it) }
        binding.choiceLayout.choice03.setOnClickListener { clickChoice(it) }
        binding.choiceLayout.choice04.setOnClickListener { clickChoice(it) }
        binding.choiceLayout.choice05.setOnClickListener { clickChoice(it) }
        binding.choiceLayout.choice06.setOnClickListener { clickChoice(it) }
        binding.choiceLayout.choice07.setOnClickListener { clickChoice(it) }
        binding.choiceLayout.choice08.setOnClickListener { clickChoice(it) }
        binding.choiceLayout.choice09.setOnClickListener { clickChoice(it) }
        binding.choiceLayout.choice10.setOnClickListener { clickChoice(it) }
    }


    fun clickChoice(v: View) {

        // 기존에 선택되어 있던 ImageView의 배경 이미지를 선택되지 않은 배경으로 변경
        findViewById<ImageView>(choiceID).setBackgroundResource(R.drawable.bg_choice)
        // 현재 선택한 아이콘의 배경 그림을 선택된 배경색으로 변경
        findViewById<ImageView>(v.id).setBackgroundResource(R.drawable.bg_choice_selected)
        choiceID = v.id

        when (v.id) {
            R.id.choice01 -> {
                searchQuery = "화장실"
            }

            R.id.choice02 -> {
                searchQuery = "주유소"
            }

            R.id.choice03 -> {
                searchQuery = "전기차충전소"
            }

            R.id.choice04 -> {
                searchQuery = "약국"
            }

            R.id.choice05 -> {
                searchQuery = "공원"
            }

            R.id.choice06 -> {
                searchQuery = "편의점"
            }

            R.id.choice07 -> {
                searchQuery = "맛집"
            }

            R.id.choice08 -> {
                searchQuery = "주차장"
            }

            R.id.choice09 -> {
                searchQuery = "휴게실"
            }

            R.id.choice10 -> {
                searchQuery = "여성전용"
            }
        }
        binding.etSearch.setText(searchQuery)
        binding.etSearch.clearFocus()

        placeLayer.removeAll()
        viewModel.searchPlaces()
    }

    // 클러스터 생성 함수
    private fun makeClusters(
        placeList: List<PlaceUiModel>,
        zoom: Int
    ): List<Cluster> {

        // 범위
        val cellSize = when {
            zoom >= 17 -> 0.0005
            zoom >= 16 -> 0.001
            zoom >= 15 -> 0.003
            zoom >= 14 -> 0.008
            zoom >= 13 -> 0.015
            zoom >= 12 -> 0.03
            zoom >= 11 -> 0.05
            else -> 0.1
        }

        val map = HashMap<Pair<Int, Int>, Cluster>()

        placeList.forEach { item ->

            val lat = item.place.latitude.toDouble()
            val lng = item.place.longitude.toDouble()

            val x = (lng / cellSize).toInt()
            val y = (lat / cellSize).toInt()

            val key = Pair(x, y)

            val cluster = map.getOrPut(key) {
                Cluster()
            }

            cluster.places.add(item)
        }

        // 평균 좌표 계산
        map.values.forEach { cluster ->

            cluster.latitude =
                cluster.places.sumOf {
                    it.place.latitude.toDouble()
                } / cluster.places.size

            cluster.longitude =
                cluster.places.sumOf {
                    it.place.longitude.toDouble()
                } / cluster.places.size
        }

        return map.values.toList()
    }

    // bimap 클러스터 그리기
    private fun createClusterBitmap(count: Int): Bitmap {

        val size = 120

        val bitmap = createBitmap(size, size)

        val canvas = Canvas(bitmap)

        // 원
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = "#ddFFAAAA".toColorInt()
        }

        canvas.drawCircle(
            size / 2f,
            size / 2f,
            size / 2.2f,
            circlePaint
        )

        // 흰색 테두리
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }

        canvas.drawCircle(
            size / 2f,
            size / 2f,
            size / 2.2f,
            strokePaint
        )

        // 숫자
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = 42f
            typeface = Typeface.DEFAULT_BOLD
        }

        val y = size / 2f - (textPaint.descent() + textPaint.ascent()) / 2

        canvas.drawText(
            count.toString(),
            size / 2f,
            y,
            textPaint
        )

        return bitmap
    }
}