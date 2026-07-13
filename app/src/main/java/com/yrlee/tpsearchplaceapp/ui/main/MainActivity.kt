package com.yrlee.tpsearchplaceapp.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.yrlee.tpsearchplaceapp.R
import com.yrlee.tpsearchplaceapp.network.RetrofitHelper
import com.yrlee.tpsearchplaceapp.network.RetrofitService
import com.yrlee.tpsearchplaceapp.databinding.ActivityMainBinding
import com.yrlee.tpsearchplaceapp.model.DothomeResponse
import com.yrlee.tpsearchplaceapp.model.KakaoSearchPlaceResponse
import com.yrlee.tpsearchplaceapp.repository.PlaceRepository
import com.yrlee.tpsearchplaceapp.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // 내 위치 얻어오기 [개인정보이기에 동적퍼미션 필요]
    // 내 위치 검색은 Google Fused Location API 사용 [라이브러리 추가 필요 : play-services-location]

    // 내 위치 정보를 얻어오기 위한 클래스의 참조변수 [위치정보제공자(gps, network, passive)를 사용하는 객체]
//    val locationProviderClient : FusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    // 카카오의 로컬 검색 API를 사용하는 데 필요한 요청 파라미터
    // 1. 검색장소명
    var searchQuery: String = "화장실"
    // 2. 현재 내 위치 정보(위도, 경도 정보를 멤버로 보유한 객체)
    var myLocation: Location? = null

    // 카카오 장소 검색 응답결과 json을 분석하여 만들어진 객체 참조변수
    var searchPlaceResponse: KakaoSearchPlaceResponse? = null

    // recyclerview adapter
    lateinit var adapter: PlaceListAdapter


    // MVVM 적용
    private val viewModel: MainViewModel by viewModels()

    // view binding
//    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    // data binding
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.vm = viewModel
        binding.lifecycleOwner = this

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        adapter = PlaceListAdapter(this@MainActivity){
            viewModel.likePlace(it)
        }
        binding.recyclerView.adapter = adapter


        // 내 위치 정보 취득에 대한 동적 퍼미션
        val permissionResult = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        if(permissionResult==PackageManager.PERMISSION_DENIED)
            permissionResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) // 퍼미션 요청
        else viewModel.requestMyLocation()


        // drawer 열기
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // 검색어 입력 리스너
        binding.etSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.searchPlaces() // 검색 장소명 키워드로 장소들 검색
                true
            } else {
                false // 액션 버튼이 클릭되었을때, 여기서 모든 처리를 소비하지 않겠다.
            }
        }

        // 검색어 단축 버튼들 클릭에 반응하는 작업..
        setChoiceButtonListener()


        //-----------------------------------------------------------------------------------------
        // MVVM observe
        viewModel.placeList.observe(this){
            adapter.clear()
            adapter.addPlaceList(it)
        }

        viewModel.myLocation.observe(this){
            Log.d("mylocation", "latitude: ${it.latitude}, longitude: ${it.longitude}")
        }
    }
    
    // 퍼미션요청 작업을 대신 수행하는 대행사 객체를 등록(생성)
    val permissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if(it) viewModel.requestMyLocation()
        else Toast.makeText(this, "내 위치정보를 제공하지 않아 검색기능 사용이 제한됩니다.", Toast.LENGTH_SHORT).show()
    }


    // 검색어 단축 버튼들의 리스너를 설정하는 작업 메소드
    fun setChoiceButtonListener(){
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

    var choiceID = R.id.choice01

    fun clickChoice(v: View){

        // 기존에 선택되어 있던 ImageView의 배경 이미지를 선택되지 않은 배경으로 변경
        findViewById<ImageView>(choiceID).setBackgroundResource(R.drawable.bg_choice)
        // 현재 선택한 아이콘의 배경 그림을 선택된 배경색으로 변경
        findViewById<ImageView>(v.id).setBackgroundResource(R.drawable.bg_choice_selected)
        choiceID = v.id

        when(v.id){
            R.id.choice01 -> searchQuery = "화장실"
            R.id.choice02 -> searchQuery = "주유소"
            R.id.choice03 -> searchQuery = "전기차충전소"
            R.id.choice04 -> searchQuery = "약국"
            R.id.choice05 -> searchQuery = "공원"
            R.id.choice06 -> searchQuery = "편의점"
            R.id.choice07 -> searchQuery = "맛집"
            R.id.choice08 -> searchQuery = "주차장"
            R.id.choice09 -> searchQuery = "휴게실"
            R.id.choice10 -> searchQuery = "여성전용"
        }
        binding.etSearch.setText(searchQuery)
        binding.etSearch.clearFocus()

        adapter.clear()
        viewModel.searchPlaces()
    }




}