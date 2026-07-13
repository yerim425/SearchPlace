package com.yrlee.tpsearchplaceapp.data.local

import com.yrlee.tpsearchplaceapp.model.Place
import jakarta.inject.Inject

class FavoriteRepository @Inject constructor(
    private val dao: FavoriteDao
) {

//    val favoriteIds = mutableSetOf<String>() // 좋아요 Set

    val favorites = dao.getAll()

    suspend fun insert(place: Place) {
        dao.insert(place)
    }

    suspend fun delete(id: String) {
        dao.deleteById(id)
    }

    fun getFavoriteIds() = dao.getAllIds()

    suspend fun isFavorite(id: String): Boolean {
        return dao.isFavorite(id)
    }
}