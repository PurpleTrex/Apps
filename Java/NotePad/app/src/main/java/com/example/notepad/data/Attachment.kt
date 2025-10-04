package com.example.notepad.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "attachments")
data class Attachment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val noteId: Long,
    val type: String, // image, file, etc.
    val uri: String,
    val createdAt: Long = Instant.now().toEpochMilli()
)
