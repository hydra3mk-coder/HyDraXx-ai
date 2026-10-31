package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AssistantOrchestrator
import com.example.ai.GeminiAiEngine
import com.example.data.local.ShadowDatabase
import com.example.data.model.AppLanguage
import com.example.data.model.AssistantExpression
import com.example.data.model.AssistantTaskNote
import com.example.data.model.AuthProvider
import com.example.data.model.AuthUser
import com.example.data.model.ChatMessage
import com.example.data.model.DeviceStatus
import com.example.data.model.InstalledAppInfo
import com.example.data.model.MemoryItem
import com.example.data.model.SleepPhase
import com.example.service.FloatingAssistantService
import com.example.tools.DeviceToolManager
import com.example.voice.ElevenLabsTtsEngine
import com.example.voice.TtsManager
import com.example.voice.VoiceManager
import com.example.voice.VoiceProfile
import com.example.voice.VoiceState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AssistantSheet {
  NONE,
  CHAT_LOGS,
  MEMORIES,
  TOOLS,
  EXPRESSIONS,
  SETTINGS,
  ONBOARDING
}

class AssistantViewModel(application: Application) : AndroidViewModel(application) {

  private val context = application.applicationContext
  private val database = ShadowDatabase.getDatabase(context)
  private val toolManager = DeviceToolManager(context)
  private val geminiEngine = GeminiAiEngine()
  private val fallbackTts = TtsManager(context)
  val elevenLabsEngine = ElevenLabsTtsEngine(context, fallbackTts)
  private val voiceManager = VoiceManager(context)

  private val orchestrator = AssistantOrchestrator(
    context = context,
    database = database,
    toolManager = toolManager,
    geminiEngine = geminiEngine,
    ttsManager = fallbackTts,
    scope = viewModelScope
  )

  private val sharedPrefs = context.getSharedPreferences("hydraxx_prefs", Context.MODE_PRIVATE)

  val currentExpression: StateFlow<AssistantExpression> = orchestrator.currentExpression
  val isThinking: StateFlow<Boolean> = orchestrator.isThinking
  val isSpeaking: StateFlow<Boolean> = fallbackTts.isSpeaking
  val lastActionStatus: StateFlow<String?> = orchestrator.lastActionStatus
  val voiceState: StateFlow<VoiceState> = voiceManager.voiceState

  val chatMessages = database.chatDao().getAllMessages()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val memories = database.memoryDao().getAllMemories()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val notes = database.noteDao().getAllNotes()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _deviceStatus = MutableStateFlow(toolManager.getDeviceStatus())
  val deviceStatus: StateFlow<DeviceStatus> = _deviceStatus.asStateFlow()

  private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
  val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

  private val _activeSheet = MutableStateFlow(AssistantSheet.NONE)
  val activeSheet: StateFlow<AssistantSheet> = _activeSheet.asStateFlow()

  private val _customApiKey = MutableStateFlow(sharedPrefs.getString("gemini_key", "") ?: "")
  val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

  private val _elevenLabsKey = MutableStateFlow(sharedPrefs.getString("eleven_key", "") ?: "")
  val elevenLabsKey: StateFlow<String> = _elevenLabsKey.asStateFlow()

  private val _selectedVoiceId = MutableStateFlow(
    sharedPrefs.getString("eleven_voice", ElevenLabsTtsEngine.AVAILABLE_VOICES[0].elevenLabsVoiceId)
      ?: ElevenLabsTtsEngine.AVAILABLE_VOICES[0].elevenLabsVoiceId
  )
  val selectedVoiceId: StateFlow<String> = _selectedVoiceId.asStateFlow()

  private val _isFloatingOverlayActive = MutableStateFlow(false)
  val isFloatingOverlayActive: StateFlow<Boolean> = _isFloatingOverlayActive.asStateFlow()

  private val _isDynamicIslandExpanded = MutableStateFlow(false)
  val isDynamicIslandExpanded: StateFlow<Boolean> = _isDynamicIslandExpanded.asStateFlow()

