package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.AssistantExpression
import com.example.ui.components.ChatSheet
import com.example.ui.components.DynamicIslandBar
import com.example.ui.components.ExpressionGallerySheet
import com.example.ui.components.MemoriesSheet
import com.example.ui.components.SettingsSheet
import com.example.ui.components.ShadowFaceView
import com.example.ui.components.ToolsSheet
import com.example.ui.components.VoiceWaveBar
import com.example.ui.theme.ShadowAccentCyan
import com.example.ui.theme.ShadowAccentGreen
import com.example.ui.theme.ShadowAccentPurple
import com.example.ui.theme.ShadowBlack
import com.example.ui.theme.ShadowDarkBackground
import com.example.ui.theme.ShadowGlowMuted
import com.example.ui.theme.ShadowGlowWhite
import com.example.ui.theme.ShadowSurfaceBorder
import com.example.ui.theme.ShadowSurfaceDark
import com.example.ui.theme.ShadowSurfaceElevated
import com.example.voice.VoiceState

@Composable
fun MainAssistantScreen(
  viewModel: AssistantViewModel
) {
  val context = LocalContext.current

  val currentExpression by viewModel.currentExpression.collectAsState()
  val voiceState by viewModel.voiceState.collectAsState()
  val isSpeaking by viewModel.isSpeaking.collectAsState()
  val isThinking by viewModel.isThinking.collectAsState()
  val lastActionStatus by viewModel.lastActionStatus.collectAsState()
  val activeSheet by viewModel.activeSheet.collectAsState()

  val chatMessages by viewModel.chatMessages.collectAsState()
  val memories by viewModel.memories.collectAsState()
  val notes by viewModel.notes.collectAsState()
  val deviceStatus by viewModel.deviceStatus.collectAsState()
  val installedApps by viewModel.installedApps.collectAsState()
  val apiKey by viewModel.customApiKey.collectAsState()
  val elevenLabsKey by viewModel.elevenLabsKey.collectAsState()
  val selectedVoiceId by viewModel.selectedVoiceId.collectAsState()
  val isFloatingActive by viewModel.isFloatingOverlayActive.collectAsState()

  // Audio permission launcher
  var hasAudioPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasAudioPermission = isGranted
    if (isGranted) {
      viewModel.startVoiceInput()
    }
  }

  val isListening = voiceState is VoiceState.Listening
  val rmsLevel = (voiceState as? VoiceState.Listening)?.rmsLevel ?: 0f
  val partialVoiceText = (voiceState as? VoiceState.Listening)?.partialText.orEmpty()

  Scaffold(
    containerColor = ShadowBlack
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(ShadowBlack)
    ) {
      // Subtle background ambient aura
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            brush = Brush.radialGradient(
              colors = listOf(
                ShadowSurfaceElevated.copy(alpha = 0.45f),
                ShadowBlack
              ),
              radius = 900f
            )
          )
      )

      Column(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .navigationBarsPadding()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // --- DYNAMIC ISLAND AT THE TOP ---
        val isIslandExpanded by viewModel.isDynamicIslandExpanded.collectAsState()
        DynamicIslandBar(
          isExpanded = isIslandExpanded,
          expression = currentExpression,
          isListening = isListening,
          isSpeaking = isSpeaking,
          rmsLevel = rmsLevel,
          transcript = partialVoiceText,
          onToggleExpand = { viewModel.toggleDynamicIsland() },
          onMicClick = {
            if (isListening) viewModel.stopVoiceInput()
            else {
              if (hasAudioPermission) viewModel.startVoiceInput()
              else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
          },
          onClose = { viewModel.toggleDynamicIsland(false) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // --- TOP APP BAR ---
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // HyDraXx branding & Intelligence Pill
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (viewModel.isGeminiConfigured) ShadowAccentGreen else ShadowAccentCyan)
            )
            Text(
              text = "HYDRAXX",
              color = ShadowGlowWhite,
              fontSize = 17.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 2.sp
            )
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(ShadowSurfaceDark)
                .border(1.dp, ShadowSurfaceBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
              Text(
                text = if (viewModel.isGeminiConfigured) "Gemini 2.5" else "Offline Brain",
                color = ShadowGlowMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }

          // Top Action Buttons
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            IconButton(onClick = { viewModel.setActiveSheet(AssistantSheet.EXPRESSIONS) }) {
              Icon(Icons.Default.EmojiEmotions, contentDescription = "Expressions", tint = ShadowGlowWhite)
            }
            IconButton(onClick = { viewModel.setActiveSheet(AssistantSheet.TOOLS) }) {
              Icon(Icons.Default.Widgets, contentDescription = "Tools", tint = ShadowGlowWhite)
            }
            IconButton(onClick = { viewModel.setActiveSheet(AssistantSheet.MEMORIES) }) {
              Icon(Icons.Default.Psychology, contentDescription = "Memories", tint = ShadowGlowWhite)
            }
            IconButton(onClick = { viewModel.setActiveSheet(AssistantSheet.SETTINGS) }) {
              Icon(Icons.Default.Settings, contentDescription = "Settings", tint = ShadowGlowWhite)
            }
          }
        }

        Spacer(modifier = Modifier.weight(0.15f))

        // --- CENTER STAGE: LIVING ANIMATED FACE ---
        Box(
          modifier = Modifier.size(280.dp),
          contentAlignment = Alignment.Center
        ) {
          ShadowFaceView(
            expression = currentExpression,
            isListening = isListening,
            isSpeaking = isSpeaking,
            rmsLevel = rmsLevel,
            size = 270.dp,
            onFaceTapped = {
              // Tap to trigger random joyful interaction or listening
              viewModel.setExpression(
                if (currentExpression == AssistantExpression.HAPPY) AssistantExpression.LOVE
                else AssistantExpression.HAPPY
              )
            }
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Pill with current expression quote
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(ShadowSurfaceDark)
            .border(1.dp, ShadowSurfaceBorder, RoundedCornerShape(24.dp))
            .clickable { viewModel.setActiveSheet(AssistantSheet.EXPRESSIONS) }
            .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = if (isListening) ShadowAccentCyan else ShadowGlowWhite,
              modifier = Modifier.size(14.dp)
            )
            Text(
              text = when {
                isListening -> "Listening to you..."
                isThinking -> "Thinking in the shadows..."
                isSpeaking -> "Speaking to you..."
                else -> currentExpression.quote
              },
              color = ShadowGlowWhite,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }

        // Live Voice Partial Transcript / Action Status
        AnimatedVisibility(
          visible = partialVoiceText.isNotBlank() || lastActionStatus != null,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 10.dp)
          ) {
            if (partialVoiceText.isNotBlank()) {
              Text(
                text = "\"$partialVoiceText\"",
                color = ShadowAccentCyan,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
              )
            }

            lastActionStatus?.let { status ->
              Spacer(modifier = Modifier.height(6.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(ShadowSurfaceElevated)
                  .border(1.dp, ShadowAccentGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                  .padding(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(Icons.Default.Info, contentDescription = null, tint = ShadowAccentGreen, modifier = Modifier.size(14.dp))
                  Text(text = status, color = ShadowGlowWhite, fontSize = 12.sp)
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- QUICK ACTION SUGGESTIONS CHIPS ---
        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Special "Hey HyDra!" Island trigger chip
          item {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(ShadowAccentCyan.copy(alpha = 0.2f))
                .border(1.dp, ShadowAccentCyan, RoundedCornerShape(20.dp))
                .clickable { viewModel.triggerHeyHydra() }
                .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
              Text(
                text = "🏝️ Hey HyDra!",
                color = ShadowAccentCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          val suggestions = listOf(
            "🪙 Flip Coin",
            "🎲 Roll Dice",
            "Open YouTube",
            "Turn on Flashlight",
            "Battery Level",
            "Silent Mode",
            "Set Timer 5 min",
            "Remember that I like coffee",
            "افتح الكاميرا",
            "كم البطارية؟"
          )
          items(suggestions) { suggestion ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(ShadowSurfaceDark)
                .border(1.dp, ShadowSurfaceBorder, RoundedCornerShape(20.dp))
                .clickable {
                  viewModel.sendPrompt(suggestion)
                }
                .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
              Text(
                text = suggestion,
                color = ShadowGlowWhite,
                fontSize = 12.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- BOTTOM VOICE INTERACTION DOCK ---
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(ShadowSurfaceDark)
            .border(1.dp, ShadowSurfaceBorder, RoundedCornerShape(32.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Open Chat Transcript Button
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(ShadowSurfaceElevated)
              .border(1.dp, ShadowSurfaceBorder, CircleShape)
              .clickable { viewModel.setActiveSheet(AssistantSheet.CHAT_LOGS) },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Chat,
              contentDescription = "Chat",
              tint = ShadowGlowWhite,
              modifier = Modifier.size(20.dp)
            )
          }

          // Voice Wave Visualizer Bar
          VoiceWaveBar(
            isActive = isListening || isSpeaking,
            rmsLevel = rmsLevel,
            modifier = Modifier.padding(horizontal = 8.dp)
          )

          // Glowing Primary Microphone Action Button
          Box(
            modifier = Modifier
              .size(56.dp)
              .clip(CircleShape)
              .background(if (isListening) ShadowAccentCyan else ShadowGlowWhite)
              .clickable {
                if (isListening) {
                  viewModel.stopVoiceInput()
                } else {
                  if (hasAudioPermission) {
                    viewModel.startVoiceInput()
                  } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                  }
                }
              },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
              contentDescription = if (isListening) "Stop Listening" else "Start Voice Input",
              tint = ShadowBlack,
              modifier = Modifier.size(28.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))
      }

      // --- SHEETS ---
      when (activeSheet) {
        AssistantSheet.EXPRESSIONS -> {
          ExpressionGallerySheet(
            currentExpression = currentExpression,
            onSelectExpression = { exp ->
              viewModel.setExpression(exp)
              viewModel.closeSheet()
            },
            onDismiss = { viewModel.closeSheet() }
          )
        }

        AssistantSheet.CHAT_LOGS -> {
          ChatSheet(
            messages = chatMessages,
            onSendMessage = { text ->
              viewModel.sendPrompt(text)
            },
            onDismiss = { viewModel.closeSheet() }
          )
        }

        AssistantSheet.MEMORIES -> {
          MemoriesSheet(
            memories = memories,
            notes = notes,
            onAddMemory = { k, v -> viewModel.addMemory(k, v) },
            onDeleteMemory = { mem -> viewModel.deleteMemory(mem) },
            onAddNote = { content -> viewModel.addNote(content) },
            onToggleNote = { note -> viewModel.toggleNoteCompleted(note) },
            onDeleteNote = { note -> viewModel.deleteNote(note) },
            onDismiss = { viewModel.closeSheet() }
          )
        }

        AssistantSheet.TOOLS -> {
          ToolsSheet(
            deviceStatus = deviceStatus,
            installedApps = installedApps,
            onToggleTorch = { viewModel.toggleFlashlight() },
            onSetRinger = { mode -> viewModel.setRingerMode(mode) },
            onLaunchApp = { pkg ->
              viewModel.launchApp(pkg)
              viewModel.closeSheet()
            },
            onDismiss = { viewModel.closeSheet() }
          )
        }

        AssistantSheet.SETTINGS -> {
          SettingsSheet(
            apiKey = apiKey,
            elevenLabsKey = elevenLabsKey,
            selectedVoiceId = selectedVoiceId,
            isGeminiActive = viewModel.isGeminiConfigured,
            isFloatingActive = isFloatingActive,
            hasOverlayPermission = viewModel.hasOverlayPermission(),
            onSaveApiKey = { newKey -> viewModel.saveApiKey(newKey) },
            onSaveElevenLabsKey = { newKey -> viewModel.saveElevenLabsKey(newKey) },
            onSelectVoice = { voiceId -> viewModel.selectVoice(voiceId) },
            onToggleFloating = { enable -> viewModel.toggleFloatingOverlay(enable) },
            onOpenDefaultAssistantSettings = { viewModel.openDefaultAssistantSettings() },
            onDismiss = { viewModel.closeSheet() }
          )
        }

        AssistantSheet.NONE -> {}
      }
    }
  }
}
