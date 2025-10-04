package com.example.notepad.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tags", indices = [Index(value=["name"], unique = true)])
data class Tag(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(tableName = "note_tags", primaryKeys = ["noteId", "tagId"])
data class NoteTagCrossRef(
    val noteId: Long,
    val tagId: Long
)
