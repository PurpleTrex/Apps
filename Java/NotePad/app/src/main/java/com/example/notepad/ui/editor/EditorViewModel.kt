package com.example.notepad.ui.editor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.data.Note
import com.example.notepad.data.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EditorState(
    val id: Long? = null,
    val title: String = "",
    val content: String = "",
    val isNew: Boolean = true
)

class EditorViewModel(app: Application): AndroidViewModel(app) {
    private val repo = NoteRepository.get(app)
    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state

    fun load(id: Long) = viewModelScope.launch {
        val n = repo.get(id) ?: return@launch
        _state.value = EditorState(id = n.id, title = n.title, content = n.content, isNew = false)
    }

    fun setTitle(t: String) { _state.value = _state.value.copy(title = t) }
    fun setContent(c: String) { _state.value = _state.value.copy(content = c) }

    fun save(onSaved: (Long) -> Unit) = viewModelScope.launch {
        val s = _state.value
        val note = if (s.id == null) {
            val id = repo.new(s.title.trim(), s.content.trim())
            onSaved(id)
            return@launch
        } else {
            Note(id = s.id, title = s.title.trim(), content = s.content.trim())
        }
        repo.upsert(note)
        onSaved(note.id)
    }
}
