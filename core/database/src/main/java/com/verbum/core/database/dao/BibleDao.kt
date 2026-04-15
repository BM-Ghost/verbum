package com.verbum.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.verbum.core.database.entity.BibleBookEntity
import com.verbum.core.database.entity.BibleVerseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BibleDao {

    // ── Books ──
    @Query("SELECT * FROM bible_books ORDER BY orderIndex ASC")
    fun getAllBooks(): Flow<List<BibleBookEntity>>

    @Query("SELECT * FROM bible_books WHERE testament = :testament ORDER BY orderIndex ASC")
    fun getBooksByTestament(testament: String): Flow<List<BibleBookEntity>>

    @Query("SELECT * FROM bible_books WHERE id = :bookId")
    suspend fun getBookById(bookId: Int): BibleBookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BibleBookEntity>)

    // ── Verses ──
    @Query("SELECT * FROM bible_verses WHERE bookId = :bookId AND chapter = :chapter ORDER BY verse ASC")
    fun getVerses(bookId: Int, chapter: Int): Flow<List<BibleVerseEntity>>

    @Query("SELECT * FROM bible_verses WHERE bookId = :bookId AND chapter = :chapter AND verse = :verse")
    suspend fun getVerse(bookId: Int, chapter: Int, verse: Int): BibleVerseEntity?

    @Query(
        """
        SELECT * FROM bible_verses 
        WHERE text LIKE '%' || :query || '%' 
        ORDER BY bookId ASC, chapter ASC, verse ASC 
        LIMIT :limit
        """
    )
    suspend fun searchVerses(query: String, limit: Int = 50): List<BibleVerseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerses(verses: List<BibleVerseEntity>)

    @Query("SELECT MAX(chapter) FROM bible_verses WHERE bookId = :bookId")
    suspend fun getChapterCount(bookId: Int): Int?
}
