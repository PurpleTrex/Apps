package com.example.notepad.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class NoteWithTags(
    @Embedded val note: Note,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteTagCrossRef::class,
            parentColumn = "noteId",
            entityColumn = "tagId"
        )
    )
    val tags: List<Tag>
)

@Dao
interface TagDao {
    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun observeAll(): Flow<List<Tag>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: Tag): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCrossRef(crossRef: NoteTagCrossRef)

    @Query("DELETE FROM note_tags WHERE noteId = :noteId")
    suspend fun clearNoteTags(noteId: Long)

    @Transaction
    suspend fun setTagsForNote(noteId: Long, tagNames: List<String>) {
        clearNoteTags(noteId)
        tagNames.map { it.trim() }.filter { it.isNotBlank() }.forEach { name ->
            val existingId = insert(Tag(name = name)).let { if (it == -1L) null else it }
            val tagId = existingId ?: findByName(name)?.id ?: return@forEach
            addCrossRef(NoteTagCrossRef(noteId, tagId))
        }
    }

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): Tag?

    // Aggregated mapping of noteId -> CSV of tag names
    @Query("SELECT noteId, group_concat(tags.name, ',') as names FROM note_tags JOIN tags ON tags.id = note_tags.tagId GROUP BY noteId")
    fun observeNoteTags(): Flow<List<NoteTagsAggregate>>

    @Query("DELETE FROM note_tags WHERE noteId = :noteId")
    suspend fun deleteTagsForNote(noteId: Long)
}

data class NoteTagsAggregate(
    val noteId: Long,
    val names: String?
)
