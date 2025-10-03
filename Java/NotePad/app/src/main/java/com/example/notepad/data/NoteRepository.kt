package com.example.notepad.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class NoteRepository private constructor(context: Context) {
    private val dao = NoteDatabase.get(context).noteDao()

    fun notes(): Flow<List<Note>> = dao.observeAll()

    suspend fun upsert(note: Note) {
        dao.upsert(note.copy(updatedAt = Instant.now().toEpochMilli()))
    }

    suspend fun togglePin(note: Note) = upsert(note.copy(pinned = !note.pinned))
    suspend fun delete(note: Note) = dao.delete(note)

    suspend fun new(title: String, content: String): Long = dao.upsert(
        Note(title = title, content = content)
    )

    suspend fun get(id: Long) = dao.get(id)

    companion object {
        @Volatile private var INSTANCE: NoteRepository? = null
        fun get(context: Context): NoteRepository = INSTANCE ?: synchronized(this) {
            INSTANCE ?: NoteRepository(context.applicationContext).also { INSTANCE = it }
        }
    }
}
