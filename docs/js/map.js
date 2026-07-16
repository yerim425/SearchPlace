// 기본 위치 (서울 시청)
const map = new kakao.maps.Map(
    document.getElementById("map"),
    {
        center:new kakao.maps.LatLng(
            37.5665,
            126.9780
        ),
        level:4
    }
);

// 클러스터러 생성
const clusterer = new kakao.maps.MarkerClusterer({

    map:map,

    averageCenter:true,

    minLevel:6

});