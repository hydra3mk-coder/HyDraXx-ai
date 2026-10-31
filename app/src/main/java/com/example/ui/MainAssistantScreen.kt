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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.WbSunny
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
import com.example.data.model.AppLanguage
import com.example.data.model.AssistantExpression
import com.example.data.model.SleepPhase
import com.example.ui.components.ChatSheet
import com.example.ui.components.DynamicIslandBar
import com.example.ui.components.ExpressionGallerySheet
import com.example.ui.components.MemoriesSheet
import com.example.ui.components.OnboardingSheet
import com.example.ui.components.SettingsSheet
import com.example.ui.components.ShadowFaceView
import com.example.ui.components.ToolsSheet
import com.example.ui.components.VoiceWaveBar
import com.example.ui.theme.ShadowAccentAmber
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
  val sleepPhase by viewModel.sleepPhase.collectAsState()
  val appLanguage by viewModel.appLanguage.collectAsState()
  val currentUser by viewModel.currentUser.collectAsState()

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
  val isIslandExpanded by viewModel.isDynamicIslandExpanded.collectAsState()

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
  val isAsleep = sleepPhase == SleepPhase.DEEP_SLEEP || currentExpression == AssistantExpression.SLEEPING

  Scaffold(
    containerColor = ShadowBlack
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(ShadowBlack)
    ) {
      // Background ambient aura with subtle purple in sleep mode
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            brush = Brush.radialGradient(
              colors = listOf(
                if (isAsleep) ShadowAccentPurple.copy(alpha = 0.20f) else ShadowSurfaceElevated.copy(alpha = 0.40f),
                ShadowBlack
              ),
              radius = 950f
            )
          )
      )

      Column(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .navigationBarsPadding()
          .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // --- 1. DYNAMIC ISLAND EXPANDABLE BAR AT TOP ---
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

        Spacer(modifier = Modifier.height(8.dp))

        // --- 2. TOP APP BAR & PROFILE ROW ---
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Brand + Beta badge
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(if (viewModel.isGeminiConfigured) ShadowAccentGreen else ShadowAccentCyan)
            )
            Text(
              text = "HYDRAXX",
              color = ShadowGlowWhite,
              fontSize = 16.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 2.sp
            )
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(ShadowAccentAmber.copy(alpha = 0.2f))
                .border(1.dp, ShadowAccentAmber.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = "BETA",
                color = ShadowAccentAmber,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              )
            }
          }

          // Controls & Language Switcher
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            // Language Toggle Pill ("العربية" / "EN")
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(ShadowSurfaceDark)
                .border(1.dp, ShadowSurfaceBorder, RoundedCornerShape(12.dp))
                .clickable { viewModel.toggleLanguage() }
                .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Language, contentDescription = null, tint = ShadowAccentCyan, modifier = Modifier.size(13.dp))
                Text(
                  text = if (appLanguage == AppLanguage.ARABIC) "عربي" else "EN",
                  color = ShadowGlowWhite,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            IconButton(onClick = { viewModel.setActiveSheet(AssistantSheet.EXPRESSIONS) }) {
              Icon(Icons.Default.EmojiEmotions, contentDescription = "Expressions", tint = ShadowGlowWhite, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = { viewModel.setActiveSheet(AssistantSheet.TOOLS) }) {
              Icon(Icons.Default.Widgets, contentDescription = "Tools", tint = ShadowGlowWhite, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = { viewModel.setActiveSheet(AssistantSheet.MEMORIES) }) {
              Icon(Icons.Default.Psychology, contentDescription = "Memories", tint = ShadowGlowWhite, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = { viewModel.setActiveSheet(AssistantSheet.SETTINGS) }) {
              Icon(Icons.Default.Settings, contentDescription = "Settings", tint = ShadowGlowWhite, modifier = Modifier.size(20.dp))
            }
          }
        }

        Spacer(modifier = Modifier.weight(0.12f))

        // --- 3. CENTER STAGE: SENTIENT LIVING FACE VIEW ---
        Box(
          modifier = Modifier.size(280.dp),
          contentAlignment = Alignment.Center
        ) {
          ShadowFaceView(
            expression = currentExpression,
            sleepPhase = sleepPhase,
            isListening = isListening,
            isSpeaking = isSpeaking,
            rmsLevel = rmsLevel,
            size = 270.dp,
            onFaceTapped = {
              viewModel.registerInteraction()
              viewModel.setExpression(
                if (currentExpression == AssistantExpression.HAPPY) AssistantExpression.LOVE
                else AssistantExpression.HAPPY
              )
            },
            onFacePet = {
              viewModel.registerInteraction()
            }
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status Pill with language-aware quote
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(ShadowSurfaceDark)
            .border(1.dp, ShadowSurfaceBorder, RoundedCornerShape(24.dp))
            .clickable {
              if (isAsleep) viewModel.registerInteraction()
              else viewModel.setActiveSheet(AssistantSheet.EXPRESSIONS)
            }
            .padding(horizontal = 16.dp, vertical = 7.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = if (isAsleep) Icons.Default.WbSunny else Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = if (isListening) ShadowAccentCyan else if (isAsleep) ShadowAccentPurple else ShadowGlowWhite,
              modifier = Modifier.size(14.dp)
            )
            Text(
              text = when {
                isListening -> if (appLanguage == AppLanguage.ARABIC) "أسمعك بدقة... تحدث الآن" else "Listening to you... Speak now"
                isThinking -> if (appLanguage == AppLanguage.ARABIC) "هايدرا يفكر في طلبك..." else "HyDraXx is thinking..."
                isSpeaking -> if (appLanguage == AppLanguage.ARABIC) "أتحدث إليك..." else "Speaking to you..."
                isAsleep -> if (appLanguage == AppLanguage.ARABIC) "نائم في الظل... اضغط لإيقاظي zZ" else "Sleeping in shadows... Tap to wake zZ"
                sleepPhase == SleepPhase.YAWNING -> if (appLanguage == AppLanguage.ARABIC) "يتثاءب... يشعر بالنعاس" else "Yawning... getting sleepy"
                else -> if (appLanguage == AppLanguage.ARABIC) currentExpression.arabicQuote else currentExpression.quote
              },
              color = ShadowGlowWhite,
              fontSize = 12.5.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }

        // Action Executed Feedback
        AnimatedVisibility(
          visible = partialVoiceText.isNotBlank() || lastActionStatus != null,
          enter = fadeIn(),
          exit = fadeOut()
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 8.dp)
          ) {
            if (partialVoiceText.isNotBlank()) {
              Text(
                text = "\"$partialVoiceText\"",
                color = ShadowAccentCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
              )
            }

            lastActionStatus?.let { status ->
              Spacer(modifier = Modifier.height(4.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(ShadowSurfaceElevated)
                  .border(1.dp, ShadowAccentGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                  .padding(horizontal = 12.dp, vertical = 5.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(Icons.Default.Info, contentDescription = null, tint = ShadowAccentGreen, modifier = Modifier.size(13.dp))
                  Text(text = status, color = ShadowGlowWhite, fontSize = 11.5.sp)
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- 4. QUICK ACTION CHIPS ROW ---
        LazyRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Dynamic Island summon trigger chip
          item {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(ShadowAccentCyan.copy(alpha = 0.22f))
                .border(1.dp, ShadowAccentCyan, RoundedCornerShape(20.dp))
                .clickable { viewModel.triggerHeyHydra() }
                .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
              Text(
                text = if (appLanguage == AppLanguage.ARABIC) "🏝️ يا هايدرا!" else "🏝️ Hey HyDra!",
                color = ShadowAccentCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          val suggestions = if (appLanguage == AppLanguage.ARABIC) listOf(
            "🪙 رمي قرعة",
            "🎲 رمي نرد",
            "افتح يوتيوب",
            "شغل الكشاف",
            "كم نسبة البطارية؟",
            "وضع الصامت",
            "مؤقت 5 دقائق",
            "تذكر أنني أحب القهوة السوداء",
            "افتح الكاميرا"
          ) else listOf(
            "🪙 Flip Coin",
            "🎲 Roll Dice",
            "Open YouTube",
            "Turn on Flashlight",
            "Battery Level",
            "Silent Mode",
            "Set Timer 5 min",
            "Remember that I like coffee",
            "Open Camera"
          )

          items(suggestions) { suggestion ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(ShadowSurfaceDark)
                .border(1.dp, ShadowSurfaceBorder, RoundedCornerShape(20.dp))
                .clickable { viewModel.sendPrompt(suggestion) }
                .padding(horizontal = 13.dp, vertical = 7.dp)
            ) {
              Text(
                text = suggestion,
                color = ShadowGlowWhite,
                fontSize = 11.5.sp
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 5. BOTTOM VOICE INTERACTION DOCK ---
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(ShadowSurfaceDark)
            .border(1.dp, ShadowSurfaceBorder, RoundedCornerShape(32.dp))
            .padding(horizontal = 14.dp, vertical = 9.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Open Chat Transcript Button
          Box(
            modifier = Modifier
              .size(44.dp)
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
              modifier = Modifier.size(19.dp)
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
              .size(54.dp)
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
              modifier = Modifier.size(26.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))
      }

      // --- SHEETS & DIALOGS ---
      when (activeSheet) {
        AssistantSheet.ONBOARDING -> {
          OnboardingSheet(
            language = appLanguage,
            onLanguageChange = { viewModel.setLanguage(it) },
            onUserSignIn = { viewModel.signInUser(it) },
            onComplete = { viewModel.completeOnboarding() },
            onDismiss = { viewModel.closeSheet() }
          )
        }

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
            onSendMessage = { text -> viewModel.sendPrompt(text) },
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
            language = appLanguage,
            currentUser = currentUser,
            onLanguageChange = { viewModel.setLanguage(it) },
            onOpenOnboarding = { viewModel.openOnboarding() },
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
