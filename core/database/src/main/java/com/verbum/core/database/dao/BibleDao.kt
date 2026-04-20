package com.verbum.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.paging.PagingSource
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

    @Query("SELECT id FROM bible_books WHERE abbreviation = :abbreviation LIMIT 1")
    suspend fun getBookIdByAbbreviation(abbreviation: String): Int?

    @Query("SELECT id FROM bible_books WHERE name = :bookName LIMIT 1")
    suspend fun getBookIdByName(bookName: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BibleBookEntity>)

    @Query("SELECT COUNT(*) FROM bible_books")
    suspend fun countBooks(): Int

    // ── Verses ──
    @Query("SELECT * FROM bible_verses WHERE languageCode = :languageCode AND bookId = :bookId AND chapter = :chapter ORDER BY verse ASC")
    fun getVerses(bookId: Int, chapter: Int, languageCode: String): Flow<List<BibleVerseEntity>>

    @Query("SELECT * FROM bible_verses WHERE languageCode = :languageCode AND bookId = :bookId AND chapter = :chapter ORDER BY verse ASC")
    fun getVersesPagingSource(bookId: Int, chapter: Int, languageCode: String): PagingSource<Int, BibleVerseEntity>

    @Query("SELECT * FROM bible_verses WHERE languageCode = :languageCode AND bookId = :bookId AND chapter = :chapter AND verse = :verse")
    suspend fun getVerse(bookId: Int, chapter: Int, verse: Int, languageCode: String): BibleVerseEntity?

    @Query(
        """
        SELECT * FROM bible_verses 
                WHERE languageCode = :languageCode
                    AND text LIKE '%' || :query || '%'
        ORDER BY bookId ASC, chapter ASC, verse ASC 
        LIMIT :limit
        """
    )
        suspend fun searchVerses(query: String, languageCode: String, limit: Int = 50): List<BibleVerseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerses(verses: List<BibleVerseEntity>)

    @Query("SELECT COUNT(*) FROM bible_verses")
    suspend fun countVerses(): Int

    @Query("SELECT COUNT(*) FROM bible_verses WHERE languageCode = :languageCode")
    suspend fun countVerses(languageCode: String): Int

    @Query("SELECT MAX(chapter) FROM bible_verses WHERE bookId = :bookId AND languageCode = :languageCode")
    suspend fun getChapterCount(bookId: Int, languageCode: String): Int?

    @Query("SELECT DISTINCT languageCode FROM bible_verses ORDER BY languageCode ASC")
    suspend fun getAvailableVerseLanguages(): List<String>
}