  // Language state (defaults to Arabic, toggleable)
  private val _appLanguage = MutableStateFlow(
    if (sharedPrefs.getString("app_lang", "ar") == "en") AppLanguage.ENGLISH else AppLanguage.ARABIC
  )
  val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

  // Fluid Sleep Transition State
  private val _sleepPhase = MutableStateFlow(SleepPhase.AWAKE)
  val sleepPhase: StateFlow<SleepPhase> = _sleepPhase.asStateFlow()

  // User Profile
  private val _currentUser = MutableStateFlow<AuthUser?>(loadUserFromPrefs())
  val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

  private var lastInteractionTime = System.currentTimeMillis()

  val isGeminiConfigured: Boolean
    get() = geminiEngine.isConfigured

  init {
    // Restore saved API keys
    val savedKey = sharedPrefs.getString("gemini_key", "") ?: ""
    if (savedKey.isNotBlank()) {
      geminiEngine.setApiKey(savedKey)
    }

    val savedElevenKey = sharedPrefs.getString("eleven_key", "") ?: ""
    if (savedElevenKey.isNotBlank()) {
      elevenLabsEngine.setApiKey(savedElevenKey)
    }
    elevenLabsEngine.setVoiceId(_selectedVoiceId.value)

    // Voice recognition listener
    voiceManager.setOnResultListener { spokenText ->
      registerInteraction()
      val lower = spokenText.trim().lowercase()

      // Hotword check: "Hey Hydra", "هاي هيدرا", "هيدرا"
      if (lower.contains("hey hydra") || lower.contains("هاي هيدرا") || lower.contains("hydraxx")) {
        _isDynamicIslandExpanded.value = true
        orchestrator.setExpression(AssistantExpression.HAPPY)
      }

      orchestrator.processUserInput(spokenText, speakOutLoud = true)
    }

    refreshDeviceStatus()
    loadInstalledApps()
    seedInitialMemoriesIfNeeded()
    startIdleSleepTimer()

    // If first launch, show onboarding
    if (!sharedPrefs.getBoolean("has_completed_onboarding", false)) {
      _activeSheet.value = AssistantSheet.ONBOARDING
    }
  }

  private fun loadUserFromPrefs(): AuthUser {
    val uid = sharedPrefs.getString("user_uid", "hydra_dev_1") ?: "hydra_dev_1"
    val name = sharedPrefs.getString("user_name", "Hydra Owner") ?: "Hydra Owner"
    val email = sharedPrefs.getString("user_email", "hydra@device.ai") ?: "hydra@device.ai"
    val providerStr = sharedPrefs.getString("user_provider", "GOOGLE") ?: "GOOGLE"
    val provider = try { AuthProvider.valueOf(providerStr) } catch (_: Exception) { AuthProvider.GOOGLE }
    return AuthUser(uid = uid, displayName = name, email = email, provider = provider)
  }

  fun setLanguage(lang: AppLanguage) {
    _appLanguage.value = lang
    sharedPrefs.edit().putString("app_lang", lang.code).apply()
    registerInteraction()
  }

  fun toggleLanguage() {
    val next = if (_appLanguage.value == AppLanguage.ARABIC) AppLanguage.ENGLISH else AppLanguage.ARABIC
    setLanguage(next)
  }

  fun signInUser(user: AuthUser) {
    _currentUser.value = user
    sharedPrefs.edit()
      .putString("user_uid", user.uid)
      .putString("user_name", user.displayName)
      .putString("user_email", user.email)
      .putString("user_provider", user.provider.name)
      .apply()
    registerInteraction()
  }

  fun completeOnboarding() {
    sharedPrefs.edit().putBoolean("has_completed_onboarding", true).apply()
    _activeSheet.value = AssistantSheet.NONE
    registerInteraction()
    val welcomeMsg = if (_appLanguage.value == AppLanguage.ARABIC)
      "أهلاً بك! مساعد هايدرا جاهز وفي خدمتك دائماً!"
    else
      "Welcome! HyDraXx is awake and ready to serve you!"
    fallbackTts.speak(welcomeMsg)
  }

