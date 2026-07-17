package com.yrlee.tpsearchplaceapp.model

data class Cluster(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    val places: MutableList<PlaceUiModel> = mutableListOf()
)