package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAiEngine(
  private var customApiKey: String? = null
) : AiEngine {

  override val engineName: String = "Gemini 2.5 Flash"

  private val client = OkHttpClient.Builder()
    .connectTimeout(20, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(20, TimeUnit.SECONDS)
    .build()

  fun setApiKey(key: String) {
    customApiKey = key.trim()
  }

  private fun getEffectiveApiKey(): String {
    val key = customApiKey
    if (!key.isNullOrBlank()) return key

    return try {
      val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
      val buildKey = field.get(null) as? String
      if (!buildKey.isNullOrBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
    } catch (_: Exception) {
      ""
    }
  }

  override val isConfigured: Boolean
    get() = getEffectiveApiKey().isNotBlank()

  override suspend fun generateResponse(
    userPrompt: String,
    systemInstruction: String,
    history: List<Pair<String, Boolean>>,
    memories: List<String>
  ): Result<String> = withContext(Dispatchers.IO) {
    val apiKey = getEffectiveApiKey()
    if (apiKey.isBlank()) {
      return@withContext Result.failure(IllegalStateException("No Gemini API Key configured"))
    }

    try {
      val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

      val rootJson = JSONObject()

      // System instruction with memories injected
      val enrichedSystemInstruction = buildString {
        append(systemInstruction)
        if (memories.isNotEmpty()) {
          append("\n\n--- THINGS YOU REMEMBER ABOUT THIS USER (PERMANENT MEMORY) ---\n")
          memories.forEach { append("- ").append(it).append("\n") }
        }
      }

      val systemObj = JSONObject().apply {
        put("parts", JSONArray().put(JSONObject().put("text", enrichedSystemInstruction)))
      }
      rootJson.put("system_instruction", systemObj)

      // Contents array
      val contentsArray = JSONArray()

      // Recent history turns (up to 8 turns for speed & context)
      val recentTurns = history.takeLast(8)
      for ((msgText, isUser) in recentTurns) {
        val turnObj = JSONObject().apply {
          put("role", if (isUser) "user" else "model")
          put("parts", JSONArray().put(JSONObject().put("text", msgText)))
        }
        contentsArray.put(turnObj)
      }

      // Add current user prompt
      val currentTurn = JSONObject().apply {
        put("role", "user")
        put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
      }
      contentsArray.put(currentTurn)

      rootJson.put("contents", contentsArray)

      // Generation config
      val genConfig = JSONObject().apply {
        put("temperature", 0.7)
        put("maxOutputTokens", 800)
      }
      rootJson.put("generationConfig", genConfig)

      val requestBody = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

      val request = Request.Builder()
        .url(endpoint)
        .post(requestBody)
        .build()

      val response = client.newCall(request).execute()
      val responseBody = response.body?.string().orEmpty()

      if (!response.isSuccessful) {
        val errorMsg = try {
          val errorJson = JSONObject(responseBody).optJSONObject("error")
          errorJson?.optString("message") ?: "HTTP ${response.code}"
        } catch (_: Exception) {
          "HTTP ${response.code}: $responseBody"
        }
        return@withContext Result.failure(Exception("Gemini error: $errorMsg"))
      }

      val parsed = JSONObject(responseBody)
      val candidates = parsed.optJSONArray("candidates")
      if (candidates == null || candidates.length() == 0) {
        return@withContext Result.failure(Exception("No answer received from Gemini"))
      }

      val firstCandidate = candidates.getJSONObject(0)
      val content = firstCandidate.getJSONObject("content")
      val parts = content.getJSONArray("parts")
      val textResponse = parts.getJSONObject(0).getString("text")

      Result.success(textResponse.trim())
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
