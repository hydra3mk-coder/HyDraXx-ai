package com.example.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsManager(private val context: Context) : TextToSpeech.OnInitListener {

  private var tts: TextToSpeech? = null
  private var isInitialized = false

  private val _isSpeaking = MutableStateFlow(false)
  val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

  private var onSpeechDoneCallback: (() -> Unit)? = null

  init {
    tts = TextToSpeech(context, this)
  }

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      isInitialized = true
      tts?.setSpeechRate(1.0f)
      tts?.setPitch(1.05f) // Slightly friendly, intelligent timbre

      tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
          _isSpeaking.value = true
        }

        override fun onDone(utteranceId: String?) {
          _isSpeaking.value = false
          onSpeechDoneCallback?.invoke()
          onSpeechDoneCallback = null
        }

        override fun onError(utteranceId: String?) {
          _isSpeaking.value = false
          onSpeechDoneCallback?.invoke()
          onSpeechDoneCallback = null
        }
      })
    }
  }

  fun speak(text: String, onDone: (() -> Unit)? = null) {
    if (!isInitialized || tts == null) {
      onDone?.invoke()
      return
    }

    this.onSpeechDoneCallback = onDone

    // Auto-detect Arabic characters vs English
    val hasArabic = text.any { it in '\u0600'..'\u06FF' }
    val targetLocale = if (hasArabic) Locale("ar") else Locale.US

    try {
      val result = tts?.setLanguage(targetLocale)
      if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
        tts?.setLanguage(Locale.getDefault())
      }
    } catch (_: Exception) {
      tts?.setLanguage(Locale.getDefault())
    }

    val utteranceId = "shadow_utterance_${System.currentTimeMillis()}"
    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
  }

  fun stop() {
    try {
      tts?.stop()
      _isSpeaking.value = false
    } catch (_: Exception) {}
  }

  fun shutdown() {
    try {
      tts?.stop()
      tts?.shutdown()
    } catch (_: Exception) {}
    tts = null
    isInitialized = false
  }
}
