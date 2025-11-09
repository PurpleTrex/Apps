// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Main Room database for PocketBoy
 * Handles storage of achievements, game statistics, and user profile data
 */
@Database(
    entities = [
        Achievement::class,
        GameStatistics::class,
        UserProfile::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PocketBoyDatabase : RoomDatabase() {
    abstract fun achievementDao(): AchievementDao
    abstract fun gameStatisticsDao(): GameStatisticsDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        private const val DATABASE_NAME = "pocketboy_database"

        @Volatile
        private var instance: PocketBoyDatabase? = null

        fun getInstance(context: Context): PocketBoyDatabase {
            return instance ?: synchronized(this) {
                instance ?: createDatabase(context).also { instance = it }
            }
        }

        private fun createDatabase(context: Context): PocketBoyDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                PocketBoyDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
