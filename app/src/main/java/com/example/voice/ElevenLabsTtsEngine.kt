package com.example.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class VoiceProfile(
  val id: String,
  val name: String,
  val description: String,
  val elevenLabsVoiceId: String
)

class ElevenLabsTtsEngine(
  private val context: Context,
  private val fallbackTts: TtsManager
) {

  companion object {
    val AVAILABLE_VOICES = listOf(
      VoiceProfile("bella", "Bella (Cute & Soft)", "Gentle, cute and expressive companion voice", "EXAVITQu4vr4xnSDxMaL"),
      VoiceProfile("elli", "Elli (Playful & Energetic)", "Cute, lively and high energy companion", "MF3mGyEYCl7XYWbV9V6O"),
      VoiceProfile("rachel", "Rachel (Calm & Intelligent)", "Crisp, smart and friendly companion tone", "21m00Tcm4TlvDq8ikWAM"),
      VoiceProfile("antoni", "Antoni (Deep & Loyal)", "Warm, deep and respectful guardian tone", "ErXwobaYiN019PkySvjV"),
      VoiceProfile("josh", "Josh (Tech & Witty)", "Natural, thoughtful assistant voice", "TxGEqnHWrfWFTfGW9XjX")
    )
  }

  private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(25, TimeUnit.SECONDS)
    .build()

  private var mediaPlayer: MediaPlayer? = null
  private var elevenLabsApiKey: String = ""
  private var selectedVoiceId: String = AVAILABLE_VOICES[0].elevenLabsVoiceId

  private val _isSpeaking = MutableStateFlow(false)
  val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

  fun setApiKey(key: String) {
    elevenLabsApiKey = key.trim()
  }

  fun setVoiceId(voiceId: String) {
    selectedVoiceId = voiceId
  }

  val hasApiKey: Boolean
    get() = elevenLabsApiKey.isNotBlank()

  suspend fun speak(text: String, onDone: (() -> Unit)? = null) {
    if (!hasApiKey) {
      // Gracefully use local TTS with cute companion pitch
      fallbackTts.speak(text, onDone)
      return
    }

    withContext(Dispatchers.IO) {
      try {
        val endpoint = "https://api.elevenlabs.io/v1/text-to-speech/$selectedVoiceId"

        val json = JSONObject().apply {
          put("text", text)
          put("model_id", "eleven_multilingual_v2")
          val voiceSettings = JSONObject().apply {
            put("stability", 0.55)
            put("similarity_boost", 0.8)
            put("use_speaker_boost", true)
          }
          put("voice_settings", voiceSettings)
        }

        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
          .url(endpoint)
          .addHeader("xi-api-key", elevenLabsApiKey)
          .addHeader("Accept", "audio/mpeg")
          .post(requestBody)
          .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful || response.body == null) {
          // Fallback to local TTS
          withContext(Dispatchers.Main) {
            fallbackTts.speak(text, onDone)
          }
          return@withContext
        }

        val tempAudio = File(context.cacheDir, "hydra_speech_${System.currentTimeMillis()}.mp3")
        FileOutputStream(tempAudio).use { output ->
          response.body!!.byteStream().copyTo(output)
        }

        withContext(Dispatchers.Main) {
          playAudioFile(tempAudio, onDone)
        }
      } catch (e: Exception) {
        withContext(Dispatchers.Main) {
          fallbackTts.speak(text, onDone)
        }
      }
    }
  }

  private fun playAudioFile(file: File, onDone: (() -> Unit)?) {
    stop()
    try {
      mediaPlayer = MediaPlayer().apply {
        setAudioAttributes(
          AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
            .build()
        )
        setDataSource(file.absolutePath)
        prepare()
        _isSpeaking.value = true
        setOnCompletionListener {
          _isSpeaking.value = false
          it.release()
          mediaPlayer = null
          file.delete()
          onDone?.invoke()
        }
        setOnErrorListener { _, _, _ ->
          _isSpeaking.value = false
          mediaPlayer?.release()
          mediaPlayer = null
          file.delete()
          onDone?.invoke()
          true
        }
        start()
      }
    } catch (_: Exception) {
      _isSpeaking.value = false
      onDone?.invoke()
    }
  }

  fun stop() {
    try {
      mediaPlayer?.stop()
      mediaPlayer?.release()
      mediaPlayer = null
    } catch (_: Exception) {}
    _isSpeaking.value = false
    fallbackTts.stop()
  }
}
