package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AssistantExpression(val displayName: String, val quote: String, val arabicQuote: String) {
  NEUTRAL("Neutral", "How can I help you?", "كيف أقدر أساعدك؟"),
  HAPPY("Happy", "I'm here!", "أنا هنا معك!"),
  SLEEPING("Sleeping", "Sleeping...", "نايم في الظل... zZ"),
  CURIOUS("Curious", "Hmm?", "همم؟ فكرني فيها"),
  SURPRISED("Surprised", "Oh!", "أوه! وجدتها!"),
  ANNOYED("Focused", "...", "...مركز تماماً"),
  LOVE("Affection", "Aww~", "من عيوني~"),
  TIRED("Tired", "Tired...", "تعبان شوية..."),
  PEEKING("Peeking", "...", "...أنا أراقبك من الأسفل"),
  LISTENING("Listening", "Listening to you...", "أسمعك بدقة..."),
  SPEAKING("Speaking", "Responding...", "أتحدث إليك...")
}

@Entity(tableName = "memories")
data class MemoryItem(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val key: String,
  val value: String,
  val category: String = "general",
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val text: String,
  val isUser: Boolean,
  val timestamp: Long = System.currentTimeMillis(),
  val expression: AssistantExpression = AssistantExpression.NEUTRAL,
  val actionSummary: String? = null
)

@Entity(tableName = "assistant_notes")
data class AssistantTaskNote(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val content: String,
  val isCompleted: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
)

data class InstalledAppInfo(
  val label: String,
  val packageName: String
)

data class DeviceStatus(
  val batteryLevel: Int,
  val isCharging: Boolean,
  val freeRamMb: Long,
  val totalRamMb: Long,
  val freeStorageGb: Long,
  val totalStorageGb: Long,
  val isWifiConnected: Boolean,
  val isTorchOn: Boolean
)
