package com.verbum.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.paging.PagingSource
import com.verbum.core.database.entity.BibleBookEntity
import com.verbum.core.database.entity.BibleVerseEntity
import com.verbum.core.database.entity.BibleCrossReferenceEntity
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

    @Update
    suspend fun updateBooks(books: List<BibleBookEntity>)

    @Query("SELECT COUNT(*) FROM bible_books")
    suspend fun countBooks(): Int

    @Query("DELETE FROM bible_books WHERE id NOT IN (:bookIds)")
    suspend fun deleteBooksNotIn(bookIds: List<Int>)

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

    @Query(
        """
        SELECT * FROM bible_verses 
                WHERE languageCode = :languageCode
                    AND bookId = :bookId
                    AND chapter = :chapter
                    AND verse = :verse
        LIMIT 1
        """
    )
    suspend fun getExactVerse(bookId: Int, chapter: Int, verse: Int, languageCode: String): BibleVerseEntity?

    @Query(
        """
        SELECT * FROM bible_verses 
                WHERE languageCode = :languageCode
                    AND bookId = :bookId
                    AND chapter = :chapter
        ORDER BY verse ASC
        """
    )
    suspend fun getVersesForChapter(bookId: Int, chapter: Int, languageCode: String): List<BibleVerseEntity>

    @Query("SELECT * FROM bible_books WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getBookByName(name: String): BibleBookEntity?

    @Query("SELECT * FROM bible_books WHERE LOWER(name) LIKE LOWER(:pattern) || '%' LIMIT 1")
    suspend fun getBookByNamePrefix(pattern: String): BibleBookEntity?

    @Query("SELECT * FROM bible_books WHERE LOWER(abbreviation) = LOWER(:abbr) LIMIT 1")
    suspend fun getBookByAbbreviation(abbr: String): BibleBookEntity?
    @Query("DELETE FROM bible_verses WHERE languageCode = :languageCode")
    suspend fun deleteVersesByLanguage(languageCode: String)

    @Query("DELETE FROM bible_verses WHERE bookId NOT IN (:bookIds)")
    suspend fun deleteVersesForUnknownBooks(bookIds: List<Int>)

    @Query("SELECT MAX(chapter) FROM bible_verses WHERE bookId = :bookId AND languageCode = :languageCode")
    suspend fun getChapterCount(bookId: Int, languageCode: String): Int?

    @Query("SELECT DISTINCT languageCode FROM bible_verses ORDER BY languageCode ASC")
    suspend fun getAvailableVerseLanguages(): List<String>

    @Query("SELECT COUNT(*) FROM bible_verses WHERE languageCode = :languageCode")
    suspend fun countVerses(languageCode: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerses(verses: List<BibleVerseEntity>)

    @Query(
        """
        SELECT * FROM bible_cross_references
        WHERE fromBookId = :bookId AND fromChapter = :chapter AND fromVerse = :verse
        ORDER BY votes DESC
        LIMIT :limit
        """
    )
    suspend fun getCrossReferences(bookId: Int, chapter: Int, verse: Int, limit: Int = 20): List<BibleCrossReferenceEntity>

    @Query("SELECT COUNT(*) FROM bible_cross_references")
    suspend fun countCrossReferences(): Int

    @Query("DELETE FROM bible_cross_references")
    suspend fun deleteCrossReferences()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossReferences(references: List<BibleCrossReferenceEntity>)
}
