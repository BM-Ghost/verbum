package com.verbum.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Int,
    val chapter: Int,
    val verse: Int,
    val note: String? = null,
    val highlightColorHex: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
