// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing user profile data
 * Abstracts the data layer from the rest of the application
 */
class UserProfileRepository(private val userProfileDao: UserProfileDao) {

    fun getUserProfile(): Flow<UserProfile?> {
        return userProfileDao.getUserProfile()
    }

    suspend fun getUserProfileOnce(): UserProfile? {
        return userProfileDao.getUserProfileOnce()
    }

    suspend fun insertUserProfile(profile: UserProfile) {
        userProfileDao.insertUserProfile(profile)
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        userProfileDao.updateUserProfile(profile)
    }

    suspend fun updateProfileStatistics(
        totalPlayTime: Long,
        gamesPlayed: Int,
        totalAchievements: Int
    ) {
        userProfileDao.updateProfileStatistics(totalPlayTime, gamesPlayed, totalAchievements)
    }

    suspend fun linkRetroAchievements(username: String) {
        userProfileDao.linkRetroAchievements(username)
    }

    suspend fun unlinkRetroAchievements() {
        userProfileDao.unlinkRetroAchievements()
    }

    suspend fun updateRetroAchievementsStats(points: Int, rank: String) {
        userProfileDao.updateRetroAchievementsStats(points, rank)
    }

    suspend fun updateUsername(username: String) {
        userProfileDao.updateUsername(username)
    }

    suspend fun updateProfileImage(imageUrl: String) {
        userProfileDao.updateProfileImage(imageUrl)
    }

    suspend fun setDarkMode(enabled: Boolean) {
        userProfileDao.setDarkMode(enabled)
    }

    suspend fun setAchievementNotifications(enabled: Boolean) {
        userProfileDao.setAchievementNotifications(enabled)
    }

    suspend fun ensureProfileExists() {
        val profile = getUserProfileOnce()
        if (profile == null) {
            insertUserProfile(UserProfile.default())
        }
    }
}
