package com.example.notepad.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Note::class, Folder::class, Tag::class, NoteTagCrossRef::class, Attachment::class, Revision::class],
    version = 2,
    exportSchema = false
)
abstract class NoteDatabase: RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao
    abstract fun tagDao(): TagDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun revisionDao(): RevisionDao

    companion object {
        @Volatile private var INSTANCE: NoteDatabase? = null
        fun get(context: Context): NoteDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                NoteDatabase::class.java,
                "notepad.db"
            ).fallbackToDestructiveMigration()
                .addCallback(object: RoomDatabase.Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        // Enable secure_delete using query instead of execSQL to avoid SQLiteException
                        try {
                            db.query("PRAGMA secure_delete=ON")
                        } catch (e: Exception) {
                            // Fallback: ignore if not supported on this device
                            android.util.Log.w("NoteDatabase", "Could not enable secure_delete: ${e.message}")
                        }
                    }
                })
                .build().also { INSTANCE = it }
        }
    }
}

