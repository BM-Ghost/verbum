package com.verbum.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "bible_cross_references",
    primaryKeys = ["fromBookId", "fromChapter", "fromVerse", "toBookId", "toChapter", "toVerseStart"],
    indices = [
        Index("fromBookId", "fromChapter", "fromVerse", "votes"),
        Index("toBookId", "toChapter", "toVerseStart"),
    ],
    foreignKeys = [
        ForeignKey(
            entity = BibleBookEntity::class,
            parentColumns = ["id"],
            childColumns = ["fromBookId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = BibleBookEntity::class,
            parentColumns = ["id"],
            childColumns = ["toBookId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class BibleCrossReferenceEntity(
    val fromBookId: Int,
    val fromChapter: Int,
    val fromVerse: Int,
    val toBookId: Int,
    val toChapter: Int,
    val toVerseStart: Int,
    val toVerseEnd: Int,
    val votes: Int,
)