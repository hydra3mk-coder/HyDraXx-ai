package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
  ARABIC("ar", "Arabic", "العربية"),
  ENGLISH("en", "English", "English")
}

enum class SleepPhase {
  AWAKE,
  YAWNING,
  DROWSY,
  DEEP_SLEEP,
  WAKING_BURST
}

enum class AssistantExpression(val displayName: String, val quote: String, val arabicQuote: String) {
  NEUTRAL("Neutral", "How can I help you?", "كيف أقدر أساعدك يا صديقي؟"),
  HAPPY("Happy", "I'm here for you!", "أنا هنا معك وبخدمتك!"),
  SLEEPING("Sleeping", "Sleeping in the shadows...", "نائم في الظل... zZ"),
  CURIOUS("Curious", "Hmm? Tell me more...", "همم؟ فكرني فيها أكتر"),
  SURPRISED("Surprised", "Oh! Found it!", "أوه! وجدتها فوراً!"),
  ANNOYED("Focused", "...", "...مركز تماماً على طلبك"),
  LOVE("Affection", "Aww~ With pleasure!", "من عيوني~ فوراً!"),
  TIRED("Tired", "Yawning... Getting sleepy...", "تثاؤب... بدأت أنعس شوية..."),
  PEEKING("Peeking", "Watching over your phone...", "...أراقب جهازك في هدوء"),
  LISTENING("Listening", "Listening to you...", "أسمعك بدقة متناهية..."),
  SPEAKING("Speaking", "Responding...", "أتحدث إليك الآن...")
}

enum class AuthProvider {
  GOOGLE,
  GITHUB,
  GUEST
}

data class AuthUser(
  val uid: String,
  val displayName: String,
  val email: String,
  val avatarUrl: String? = null,
  val provider: AuthProvider = AuthProvider.GUEST,
  val joinedAt: Long = System.currentTimeMillis()
)

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
