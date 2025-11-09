// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * RetroAchievements API service
 * Fetches achievement data from RetroAchievements.org
 */
interface RetroAchievementsService {

    /**
     * Get game achievements by game ID
     * @param gameId RetroAchievements game ID
     */
    @GET("API_GetGameInfoAndUserProgress.php")
    suspend fun getGameAchievements(
        @Query("g") gameId: Long,
        @Query("u") username: String = ""
    ): GameAchievementsResponse

    /**
     * Get user profile and statistics
     * @param username RetroAchievements username
     */
    @GET("API_GetUserSummary.php")
    suspend fun getUserProfile(
        @Query("u") username: String
    ): UserProfileResponse

    /**
     * Get user's recent achievements
     * @param username RetroAchievements username
     * @param limit Number of achievements to return
     */
    @GET("API_GetUserRecentAchievements.php")
    suspend fun getUserRecentAchievements(
        @Query("u") username: String,
        @Query("z") limit: Int = 10
    ): List<RecentAchievementResponse>
}

// Response Data Classes

data class GameAchievementsResponse(
    @SerializedName("ID")
    val gameId: Long = 0,

    @SerializedName("Title")
    val title: String = "",

    @SerializedName("ImageIcon")
    val imageIcon: String = "",

    @SerializedName("Achievements")
    val achievements: Map<String, AchievementData> = emptyMap(),

    @SerializedName("UserCompletion")
    val userCompletion: UserCompletionData? = null
)

data class AchievementData(
    @SerializedName("ID")
    val id: Int = 0,

    @SerializedName("Title")
    val title: String = "",

    @SerializedName("Description")
    val description: String = "",

    @SerializedName("Points")
    val points: Int = 0,

    @SerializedName("BadgeName")
    val badgeName: String = "",

    @SerializedName("BadgeURL")
    val badgeUrl: String = "",

    @SerializedName("Rarity")
    val rarity: Float = 0f,

    @SerializedName("DateEarned")
    val dateEarned: String? = null
)

data class UserCompletionData(
    @SerializedName("NumPossibleAchievements")
    val totalAchievements: Int = 0,

    @SerializedName("PossibleScore")
    val possibleScore: Int = 0,

    @SerializedName("NumAchieved")
    val numAchieved: Int = 0,

    @SerializedName("ScoreAchieved")
    val scoreAchieved: Int = 0,

    @SerializedName("NumAchievedHardcore")
    val numAchievedHardcore: Int = 0,

    @SerializedName("ScoreAchievedHardcore")
    val scoreAchievedHardcore: Int = 0
)

data class UserProfileResponse(
    @SerializedName("User")
    val username: String = "",

    @SerializedName("UserPic")
    val userPicture: String = "",

    @SerializedName("Status")
    val status: String = "",

    @SerializedName("MemberSince")
    val memberSince: String = "",

    @SerializedName("LastActivity")
    val lastActivity: String = "",

    @SerializedName("Points")
    val points: Int = 0,

    @SerializedName("TruePoints")
    val truePoints: Int = 0,

    @SerializedName("Rank")
    val rank: String = "",

    @SerializedName("RecentAchievements")
    val recentAchievements: Map<String, RecentAchievementData> = emptyMap(),

    @SerializedName("GamesByCompletion")
    val gamesByCompletion: Map<String, GameCompletionData> = emptyMap()
)

data class RecentAchievementData(
    @SerializedName("ID")
    val achievementId: Int = 0,

    @SerializedName("GameID")
    val gameId: Long = 0,

    @SerializedName("GameTitle")
    val gameTitle: String = "",

    @SerializedName("Title")
    val title: String = "",

    @SerializedName("Description")
    val description: String = "",

    @SerializedName("Points")
    val points: Int = 0,

    @SerializedName("BadgeURL")
    val badgeUrl: String = "",

    @SerializedName("DateEarned")
    val dateEarned: String = ""
)

data class RecentAchievementResponse(
    @SerializedName("ID")
    val achievementId: Int = 0,

    @SerializedName("GameID")
    val gameId: Long = 0,

    @SerializedName("GameTitle")
    val gameTitle: String = "",

    @SerializedName("Title")
    val title: String = "",

    @SerializedName("Points")
    val points: Int = 0,

    @SerializedName("BadgeURL")
    val badgeUrl: String = "",

    @SerializedName("DateEarned")
    val dateEarned: String = ""
)

data class GameCompletionData(
    @SerializedName("NumPossibleAchievements")
    val totalAchievements: Int = 0,

    @SerializedName("PossibleScore")
    val possibleScore: Int = 0,

    @SerializedName("NumAchieved")
    val numAchieved: Int = 0,

    @SerializedName("ScoreAchieved")
    val scoreAchieved: Int = 0
)
