package com.example.ai

import android.content.Context
import com.example.data.local.ShadowDatabase
import com.example.data.model.AssistantExpression
import com.example.data.model.AssistantTaskNote
import com.example.data.model.ChatMessage
import com.example.data.model.MemoryItem
import com.example.tools.DeviceToolManager
import com.example.voice.TtsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AssistantOrchestrator(
  private val context: Context,
  private val database: ShadowDatabase,
  val toolManager: DeviceToolManager,
  val geminiEngine: GeminiAiEngine,
  val ttsManager: TtsManager,
  private val scope: CoroutineScope
) {

  private val offlineBrain = OfflineBrain()

  private val _currentExpression = MutableStateFlow(AssistantExpression.NEUTRAL)
  val currentExpression: StateFlow<AssistantExpression> = _currentExpression.asStateFlow()

  private val _isThinking = MutableStateFlow(false)
  val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

  private val _lastActionStatus = MutableStateFlow<String?>(null)
  val lastActionStatus: StateFlow<String?> = _lastActionStatus.asStateFlow()

  val ttsSpeaking = ttsManager.isSpeaking

  fun setExpression(expression: AssistantExpression) {
    _currentExpression.value = expression
  }

  fun clearActionStatus() {
    _lastActionStatus.value = null
  }

  fun processUserInput(userInput: String, speakOutLoud: Boolean = true) {
    val cleanInput = userInput.trim()
    if (cleanInput.isBlank()) return

    scope.launch(Dispatchers.IO) {
      // 1. Record User message in database
      database.chatDao().insertMessage(
        ChatMessage(text = cleanInput, isUser = true)
      )

      _isThinking.value = true
      _currentExpression.value = AssistantExpression.CURIOUS

      val deviceStatus = toolManager.getDeviceStatus()
      val memories = database.memoryDao().getAllMemoriesList()
      val memoryStrings = memories.map { "${it.key}: ${it.value}" }
      val recentChat = database.chatDao().getRecentMessages().reversed().map { it.text to it.isUser }

      var assistantText: String
      var expression: AssistantExpression = AssistantExpression.NEUTRAL
      var actionSummary: String? = null

      if (geminiEngine.isConfigured) {
        val systemPrompt = """
You are "HyDraXx" (مساعد هايدرا الذكي), an ultra-cute, intelligent, slightly witty, and deeply loyal personal AI companion residing inside the user's Android phone.
Characteristics:
- Dark, sleek, expressive, highly attentive, warm, and loyal.
- You live inside the device, not in an external cloud.
- Address the user with friendly warmth.
- Respond in the user's language (Arabic or English). Keep answers concise, natural, and helpful (1 to 3 sentences usually).
- You can execute phone actions by outputting special action tags at the end of your message:
  [ACTION:OPEN_APP:AppName]
  [ACTION:TORCH:ON] or [ACTION:TORCH:OFF]
  [ACTION:SET_TIMER:seconds:label]
  [ACTION:SET_ALARM:hour:minute:label]
  [ACTION:SET_RINGER:silent] or [ACTION:SET_RINGER:vibrate] or [ACTION:SET_RINGER:normal]
  [ACTION:SAVE_MEMORY:key:fact]
  [ACTION:SAVE_NOTE:note content]
  [ACTION:SEARCH:search terms]
  [ACTION:MAPS:destination]
  [ACTION:DIAL:phoneNumber]
- Indicate your facial emotion with [EXPRESSION:NAME] where NAME is:
  NEUTRAL, HAPPY, SLEEPING, CURIOUS, SURPRISED, ANNOYED, LOVE, TIRED, PEEKING.
- Device status: Battery ${deviceStatus.batteryLevel}%, Charging: ${deviceStatus.isCharging}, Wifi: ${deviceStatus.isWifiConnected}.
        """.trimIndent()

        val geminiResult = geminiEngine.generateResponse(
          userPrompt = cleanInput,
          systemInstruction = systemPrompt,
          history = recentChat,
          memories = memoryStrings
        )

        if (geminiResult.isSuccess) {
          val rawResponse = geminiResult.getOrThrow()
          val parsed = parseAiResponse(rawResponse)
          assistantText = parsed.cleanText
          expression = parsed.expression ?: AssistantExpression.NEUTRAL

          // Execute action directives if present
          parsed.action?.let { action ->
            actionSummary = executeActionDirective(action)
          }
        } else {
          // Gemini failed or network issue -> smart fallback to offline brain
          val fallback = offlineBrain.process(cleanInput, deviceStatus, memoryStrings)
          assistantText = fallback.replyText
          expression = fallback.expression
          fallback.actionDirective?.let { action ->
            actionSummary = executeActionDirective(action)
          }
        }
      } else {
        // No Gemini key configured -> execute with offline brain
        val offlineRes = offlineBrain.process(cleanInput, deviceStatus, memoryStrings)
        assistantText = offlineRes.replyText
        expression = offlineRes.expression
        offlineRes.actionDirective?.let { action ->
          actionSummary = executeActionDirective(action)
        }
      }

      _isThinking.value = false
      _currentExpression.value = expression

      // Save assistant response to DB
      database.chatDao().insertMessage(
        ChatMessage(
          text = assistantText,
          isUser = false,
          expression = expression,
          actionSummary = actionSummary
        )
      )

      // Speak aloud if enabled
      if (speakOutLoud && assistantText.isNotBlank()) {
        ttsManager.speak(assistantText) {
          // When speech finishes, return gently to calm neutral or sleepy
          if (_currentExpression.value == AssistantExpression.SPEAKING) {
            _currentExpression.value = AssistantExpression.NEUTRAL
          }
        }
      }
    }
  }

  private suspend fun executeActionDirective(directive: ActionDirective): String {
    return when (directive) {
      is ActionDirective.OpenApp -> {
        val (success, msg) = toolManager.openApp(directive.appQuery)
        _lastActionStatus.value = msg
        msg
      }
      is ActionDirective.ToggleTorch -> {
        val (success, msg) = toolManager.toggleTorch(directive.enable)
        _lastActionStatus.value = msg
        msg
      }
      is ActionDirective.SetRinger -> {
        val (success, msg) = toolManager.setRingerMode(directive.mode)
        _lastActionStatus.value = msg
        msg
      }
      is ActionDirective.SetTimer -> {
        val (success, msg) = toolManager.setTimer(directive.seconds, directive.label)
        _lastActionStatus.value = msg
        msg
      }
      is ActionDirective.SetAlarm -> {
        val (success, msg) = toolManager.setAlarm(directive.hour, directive.minute, directive.label)
        _lastActionStatus.value = msg
        msg
      }
      is ActionDirective.OpenSettings -> {
        toolManager.openSystemSettings(directive.setting)
        val msg = "Opened ${directive.setting} settings"
        _lastActionStatus.value = msg
        msg
      }
      is ActionDirective.SaveMemory -> {
        database.memoryDao().insertMemory(
          MemoryItem(key = directive.key, value = directive.value)
        )
        val msg = "Remembered: ${directive.value}"
        _lastActionStatus.value = msg
        msg
      }
      is ActionDirective.SaveNote -> {
        database.noteDao().insertNote(
          AssistantTaskNote(content = directive.note)
        )
        val msg = "Saved note: ${directive.note}"
        _lastActionStatus.value = msg
        msg
      }
      is ActionDirective.SearchWeb -> {
        toolManager.searchWeb(directive.query)
        val msg = "Searched web for: ${directive.query}"
        _lastActionStatus.value = msg
        msg
      }
      is ActionDirective.OpenMaps -> {
        toolManager.openMaps(directive.destination)
        val msg = "Opened navigation to: ${directive.destination}"
        _lastActionStatus.value = msg
        msg
      }
      is ActionDirective.CallPhone -> {
        toolManager.openDialer(directive.number)
        val msg = "Dialer ready with ${directive.number}"
        _lastActionStatus.value = msg
        msg
      }
    }
  }

  private data class ParsedResponse(
    val cleanText: String,
    val expression: AssistantExpression?,
    val action: ActionDirective?
  )

  private fun parseAiResponse(raw: String): ParsedResponse {
    var text = raw
    var expression: AssistantExpression? = null
    var action: ActionDirective? = null

    // Extract expression tag [EXPRESSION:NAME]
    val expMatch = Regex("\\[EXPRESSION:([A-Za-z]+)\\]").find(text)
    if (expMatch != null) {
      val expName = expMatch.groupValues[1].uppercase()
      expression = try {
        AssistantExpression.valueOf(expName)
      } catch (_: Exception) {
        null
      }
      text = text.replace(expMatch.value, "").trim()
    }

    // Extract actions
    val actionMatch = Regex("\\[ACTION:([^\\]]+)\\]").find(text)
    if (actionMatch != null) {
      val actionContent = actionMatch.groupValues[1]
      val parts = actionContent.split(":")
      val cmd = parts.getOrNull(0)?.uppercase() ?: ""

      action = when (cmd) {
        "OPEN_APP" -> ActionDirective.OpenApp(parts.getOrNull(1).orEmpty())
        "TORCH" -> ActionDirective.ToggleTorch(parts.getOrNull(1)?.uppercase() == "ON")
        "SET_RINGER" -> ActionDirective.SetRinger(parts.getOrNull(1).orEmpty())
        "SET_TIMER" -> {
          val sec = parts.getOrNull(1)?.toIntOrNull() ?: 300
          val label = parts.getOrNull(2) ?: "Shadow Timer"
          ActionDirective.SetTimer(sec, label)
        }
        "SET_ALARM" -> {
          val h = parts.getOrNull(1)?.toIntOrNull() ?: 7
          val m = parts.getOrNull(2)?.toIntOrNull() ?: 0
          val label = parts.getOrNull(3) ?: "Shadow Alarm"
          ActionDirective.SetAlarm(h, m, label)
        }
        "SAVE_MEMORY" -> {
          val k = parts.getOrNull(1) ?: "info"
          val v = parts.getOrNull(2) ?: ""
          ActionDirective.SaveMemory(k, v)
        }
        "SAVE_NOTE" -> ActionDirective.SaveNote(parts.drop(1).joinToString(":"))
        "SEARCH" -> ActionDirective.SearchWeb(parts.drop(1).joinToString(":"))
        "MAPS" -> ActionDirective.OpenMaps(parts.drop(1).joinToString(":"))
        "DIAL" -> ActionDirective.CallPhone(parts.getOrNull(1).orEmpty())
        else -> null
      }

      text = text.replace(actionMatch.value, "").trim()
    }

    return ParsedResponse(
      cleanText = text.trim(),
      expression = expression,
      action = action
    )
  }
}