  fun openOnboarding() {
    _activeSheet.value = AssistantSheet.ONBOARDING
  }

  // Fluid multi-step sleep sequence timer
  private fun startIdleSleepTimer() {
    viewModelScope.launch {
      while (true) {
        delay(2500)
        val elapsed = System.currentTimeMillis() - lastInteractionTime
        val currentExp = currentExpression.value
        val isVoiceBusy = voiceState.value is VoiceState.Listening || isSpeaking.value || isThinking.value

        if (!isVoiceBusy) {
          when {
            elapsed > 45_000 && _sleepPhase.value != SleepPhase.DEEP_SLEEP -> {
              _sleepPhase.value = SleepPhase.DEEP_SLEEP
              orchestrator.setExpression(AssistantExpression.SLEEPING)
            }
            elapsed > 28_000 && _sleepPhase.value != SleepPhase.DROWSY && _sleepPhase.value != SleepPhase.DEEP_SLEEP -> {
              _sleepPhase.value = SleepPhase.DROWSY
              orchestrator.setExpression(AssistantExpression.TIRED)
            }
            elapsed > 18_000 && _sleepPhase.value == SleepPhase.AWAKE && currentExp != AssistantExpression.TIRED -> {
              _sleepPhase.value = SleepPhase.YAWNING
              delay(1600)
              if (System.currentTimeMillis() - lastInteractionTime > 18_000) {
                _sleepPhase.value = SleepPhase.DROWSY
              }
            }
            elapsed > 10_000 && _sleepPhase.value == SleepPhase.AWAKE && currentExp == AssistantExpression.NEUTRAL -> {
              orchestrator.setExpression(AssistantExpression.CURIOUS)
            }
          }
        }
      }
    }
  }

  fun registerInteraction() {
    val wasAsleep = _sleepPhase.value == SleepPhase.DEEP_SLEEP || _sleepPhase.value == SleepPhase.DROWSY
    lastInteractionTime = System.currentTimeMillis()

    if (wasAsleep) {
      viewModelScope.launch {
        _sleepPhase.value = SleepPhase.WAKING_BURST
        orchestrator.setExpression(AssistantExpression.SURPRISED)
        delay(600)
        _sleepPhase.value = SleepPhase.AWAKE
        orchestrator.setExpression(AssistantExpression.HAPPY)
      }
    } else {
      _sleepPhase.value = SleepPhase.AWAKE
    }
  }

  fun toggleDynamicIsland(expand: Boolean? = null) {
    registerInteraction()
    _isDynamicIslandExpanded.value = expand ?: !_isDynamicIslandExpanded.value
  }

  fun triggerHeyHydra() {
    registerInteraction()
    _isDynamicIslandExpanded.value = true
    orchestrator.setExpression(AssistantExpression.HAPPY)
    val quote = if (_appLanguage.value == AppLanguage.ARABIC) "أنا هنا معك!" else "Hey! HyDraXx is here!"
    fallbackTts.speak(quote)
  }

  private fun seedInitialMemoriesIfNeeded() {
    viewModelScope.launch {
      val existing = database.memoryDao().getAllMemoriesList()
      if (existing.isEmpty()) {
        database.memoryDao().insertMemory(
          MemoryItem(key = "Companion Identity", value = "HyDraXx, personal AI on-device companion", category = "identity")
        )
        database.memoryDao().insertMemory(
          MemoryItem(key = "Voice Style", value = "Cute, intelligent, expressive dark companion", category = "preference")
        )
      }
    }
  }

  fun refreshDeviceStatus() {
    _deviceStatus.value = toolManager.getDeviceStatus()
  }

  fun loadInstalledApps() {
    viewModelScope.launch {
      _installedApps.value = toolManager.getInstalledApps()
    }
  }

  fun setActiveSheet(sheet: AssistantSheet) {
    registerInteraction()
    _activeSheet.value = sheet
  }

