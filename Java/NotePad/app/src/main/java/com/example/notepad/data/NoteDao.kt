package com.example.notepad.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE deletedAt IS NULL ORDER BY pinned DESC, updatedAt DESC")
    fun observeAll(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun get(id: Long): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: Note): Long

    @Delete
    suspend fun hardDelete(note: Note)

    @Query("SELECT * FROM notes WHERE deletedAt IS NOT NULL")
    fun observeTrash(): Flow<List<Note>>

    @Query("UPDATE notes SET deletedAt = :ts WHERE id = :id AND deletedAt IS NULL")
    suspend fun softDelete(id: Long, ts: Long)

    @Query("DELETE FROM notes WHERE deletedAt IS NOT NULL AND deletedAt < :olderThan")
    suspend fun purgeDeleted(olderThan: Long)

    @Query("UPDATE notes SET content = :content, title = :title, updatedAt = :updatedAt WHERE id = :id")
    suspend fun overwriteContent(id: Long, content: String, title: String, updatedAt: Long)
}

