package com.verbum.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "bible_verses",
    primaryKeys = ["languageCode", "bookId", "chapter", "verse"],
    foreignKeys = [
        ForeignKey(
            entity = BibleBookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("bookId"),
        Index("languageCode"),
        Index("languageCode", "bookId"),
        Index("languageCode", "bookId", "chapter"),
    ],
)
data class BibleVerseEntity(
    val languageCode: String,
    val bookId: Int,
    val chapter: Int,
    val verse: Int,
    val text: String,
)
