package com.verbum.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "community_posts")
data class CommunityPostEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarUrl: String? = null,
    val content: String,
    val verseReference: String? = null,
    val verseText: String? = null,
    val tags: String, // comma-separated: "lent,gospel,prayer"
    val amenCount: Int = 0,
    val commentCount: Int = 0,
    val createdAt: Long,
    val updatedAt: Long,
)
