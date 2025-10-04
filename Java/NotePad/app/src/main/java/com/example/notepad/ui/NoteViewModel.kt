package com.example.notepad.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.data.Note
import com.example.notepad.data.NoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class NoteUiState(
    val notes: List<Note> = emptyList(),
    val query: String = "",
    val tagFilter: String? = null,
    val allTags: List<String> = emptyList(),
    val noteTags: Map<Long, List<String>> = emptyMap(),
    val darkTheme: Boolean = true
) {
    private val tagFiltered = tagFilter?.let { tf -> notes.filter { n -> (noteTags[n.id] ?: emptyList()).contains(tf) } } ?: notes
    val filtered = tagFiltered.filter { n -> if (query.isBlank()) true else (n.title.contains(query, true) || (!n.locked && n.content.contains(query, true))) }
}

class NoteViewModel(app: Application): AndroidViewModel(app) {
    private val repo = NoteRepository.get(app)

    private val query = MutableStateFlow("")
    private val notesFlow = repo.notes()
    private val tagFilter = MutableStateFlow<String?>(null)
    private val darkTheme = MutableStateFlow(true)
    private val allTagsFlow = repo.allTags()
    private val noteTagsFlow = repo.tagsMapping()

    val state: StateFlow<NoteUiState> = combine(
        notesFlow,
        query,
        tagFilter,
        allTagsFlow,
        noteTagsFlow,
        darkTheme
    ) { arr: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val notes = arr[0] as List<Note>
        val q = arr[1] as String
        val tagF = arr[2] as String?
        val allTags = arr[3] as List<String>
        val noteTags = arr[4] as Map<Long, List<String>>
        val dark = arr[5] as Boolean
        NoteUiState(notes = notes, query = q, tagFilter = tagF, allTags = allTags, noteTags = noteTags, darkTheme = dark)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NoteUiState())

    fun setQuery(q: String) { query.value = q }

    fun togglePin(note: Note) = viewModelScope.launch { repo.togglePin(note) }
    fun delete(note: Note) = viewModelScope.launch { repo.hardDelete(note) }
    fun setTagFilter(tag: String?) { tagFilter.value = tag }
    fun toggleTheme() { darkTheme.value = !darkTheme.value }
}
