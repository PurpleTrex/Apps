// Copyright PocketBoy Emulator Project / PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.utils

import android.content.SharedPreferences
import android.net.Uri
import androidx.preference.PreferenceManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.pocketboy.emulator.PocketBoyApplication
import com.pocketboy.emulator.NativeLibrary
import com.pocketboy.emulator.R
import com.pocketboy.emulator.model.CheapDocument
import com.pocketboy.emulator.model.Game
import com.pocketboy.emulator.model.GameInfo
import java.io.IOException

object GameHelper {
    const val KEY_GAME_PATH = "game_path"
    const val KEY_GAMES = "Games"

    private lateinit var preferences: SharedPreferences

    fun getGames(): List<Game> {
        val games = mutableListOf<Game>()
        val context = PocketBoyApplication.appContext
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val gamesDir = preferences.getString(KEY_GAME_PATH, "")
        val gamesUri = Uri.parse(gamesDir)

        addGamesRecursive(games, FileUtil.listFiles(gamesUri), 3)
        NativeLibrary.getInstalledGamePaths().forEach {
            games.add(getGame(Uri.parse(it), isInstalled = true, addedToLibrary = true))
        }

        // Cache list of games found on disk
        val serializedGames = mutableSetOf<String>()
        games.forEach {
            serializedGames.add(Json.encodeToString(it))
        }
        preferences.edit()
            .remove(KEY_GAMES)
            .putStringSet(KEY_GAMES, serializedGames)
            .apply()

        return games.toList()
    }

    private fun addGamesRecursive(
        games: MutableList<Game>,
        files: Array<CheapDocument>,
        depth: Int
    ) {
        if (depth <= 0) {
            return
        }

        files.forEach {
            if (it.isDirectory) {
                addGamesRecursive(games, FileUtil.listFiles(it.uri), depth - 1)
            } else {
                if (Game.allExtensions.contains(FileUtil.getExtension(it.uri))) {
                    games.add(getGame(it.uri, isInstalled = false, addedToLibrary = true))
                }
            }
        }
    }

    fun getGame(uri: Uri, isInstalled: Boolean, addedToLibrary: Boolean): Game {
        val filePath = uri.toString()
        var gameInfo: GameInfo? = GameInfo(filePath)

        if (gameInfo?.isValid() == false) {
            gameInfo = null
        }

        val isEncrypted = gameInfo?.isEncrypted() == true

        val newGame = Game(
            (gameInfo?.getTitle() ?: FileUtil.getFilename(uri)).replace("[\\t\\n\\r]+".toRegex(), " "),
            filePath.replace("\n", " "),
            filePath,
            gameInfo?.getTitleID() ?: 0,
            gameInfo?.getCompany() ?: "",
            if (isEncrypted) { PocketBoyApplication.appContext.getString(R.string.unsupported_encrypted) } else { gameInfo?.getRegions() ?: "" },
            isInstalled,
            gameInfo?.isSystemTitle() ?: false,
            gameInfo?.getIsVisibleSystemTitle() ?: false,
            gameInfo?.getIcon(),
            gameInfo?.getFileType() ?: "",
            if (FileUtil.isNativePath(filePath)) {
                PocketBoyApplication.documentsTree.getFilename(filePath)
            } else {
                FileUtil.getFilename(Uri.parse(filePath))
            }
        )

        if (addedToLibrary) {
            val addedTime = preferences.getLong(newGame.keyAddedToLibraryTime, 0L)
            if (addedTime == 0L) {
                preferences.edit()
                    .putLong(newGame.keyAddedToLibraryTime, System.currentTimeMillis())
                    .apply()
            }
        }

        return newGame
    }
}
