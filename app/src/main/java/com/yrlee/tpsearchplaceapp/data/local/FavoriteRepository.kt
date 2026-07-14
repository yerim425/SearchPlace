package com.yrlee.tpsearchplaceapp.data.local

import com.yrlee.tpsearchplaceapp.model.Place
import jakarta.inject.Inject

class FavoriteRepository @Inject constructor(
    private val dao: FavoriteDao
) {

//    val favoriteIds = mutableSetOf<String>() // 좋아요 Set

    val favorites = dao.getAll()

    //data class FavoritePlace(
    //    @PrimaryKey
    //    var id: String,
    //    var place_name: String,
    //    var category_name: String,
    //    var phone: String,
    //    var address_name: String,
    //    var road_address_name: String,
    //    @SerializedName("x") var longitude: String,
    //    @SerializedName("y") var latitude: String,
    //    var place_url: String,
    //)
    suspend fun insert(p: Place) {
        val fp = FavoritePlace(
            id = p.id,
            place_name = p.place_name,
            category_name = p.category_name,
            phone = p.phone,
            address_name = p.address_name,
            road_address_name = p.road_address_name,
            longitude = p.longitude,
            latitude = p.latitude,
            place_url = p.place_url
        )
        dao.insert(fp)
    }

    suspend fun delete(id: String) {
        dao.deleteById(id)
    }

    fun getFavoriteIds() = dao.getAllIds()

    fun getAll() = dao.getAll()


    suspend fun isFavorite(id: String): Boolean {
        return dao.isFavorite(id)
    }
}