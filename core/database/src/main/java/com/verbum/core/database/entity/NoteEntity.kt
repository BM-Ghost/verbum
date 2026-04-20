package com.verbum.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    indices = [Index(value = ["bookId", "chapter", "verse"], unique = true)],
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Int,
    val chapter: Int,
    val verse: Int,
    val content: String,
    val updatedAtEpochMillis: Long,
)
