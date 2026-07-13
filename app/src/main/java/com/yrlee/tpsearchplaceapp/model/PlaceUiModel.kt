package com.yrlee.tpsearchplaceapp.model

data class PlaceUiModel(
    val place: Place,
    var isFavorite: Boolean = false,
    var likeCount: Int,
)
