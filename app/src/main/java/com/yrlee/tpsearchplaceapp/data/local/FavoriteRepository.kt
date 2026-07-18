package com.yrlee.tpsearchplaceapp.data.local

import com.yrlee.tpsearchplaceapp.model.Place
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteRepository @Inject constructor(
    private val dao: FavoriteDao
) {

    val favoriteIds: Flow<Set<String>> =
        dao.getAllIds()
            .map { it.toSet() } // set으로 바꾸는 것이 조회 속도가 빠름

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

//    fun getFavoriteIds() = dao.getAllIds()

    fun getAll() = dao.getAll()


    suspend fun isFavorite(id: String): Boolean {
        return dao.isFavorite(id)
    }
}