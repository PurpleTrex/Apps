package com.example.notepad.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RevisionDao {
    @Query("SELECT * FROM revisions WHERE noteId = :noteId ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(noteId: Long, limit: Int = 10): Flow<List<Revision>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(revision: Revision)

    @Query("DELETE FROM revisions WHERE noteId = :noteId AND id NOT IN (SELECT id FROM revisions WHERE noteId = :noteId ORDER BY timestamp DESC LIMIT :keep)")
    suspend fun prune(noteId: Long, keep: Int = 20)
    @Query("SELECT * FROM revisions WHERE noteId = :noteId ORDER BY timestamp DESC")
    suspend fun all(noteId: Long): List<Revision>
    @Query("DELETE FROM revisions WHERE noteId = :noteId")
    suspend fun deleteForNote(noteId: Long)
}
