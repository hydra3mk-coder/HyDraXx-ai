package com.example.ai

interface AiEngine {
  val engineName: String
  val isConfigured: Boolean

  suspend fun generateResponse(
    userPrompt: String,
    systemInstruction: String,
    history: List<Pair<String, Boolean>>, // text to isUser
    memories: List<String>
  ): Result<String>
}
