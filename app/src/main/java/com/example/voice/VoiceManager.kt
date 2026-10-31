package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class VoiceState {
  object Idle : VoiceState()
  data class Listening(val rmsLevel: Float = 0f, val partialText: String = "") : VoiceState()
  object Processing : VoiceState()
  data class Error(val message: String) : VoiceState()
}

class VoiceManager(private val context: Context) {

  private var speechRecognizer: SpeechRecognizer? = null
  private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
  val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

  private var onSpeechResultListener: ((String) -> Unit)? = null

  fun setOnResultListener(listener: (String) -> Unit) {
    this.onSpeechResultListener = listener
  }

  fun isAvailable(): Boolean {
    return SpeechRecognizer.isRecognitionAvailable(context)
  }

  fun startListening(languageCode: String = "auto") {
    stopListening()

    if (!isAvailable()) {
      _voiceState.value = VoiceState.Error("Speech recognition service not available on this device")
      return
    }

    try {
      speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
        setRecognitionListener(object : RecognitionListener {
          override fun onReadyForSpeech(params: Bundle?) {
            _voiceState.value = VoiceState.Listening(0f, "")
          }

          override fun onBeginningOfSpeech() {
            _voiceState.value = VoiceState.Listening(0.2f, "")
          }

          override fun onRmsChanged(rmsdB: Float) {
            // rmsdB typically ranges from -2 to 10
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            val current = _voiceState.value
            if (current is VoiceState.Listening) {
              _voiceState.value = current.copy(rmsLevel = normalized)
            } else {
              _voiceState.value = VoiceState.Listening(rmsLevel = normalized)
            }
          }

          override fun onBufferReceived(buffer: ByteArray?) {}

          override fun onEndOfSpeech() {
            _voiceState.value = VoiceState.Processing
          }

          override fun onError(error: Int) {
            val errorMsg = when (error) {
              SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
              SpeechRecognizer.ERROR_CLIENT -> "Client side error"
              SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
              SpeechRecognizer.ERROR_NETWORK -> "Network connection error"
              SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
              SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
              SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service busy"
              SpeechRecognizer.ERROR_SERVER -> "Server error"
              SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
              else -> "Voice recognition error ($error)"
            }
            _voiceState.value = VoiceState.Error(errorMsg)
          }

          override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val recognizedText = matches?.firstOrNull().orEmpty()
            _voiceState.value = VoiceState.Idle
            if (recognizedText.isNotBlank()) {
              onSpeechResultListener?.invoke(recognizedText)
            }
          }

          override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val partial = matches?.firstOrNull().orEmpty()
            val current = _voiceState.value
            if (current is VoiceState.Listening) {
              _voiceState.value = current.copy(partialText = partial)
            }
          }

          override fun onEvent(eventType: Int, params: Bundle?) {}
        })
      }

      val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

        if (languageCode == "ar") {
          putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
          putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ar-SA")
        } else if (languageCode == "en") {
          putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
          putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US")
        } else {
          // Auto / device default
          putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
        }
      }

      speechRecognizer?.startListening(intent)
      _voiceState.value = VoiceState.Listening(0f, "")
    } catch (e: Exception) {
      _voiceState.value = VoiceState.Error("Failed to initialize microphone: ${e.localizedMessage}")
    }
  }

  fun stopListening() {
    try {
      speechRecognizer?.stopListening()
      speechRecognizer?.destroy()
    } catch (_: Exception) {}
    speechRecognizer = null
    if (_voiceState.value !is VoiceState.Error) {
      _voiceState.value = VoiceState.Idle
    }
  }
}
