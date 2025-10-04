package com.example.notepad.ui.editor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.data.Note
import com.example.notepad.data.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

data class EditorState(
    val id: Long? = null,
    val title: String = "",
    val content: String = "",
    val tags: String = "",
    val folderId: Long? = null,
    val isNew: Boolean = true,
    val locked: Boolean = false,
    val attachments: List<String> = emptyList(), // URIs
    val requireAuth: Boolean = false,
    val markdownPreview: Boolean = false,
    val htmlRendered: String = ""
)

class EditorViewModel(app: Application): AndroidViewModel(app) {
    private val repo = NoteRepository.get(app)
    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state

    fun load(id: Long) = viewModelScope.launch {
        val n = repo.get(id) ?: return@launch
            val decrypted = if (n.locked) {
                when {
                    n.content.startsWith("gcm:") -> com.example.notepad.security.EncryptionUtil.strongDecrypt(getApplication(), n.content)
                    n.content.startsWith("enc:") -> com.example.notepad.security.EncryptionUtil.simpleDecrypt(n.content.removePrefix("enc:"))
                    else -> n.content
                }
            } else if (n.content.startsWith("gcm:")) com.example.notepad.security.EncryptionUtil.strongDecrypt(getApplication(), n.content) else n.content
            _state.value = EditorState(id = n.id, title = n.title, content = decrypted, folderId = n.folderId, locked = n.locked, isNew = false)
    }

    fun setTitle(t: String) { _state.value = _state.value.copy(title = t) }
    fun setContent(c: String) { _state.value = _state.value.copy(content = c) }
    fun setTags(t: String) { _state.value = _state.value.copy(tags = t) }
    fun toggleLocked() { _state.value = _state.value.copy(locked = !_state.value.locked) }
    fun addAttachment(uri: String) { _state.value = _state.value.copy(attachments = _state.value.attachments + uri) }
    fun toggleMarkdown() {
        val s = _state.value
        val new = !s.markdownPreview
        if (new) {
            val parser = Parser.builder().build()
            val doc = parser.parse(s.content)
            val html = HtmlRenderer.builder().build().render(doc)
            _state.value = s.copy(markdownPreview = new, htmlRendered = html)
        } else _state.value = s.copy(markdownPreview = new)
    }

    fun save(onSaved: (Long) -> Unit) = viewModelScope.launch {
        val s = _state.value
        val tags = s.tags.split(',').map { it.trim() }.filter { it.isNotBlank() }
        val note = if (s.id == null) {
            val id = repo.new(s.title.trim(), s.content.trim(), s.folderId, tags)
            onSaved(id)
            return@launch
        } else {
            Note(id = s.id, title = s.title.trim(), content = s.content.trim(), folderId = s.folderId, locked = s.locked)
        }
        repo.upsert(note, tags)
        onSaved(note.id)
    }
}
