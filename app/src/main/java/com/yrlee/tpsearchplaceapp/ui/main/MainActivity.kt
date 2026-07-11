package com.yrlee.tpsearchplaceapp.ui.main

import android.Manifest
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.yrlee.tpsearchplaceapp.R
import com.yrlee.tpsearchplaceapp.data.remote.RetrofitHelper
import com.yrlee.tpsearchplaceapp.data.remote.RetrofitService
import com.yrlee.tpsearchplaceapp.databinding.ActivityMainBinding
import com.yrlee.tpsearchplaceapp.model.DothomeResponse
import com.yrlee.tpsearchplaceapp.model.KakaoSearchPlaceResponse
import com.yrlee.tpsearchplaceapp.model.Place
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    // 내 위치 얻어오기 [개인정보이기에 동적퍼미션 필요]
    // 내 위치 검색은 Google Fused Location API 사용 [라이브러리 추가 필요 : play-services-location]

    // 내 위치 정보를 얻어오기 위한 클래스의 참조변수 [위치정보제공자(gps, network, passive)를 사용하는 객체]
    val locationProviderClient : FusedLocationProviderClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    // 카카오의 로컬 검색 API를 사용하는 데 필요한 요청 파라미터
    // 1. 검색장소명
    var searchQuery: String = "화장실"
    // 2. 현재 내 위치 정보(위도, 경도 정보를 멤버로 보유한 객체)
    var myLocation: Location? = null

    // 카카오 장소 검색 응답결과 json을 분석하여 만들어진 객체 참조변수
    var searchPlaceResponse: KakaoSearchPlaceResponse? = null

    // recyclerview adapter
    lateinit var adapter: PlaceListAdapter


    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        adapter = PlaceListAdapter(this@MainActivity)
        binding.recyclerView.adapter = adapter


        // 내 위치 정보 취득에 대한 동적 퍼미션
        val permissionResult = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        if(permissionResult==PackageManager.PERMISSION_DENIED) permissionResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) // 퍼미션 요청
        else requestMyLocation()

        // 툴바의 네비게이션 아이콘을 클릭하면 .. 내 위치를 다시 갱신
        binding.ivRefresh.setOnClickListener { requestMyLocation() }

        // drawer 열기
        //
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // 검색어를 입력한 후 소프트키보드의 액션버튼(돋보기 아이콘)을 눌렀을 때 반응하기
        binding.etSearch.setOnEditorActionListener { v, actionId, event ->
            searchQuery = binding.etSearch.text.toString().trim()
            // 검색 장소명 키워드로 장소들 검색
            searchPlaces()
            binding.etSearch.setText(searchQuery)
            binding.etSearch.clearFocus()
            false // 액션 버튼이 클릭되었을때, 여기서 모든 처리를 소비하지 않겠다.
        }

        // 검색어 단축 버튼들 클릭에 반응하는 작업..
        setChoiceButtonListener()

    }
    
    // 퍼미션요청 작업을 대신 수행하는 대행사 객체를 등록(생성)
    val permissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if(it) requestMyLocation()
        else Toast.makeText(this, "내 위치정보를 제공하지 않아 검색기능 사용이 제한됩니다.", Toast.LENGTH_SHORT).show()
    }
    
    // 내 위치 검색 작업 메소드
    fun requestMyLocation(){
        // 요청 객체 생성 [정확도 우선, 5초마다 갱신]
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000).build()

        // 명시적으로 퍼미션 체크 코드가 이 메소드(requestLocationUpdate())와 같은 영역에 있어야 함.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return

        locationProviderClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    // 내 위치가 갱신될 때 마다 반응하는 콜백 객체
    val locationCallback = object : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            myLocation = p0.lastLocation // 마지막 검색된 위치

            // 위치 탐색이 종료되었으니, 위치 업데이트 멈추기
            locationProviderClient.removeLocationUpdates(this) // this : LocationCallback 객체

            adapter.clear()
            // 내 위치를 찾았으니 카카오 로컬(장소) 검색 시작
            searchPlaces()
        }
    }
    // 내 위치 주변의 특정 검색어의 장소를 찾아주는 작업 메소드
    fun searchPlaces(){
        binding.progressBar.visibility = View.VISIBLE
        // 카카오 로컬 검색 api에 필요한 요청 파라미터 2개 확인
        //Toast.makeText(this, "$searchQuery : ${myLocation?.latitude}, ${myLocation?.longitude}", Toast.LENGTH_SHORT).show()

        // retrofit http 통신 라이브러리를 이용하여 REST API 수행 [라이브러리 추가 : retrofit, gson, converter-gson, converter-scalars]
        val retrofit = RetrofitHelper.getKakaoRetrofitInstance()
        val retrofitApiService = retrofit.create(RetrofitService::class.java)
        val call = retrofitApiService.searchPlaces(searchQuery, myLocation?.longitude.toString(), myLocation?.latitude.toString(), adapter.getPage())
        call.enqueue(object : Callback<KakaoSearchPlaceResponse>{
            override fun onResponse(
                call: Call<KakaoSearchPlaceResponse>,
                response: Response<KakaoSearchPlaceResponse>
            ) {
                // 응답받은 json을 파싱한 결과 객체를 참조
                searchPlaceResponse = response.body()

                // 먼저 테스트 목적으로 데이터가 잘 왔는지 확인
                var meta = searchPlaceResponse?.meta
                var documents: List<Place>? = searchPlaceResponse?.documents
                //AlertDialog.Builder(this@MainActivity).setMessage("${meta?.total_count}\n${documents?.get(14)?.phone}").show()

                // 현재 어느탭을 보여주고 있던지.. 새로 검색결과를 받으면 무조건 PlaceListFragment가 보여주도록 함
//                binding.bnv.selectedItemId = R.id.menu_bnv_list // 선택된 아이템 아이디를 지정하면 바꿔줌

                adapter.addPlaceList(documents!!)
                binding.tvCnt.text = "${(adapter.getPage()-1)*15+documents.size}/${meta?.total_count}"

//                ObjectAnimator.ofFloat(binding.fabRefresh, "translationY", 0f).start()
//                ObjectAnimator.ofFloat(binding.fabRefresh, "rotationX", 0f).start()
                binding.progressBar.visibility = View.GONE
            }

            override fun onFailure(call: Call<KakaoSearchPlaceResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "${t.message}", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
        })
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
        adapter?.clear()
        searchPlaces()
        binding.etSearch.setText(searchQuery)
        binding.etSearch.clearFocus()
    }

    fun insertFavorPlace(place: Place){
        val retrofit = RetrofitHelper.getMyRetrofitInstance()
        val retrofitService = retrofit.create(RetrofitService::class.java)
        val call = retrofitService.insertFavorPlace(place)
        call.enqueue(object : Callback<DothomeResponse>{
            override fun onResponse(
                call: Call<DothomeResponse>,
                response: Response<DothomeResponse>
            ) {
                response.body()?.let{

                    Toast.makeText(this@MainActivity, "${it.resultCode}", Toast.LENGTH_SHORT).show()

                }
            }

            override fun onFailure(call: Call<DothomeResponse>, t: Throwable) {
                //Toast.makeText(this@MainActivity, "${t.message}", Toast.LENGTH_SHORT).show()
                Log.d("aaa", "${t.message}")
            }

        })
    }


}