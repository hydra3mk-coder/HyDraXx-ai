package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AssistantTaskNote
import com.example.data.model.ChatMessage
import com.example.data.model.MemoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
  @Query("SELECT * FROM memories ORDER BY timestamp DESC")
  fun getAllMemories(): Flow<List<MemoryItem>>

  @Query("SELECT * FROM memories ORDER BY timestamp DESC")
  suspend fun getAllMemoriesList(): List<MemoryItem>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMemory(memory: MemoryItem): Long

  @Delete
  suspend fun deleteMemory(memory: MemoryItem)

  @Query("DELETE FROM memories WHERE id = :id")
  suspend fun deleteById(id: Long)

  @Query("SELECT * FROM memories WHERE `key` LIKE '%' || :query || '%' OR `value` LIKE '%' || :query || '%'")
  suspend fun searchMemories(query: String): List<MemoryItem>
}

@Dao
interface ChatDao {
  @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
  fun getAllMessages(): Flow<List<ChatMessage>>

  @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT 20")
  suspend fun getRecentMessages(): List<ChatMessage>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: ChatMessage): Long

  @Query("DELETE FROM chat_messages")
  suspend fun clearHistory()
}

@Dao
interface NoteDao {
  @Query("SELECT * FROM assistant_notes ORDER BY createdAt DESC")
  fun getAllNotes(): Flow<List<AssistantTaskNote>>

  @Query("SELECT * FROM assistant_notes ORDER BY createdAt DESC")
  suspend fun getNotesList(): List<AssistantTaskNote>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNote(note: AssistantTaskNote): Long

  @Update
  suspend fun updateNote(note: AssistantTaskNote)

  @Delete
  suspend fun deleteNote(note: AssistantTaskNote)
}
