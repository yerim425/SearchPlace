# 서치플레이스 SearchPlace
- Android Native App, 1인 제작

## 개요
### 내 주변 공공장소를 빠르게 검색하고 길찾기까지 지원하는 공공장소 서치 앱입니다.
- 급하게 공공장소를 방문해야 할 때 현재 위치를 기반으로 주변 공공장소를 검색하여 빠르게 확인할 수 있다.
- 검색 결과는 현재 위치를 기반으로 가까운 순으로 정렬되며, 카카오맵과 연동하여 목적지까지 길찾기를 이용할 수 있다.
- 좋아요 기능을 통해 관심있는 장소들을 한 눈에 볼 수 있다.
- 지도 화면을 통해 찾으려는 장소의 위치를 직관적으로 확인할 수 있다.

## 기술 스택
- Language : Kotlin
- Architecture : MVVM
- UI : XML, Data Binding
- Network : Retrofit2, OkHttp3
- Local Database : Room(SQLite)
- Dependency Injection : Hilt
- Development Environment : Android Studio
- API / SDK : Kakao Maps SDK, Google Fused Location Provider
- Open API : Kakao Local API

## 사용기술
- Kakao Local API를 활용한 주변 장소 검색
- 현재 위치 기반 검색(GPS)
- Room을 이용한 즐겨찾기 저장
- Hilt를 이용한 의존성 주입
- Retrofit을 이용한 REST API 통신
- MVVM + Data Binding 구조 적용
- RecyclerView 무한 스크롤(페이지네이션)
- Kotlin Coroutines / Flow를 이용한 비동기 처리
- 지도 마커 클러스터링

## 와이어프레임 / 시스템 구조도
<img src="images/wireframe_structure.png" width="500"/>

## UI Flow
<img src="images/ui_flow.png" width="500">

## 주요기능

### 1) 내 위치 기반 장소 조회

### 2) 좋아요 등록/해지

### 3) 카카오 지도 마커/클러스터링

