package com.example.notepad

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.notepad.data.Note
import com.example.notepad.data.NoteDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteDaoTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val db = Room.inMemoryDatabaseBuilder(context, NoteDatabase::class.java).allowMainThreadQueries().build()
    private val dao = db.noteDao()

    @Test
    fun insertAndObserve() = runBlocking {
        dao.upsert(Note(title = "Test", content = "Body"))
        val notes = dao.observeAll().first()
        assertTrue(notes.any { it.title == "Test" })
    }

    @Test
    fun softDeleteExcludesFromActive() = runBlocking {
        val id = dao.upsert(Note(title = "Trash", content = "X"))
        dao.softDelete(id, System.currentTimeMillis())
        val active = dao.observeAll().first()
        assertTrue(active.none { it.id == id })
    }
}
