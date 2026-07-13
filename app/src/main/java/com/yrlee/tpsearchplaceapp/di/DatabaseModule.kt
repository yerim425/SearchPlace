package com.yrlee.tpsearchplaceapp.di

import android.content.Context
import androidx.room.Room
import com.yrlee.tpsearchplaceapp.data.local.AppDatabase
import com.yrlee.tpsearchplaceapp.data.local.FavoriteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "place_db"
        ).build()
    }

    @Provides
    fun provideFavoriteDao(
        db: AppDatabase
    ): FavoriteDao = db.favoriteDao()
}