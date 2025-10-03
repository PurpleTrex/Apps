package com.example.notepad.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY pinned DESC, updatedAt DESC")
    fun observeAll(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun get(id: Long): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: Note): Long

    @Delete
    suspend fun delete(note: Note)

    @Query("DELETE FROM notes WHERE id IN (:ids)")
    suspend fun deleteMany(ids: List<Long>)
}
