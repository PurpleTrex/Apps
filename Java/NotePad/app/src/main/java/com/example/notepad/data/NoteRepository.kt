package com.example.notepad.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import com.example.notepad.security.EncryptionUtil

class NoteRepository private constructor(private val context: Context) {
    private val db = NoteDatabase.get(context)
    private val noteDao = db.noteDao()
    private val tagDao = db.tagDao()
    private val attachDao = db.attachmentDao()
    private val revisionDao = db.revisionDao()

    fun notes(): Flow<List<Note>> = noteDao.observeAll()
    fun trash(): Flow<List<Note>> = noteDao.observeTrash()
    fun tagsMapping(): Flow<Map<Long, List<String>>> = tagDao.observeNoteTags().map { list ->
        list.associate { it.noteId to (it.names?.split(',')?.map { n -> n.trim() }?.filter { n -> n.isNotBlank() } ?: emptyList()) }
    }
    fun allTags(): Flow<List<String>> = tagDao.observeAll().map { it.map { t -> t.name } }

    suspend fun upsert(note: Note, tagNames: List<String> = emptyList()) {
        val shouldEncrypt = note.locked
        val newContent = when {
            shouldEncrypt && !(note.content.startsWith("enc:") || note.content.startsWith("gcm:")) -> EncryptionUtil.strongEncrypt(context, note.content)
            !shouldEncrypt && note.content.startsWith("gcm:") -> EncryptionUtil.strongDecrypt(context, note.content)
            !shouldEncrypt && note.content.startsWith("enc:" ) -> EncryptionUtil.simpleDecrypt(note.content.removePrefix("enc:"))
            else -> note.content
        }
        val updated = note.copy(updatedAt = Instant.now().toEpochMilli(), content = newContent)
        val id = noteDao.upsert(updated)
        val noteId = if (updated.id == 0L) id else updated.id
        if (tagNames.isNotEmpty()) tagDao.setTagsForNote(noteId, tagNames)
        revisionDao.insert(
            Revision(
                noteId = noteId,
                title = updated.title,
                content = if (shouldEncrypt) "(encrypted)" else if (updated.content.startsWith("gcm:")) "(encrypted:gcm)" else updated.content,
                timestamp = Instant.now().toEpochMilli()
            )
        )
        revisionDao.prune(noteId)
    }

    suspend fun togglePin(note: Note) = upsert(note.copy(pinned = !note.pinned))
    suspend fun softDelete(note: Note) {
        sanitizeBeforeRemoval(note.id)
        noteDao.softDelete(note.id, Instant.now().toEpochMilli())
    }
    suspend fun hardDelete(note: Note) {
        sanitizeBeforeRemoval(note.id)
        noteDao.hardDelete(note)
    }

    suspend fun new(title: String, content: String, folderId: Long?, tagNames: List<String>): Long {
        val id = noteDao.upsert(
            Note(title = title, content = content, folderId = folderId)
        )
        if (tagNames.isNotEmpty()) tagDao.setTagsForNote(id, tagNames)
        return id
    }

    suspend fun get(id: Long) = noteDao.get(id)

    suspend fun addAttachment(noteId: Long, type: String, uri: String) {
        attachDao.insert(Attachment(noteId = noteId, type = type, uri = uri))
    }

    suspend fun purgeTrash(olderThanMs: Long) = noteDao.purgeDeleted(olderThanMs)
    suspend fun revisions(noteId: Long) = revisionDao.all(noteId)

    private suspend fun sanitizeBeforeRemoval(noteId: Long) {
        val existing = noteDao.get(noteId) ?: return
        val targetLen = existing.content.length.coerceAtLeast(128)
        val filler = buildString(targetLen) { repeat(targetLen) { append((65..90).random().toChar()) } }
        val titleFill = "Deleted-${System.currentTimeMillis()}"
        noteDao.overwriteContent(noteId, filler, titleFill, Instant.now().toEpochMilli())
        attachDao.deleteForNote(noteId)
        revisionDao.deleteForNote(noteId)
        tagDao.deleteTagsForNote(noteId)
    }

    companion object {
        @Volatile private var INSTANCE: NoteRepository? = null
        fun get(context: Context): NoteRepository = INSTANCE ?: synchronized(this) {
            INSTANCE ?: NoteRepository(context.applicationContext).also { INSTANCE = it }
        }
    }
}