  fun closeSheet() {
    _activeSheet.value = AssistantSheet.NONE
  }

  fun sendPrompt(prompt: String) {
    registerInteraction()
    orchestrator.processUserInput(prompt, speakOutLoud = true)
  }

  fun startVoiceInput(languageCode: String = "auto") {
    registerInteraction()
    val langParam = if (languageCode != "auto") languageCode else _appLanguage.value.code
    voiceManager.startListening(langParam)
  }

  fun stopVoiceInput() {
    voiceManager.stopListening()
  }

  fun setExpression(expression: AssistantExpression) {
    registerInteraction()
    orchestrator.setExpression(expression)
  }

  fun toggleFlashlight() {
    registerInteraction()
    toolManager.toggleTorch()
    refreshDeviceStatus()
  }

  fun setRingerMode(mode: String) {
    registerInteraction()
    toolManager.setRingerMode(mode)
  }

  fun launchApp(pkg: String) {
    registerInteraction()
    val pm = context.packageManager
    val intent = pm.getLaunchIntentForPackage(pkg)
    if (intent != null) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(intent)
    }
  }

  fun addMemory(key: String, value: String, category: String = "general") {
    if (key.isBlank() || value.isBlank()) return
    registerInteraction()
    viewModelScope.launch {
      database.memoryDao().insertMemory(MemoryItem(key = key.trim(), value = value.trim(), category = category))
    }
  }

  fun deleteMemory(item: MemoryItem) {
    registerInteraction()
    viewModelScope.launch {
      database.memoryDao().deleteMemory(item)
    }
  }

  fun addNote(content: String) {
    if (content.isBlank()) return
    registerInteraction()
    viewModelScope.launch {
      database.noteDao().insertNote(AssistantTaskNote(content = content.trim()))
    }
  }

  fun toggleNoteCompleted(note: AssistantTaskNote) {
    registerInteraction()
    viewModelScope.launch {
      database.noteDao().updateNote(note.copy(isCompleted = !note.isCompleted))
    }
  }

  fun deleteNote(note: AssistantTaskNote) {
    registerInteraction()
    viewModelScope.launch {
      database.noteDao().deleteNote(note)
    }
  }

  fun saveApiKey(key: String) {
    _customApiKey.value = key.trim()
    sharedPrefs.edit().putString("gemini_key", key.trim()).apply()
    geminiEngine.setApiKey(key.trim())
  }

  fun saveElevenLabsKey(key: String) {
    _elevenLabsKey.value = key.trim()
    sharedPrefs.edit().putString("eleven_key", key.trim()).apply()
    elevenLabsEngine.setApiKey(key.trim())
  }

  fun selectVoice(voiceId: String) {
    _selectedVoiceId.value = voiceId
    sharedPrefs.edit().putString("eleven_voice", voiceId).apply()
    elevenLabsEngine.setVoiceId(voiceId)
    viewModelScope.launch {
      val testText = if (_appLanguage.value == AppLanguage.ARABIC) "تم تحديث نبرة صوت هايدرا بنجاح!" else "HyDraXx voice profile updated!"
      elevenLabsEngine.speak(testText)
    }
  }

  fun hasOverlayPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      Settings.canDrawOverlays(context)
    } else true
  }

  fun toggleFloatingOverlay(enable: Boolean) {
    registerInteraction()
    if (enable) {
      if (hasOverlayPermission()) {
        FloatingAssistantService.startService(context)
        _isFloatingOverlayActive.value = true
      }
    } else {
      FloatingAssistantService.stopService(context)
      _isFloatingOverlayActive.value = false
    }
  }

  fun openDefaultAssistantSettings() {
    try {
      val intent = Intent(Settings.ACTION_VOICE_INPUT_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (_: Exception) {
      try {
        val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
      } catch (_: Exception) {
        val intent = Intent(Settings.ACTION_SETTINGS).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    voiceManager.stopListening()
    fallbackTts.shutdown()
    elevenLabsEngine.stop()
  }
}
