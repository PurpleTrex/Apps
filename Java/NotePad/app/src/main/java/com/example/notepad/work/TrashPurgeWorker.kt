package com.example.notepad.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.notepad.data.NoteRepository

class TrashPurgeWorker(appContext: Context, params: WorkerParameters): CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = try {
        val repo = NoteRepository.get(applicationContext)
        val cutoff = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        repo.purgeTrash(cutoff)
        Result.success()
    } catch (_: Throwable) { Result.retry() }

    companion object { const val TAG = "trash_purge_periodic" }
}
