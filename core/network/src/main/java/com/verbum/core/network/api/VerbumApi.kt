package com.verbum.core.network.api

import com.verbum.core.network.dto.AiChatRequestDto
import com.verbum.core.network.dto.AiChatResponseDto
import com.verbum.core.network.dto.BibleStudyGroupDto
import com.verbum.core.network.dto.CommunityPostDto
import com.verbum.core.network.dto.MissalReadingsDto
import com.verbum.core.network.dto.UserProfileDto
import com.verbum.core.network.dto.VerseOfTheDayDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.ResponseBody
import retrofit2.http.Streaming

interface VerbumApi {

    // ── Missal ──
    @GET("missal/readings/{date}")
    suspend fun getMissalReadings(@Path("date") date: String): MissalReadingsDto

    @GET("missal/verse-of-the-day")
    suspend fun getVerseOfTheDay(): VerseOfTheDayDto

    // ── AI ──
    @POST("ai/chat")
    suspend fun sendAiMessage(@Body request: AiChatRequestDto): AiChatResponseDto

    // ── Community ──
    @GET("community/feed")
    suspend fun getCommunityFeed(
        @Query("page") page: Int,
        @Query("size") size: Int,
    ): List<CommunityPostDto>

    @POST("community/posts")
    suspend fun createPost(@Body post: CommunityPostDto): CommunityPostDto

    @DELETE("community/posts/{id}")
    suspend fun deletePost(@Path("id") postId: String)

    @POST("community/posts/{id}/amen")
    suspend fun amenPost(@Path("id") postId: String)

    // ── Users ──
    @GET("users/{id}")
    suspend fun getUserProfile(@Path("id") userId: String): UserProfileDto

    @POST("users/{id}/follow")
    suspend fun followUser(@Path("id") userId: String)

    @DELETE("users/{id}/follow")
    suspend fun unfollowUser(@Path("id") userId: String)

    // ── Bible Study Groups ──
    @GET("groups")
    suspend fun getStudyGroups(): List<BibleStudyGroupDto>

    @GET("groups/{id}")
    suspend fun getStudyGroup(@Path("id") groupId: String): BibleStudyGroupDto

    @POST("groups")
    suspend fun createStudyGroup(@Body group: BibleStudyGroupDto): BibleStudyGroupDto

    @POST("groups/{id}/join")
    suspend fun joinStudyGroup(@Path("id") groupId: String)
}

interface ScrollmapperApi {
    @Streaming
    @GET("scrollmapper/bible_databases/2025/formats/csv/DRC.csv")
    suspend fun downloadDrc(): ResponseBody
}
