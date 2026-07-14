package com.yrlee.tpsearchplaceapp.model

import com.yrlee.tpsearchplaceapp.data.local.FavoritePlace

data class FavoriteUiModel(
    val place: FavoritePlace,
    var distance: String
)
