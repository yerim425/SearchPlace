package com.yrlee.tpsearchplaceapp.model

data class DothomeResponse(
    var resultCode: Int,
    var resultMessage: String,
    var totalCount: Int,
    var data: List<Place>?
)
