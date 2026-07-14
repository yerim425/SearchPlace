package com.yrlee.tpsearchplaceapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yrlee.tpsearchplaceapp.model.Place
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(place: FavoritePlace)

    @Query("SELECT * FROM favorite_place")
    fun getAll(): Flow<List<FavoritePlace>>

    @Query("SELECT id FROM favorite_place")
    fun getAllIds(): Flow<List<String>>

    @Query("DELETE FROM favorite_place WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_place WHERE id=:id)")
    suspend fun isFavorite(id: String): Boolean

}