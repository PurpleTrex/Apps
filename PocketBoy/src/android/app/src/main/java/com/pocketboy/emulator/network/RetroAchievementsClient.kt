// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit client for RetroAchievements API
 */
object RetroAchievementsClient {
    private const val BASE_URL = "https://retroachievements.org/"

    private var service: RetroAchievementsService? = null

    fun getInstance(): RetroAchievementsService {
        return service ?: synchronized(this) {
            service ?: createService().also { service = it }
        }
    }

    private fun createService(): RetroAchievementsService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(RetroAchievementsService::class.java)
    }
}
