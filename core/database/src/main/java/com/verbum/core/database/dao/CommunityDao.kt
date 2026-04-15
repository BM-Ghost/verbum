package com.verbum.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.verbum.core.database.entity.CommunityPostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommunityDao {

    @Query("SELECT * FROM community_posts ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getPosts(limit: Int, offset: Int): List<CommunityPostEntity>

    @Query("SELECT * FROM community_posts ORDER BY createdAt DESC")
    fun observeAllPosts(): Flow<List<CommunityPostEntity>>

    @Query("SELECT * FROM community_posts WHERE id = :postId")
    suspend fun getPostById(postId: String): CommunityPostEntity?

    @Query("SELECT * FROM community_posts WHERE authorId = :authorId ORDER BY createdAt DESC")
    fun getPostsByAuthor(authorId: String): Flow<List<CommunityPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<CommunityPostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPostEntity)

    @Query("DELETE FROM community_posts WHERE id = :postId")
    suspend fun deletePost(postId: String)
}
