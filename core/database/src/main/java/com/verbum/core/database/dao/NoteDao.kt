package com.verbum.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.verbum.core.database.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE bookId = :bookId AND chapter = :chapter AND verse = :verse LIMIT 1")
    suspend fun getNote(bookId: Int, chapter: Int, verse: Int): NoteEntity?

    @Query("SELECT * FROM notes WHERE bookId = :bookId ORDER BY chapter ASC, verse ASC")
    fun getNotesForBook(bookId: Int): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE bookId = :bookId AND chapter = :chapter AND verse = :verse")
    suspend fun deleteNote(bookId: Int, chapter: Int, verse: Int)
}
