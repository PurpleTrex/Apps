// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing achievements data
 * Abstracts the data layer from the rest of the application
 */
class AchievementsRepository(private val achievementDao: AchievementDao) {

    fun getAchievementsByGame(gameId: Long): Flow<List<Achievement>> {
        return achievementDao.getAchievementsByGame(gameId)
    }

    fun getEarnedAchievementsByGame(gameId: Long): Flow<List<Achievement>> {
        return achievementDao.getEarnedAchievementsByGame(gameId)
    }

    fun getRecentAchievements(limit: Int = 10): Flow<List<Achievement>> {
        return achievementDao.getRecentAchievements(limit)
    }

    fun getTotalAchievementCount(gameId: Long): Flow<Int> {
        return achievementDao.getTotalAchievementCount(gameId)
    }

    fun getEarnedAchievementCount(gameId: Long): Flow<Int> {
        return achievementDao.getEarnedAchievementCount(gameId)
    }

    fun getAchievementById(achievementId: Int): Flow<Achievement> {
        return achievementDao.getAchievementById(achievementId)
    }

    suspend fun insertAchievement(achievement: Achievement) {
        achievementDao.insertAchievement(achievement)
    }

    suspend fun insertAchievements(achievements: List<Achievement>) {
        achievementDao.insertAchievements(achievements)
    }

    suspend fun updateAchievement(achievement: Achievement) {
        achievementDao.updateAchievement(achievement)
    }

    suspend fun deleteAchievement(achievement: Achievement) {
        achievementDao.deleteAchievement(achievement)
    }

    suspend fun deleteAchievementsByGame(gameId: Long) {
        achievementDao.deleteAchievementsByGame(gameId)
    }

    suspend fun awardAchievement(achievementId: Int) {
        achievementDao.awardAchievement(achievementId)
    }
}
