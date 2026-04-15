package com.verbum.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.verbum.core.database.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query(
        """
        SELECT * FROM bookmarks 
        WHERE bookId = :bookId AND chapter = :chapter AND verse = :verse 
        LIMIT 1
        """
    )
    suspend fun getBookmark(bookId: Int, chapter: Int, verse: Int): BookmarkEntity?

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM bookmarks 
            WHERE bookId = :bookId AND chapter = :chapter AND verse = :verse
        )
        """
    )
    fun isBookmarked(bookId: Int, chapter: Int, verse: Int): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM bookmarks")
    suspend fun getBookmarkCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE bookId = :bookId AND chapter = :chapter AND verse = :verse")
    suspend fun deleteByReference(bookId: Int, chapter: Int, verse: Int)
}
