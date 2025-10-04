package com.example.notepad.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "revisions")
data class Revision(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val noteId: Long,
    val title: String,
    val content: String,
    val timestamp: Long
)
