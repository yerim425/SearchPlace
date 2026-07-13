package com.yrlee.tpsearchplaceapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yrlee.tpsearchplaceapp.model.Place

@Database(
    entities = [Place::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoriteDao(): FavoriteDao
}