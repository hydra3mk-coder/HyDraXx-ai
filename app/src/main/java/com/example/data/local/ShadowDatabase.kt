package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.AssistantExpression
import com.example.data.model.AssistantTaskNote
import com.example.data.model.ChatMessage
import com.example.data.model.MemoryItem

class Converters {
  @TypeConverter
  fun fromExpression(expression: AssistantExpression): String = expression.name

  @TypeConverter
  fun toExpression(value: String): AssistantExpression {
    return try {
      AssistantExpression.valueOf(value)
    } catch (_: Exception) {
      AssistantExpression.NEUTRAL
    }
  }
}

@Database(
  entities = [MemoryItem::class, ChatMessage::class, AssistantTaskNote::class],
  version = 1,
  exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ShadowDatabase : RoomDatabase() {
  abstract fun memoryDao(): MemoryDao
  abstract fun chatDao(): ChatDao
  abstract fun noteDao(): NoteDao

  companion object {
    @Volatile
    private var INSTANCE: ShadowDatabase? = null

    fun getDatabase(context: Context): ShadowDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          ShadowDatabase::class.java,
          "shadow_companion.db"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
