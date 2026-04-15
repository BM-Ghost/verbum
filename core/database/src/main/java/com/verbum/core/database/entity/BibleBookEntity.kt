package com.verbum.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bible_books")
data class BibleBookEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val abbreviation: String,
    val testament: String, // "OT" or "NT"
    val totalChapters: Int,
    val orderIndex: Int,
)
