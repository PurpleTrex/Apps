package com.example.notepad.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.data.Note
import com.example.notepad.data.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class NoteUiState(
    val notes: List<Note> = emptyList(),
    val query: String = ""
) {
    val filtered = if (query.isBlank()) notes else notes.filter { it.title.contains(query, true) || it.content.contains(query, true) }
}

class NoteViewModel(app: Application): AndroidViewModel(app) {
    private val repo = NoteRepository.get(app)

    private val query = MutableStateFlow("")
    private val notesFlow = repo.notes()

    val state: StateFlow<NoteUiState> = combine(notesFlow, query) { notes, q ->
        NoteUiState(notes = notes, query = q)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NoteUiState())

    fun setQuery(q: String) { query.value = q }

    fun togglePin(note: Note) = viewModelScope.launch { repo.togglePin(note) }
    fun delete(note: Note) = viewModelScope.launch { repo.delete(note) }
}
