package com.yrlee.tpsearchplaceapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "favorite_place")
data class FavoritePlace(
    @PrimaryKey
    var id: String,
    var place_name: String,
    var category_name: String,
    var phone: String,
    var address_name: String,
    var road_address_name: String,
    var longitude: String,
    var latitude: String,
    var place_url: String,
)
