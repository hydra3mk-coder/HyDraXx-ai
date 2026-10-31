package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ShadowAccentCyan
import com.example.ui.theme.ShadowAccentGreen
import com.example.ui.theme.ShadowAccentPurple
import com.example.ui.theme.ShadowDarkBackground
import com.example.ui.theme.ShadowGlowMuted
import com.example.ui.theme.ShadowGlowWhite
import com.example.ui.theme.ShadowSurfaceBorder
import com.example.ui.theme.ShadowSurfaceDark
import com.example.ui.theme.ShadowSurfaceElevated
import com.example.voice.ElevenLabsTtsEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
  apiKey: String,
  elevenLabsKey: String,
  selectedVoiceId: String,
  isGeminiActive: Boolean,
  isFloatingActive: Boolean,
  hasOverlayPermission: Boolean,
  onSaveApiKey: (String) -> Unit,
  onSaveElevenLabsKey: (String) -> Unit,
  onSelectVoice: (String) -> Unit,
  onToggleFloating: (Boolean) -> Unit,
  onOpenDefaultAssistantSettings: () -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
  var geminiKeyInput by remember { mutableStateOf(apiKey) }
  var elevenKeyInput by remember { mutableStateOf(elevenLabsKey) }
  var showGeminiKey by remember { mutableStateOf(false) }
  var showElevenKey by remember { mutableStateOf(false) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = ShadowDarkBackground,
    tonalElevation = 8.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .imePadding()
        .navigationBarsPadding()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "HyDraXx Settings",
            color = ShadowGlowWhite,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "إعدادات مساعد هايدرا والذكاء والأصوات",
            color = ShadowGlowMuted,
            fontSize = 12.sp
          )
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = ShadowGlowWhite)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 1. Android Default Assistant App Card
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(ShadowSurfaceDark)
          .border(1.2.dp, ShadowAccentPurple.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
          .padding(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(Icons.Default.Assistant, contentDescription = null, tint = ShadowAccentPurple, modifier = Modifier.size(24.dp))
            Column {
              Text(
                text = "Default Phone Assistant",
                color = ShadowGlowWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Set HyDraXx as your system digital assistant (summon with power button or corner swipe).",
                color = ShadowGlowMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          Button(
            onClick = onOpenDefaultAssistantSettings,
            colors = ButtonDefaults.buttonColors(containerColor = ShadowAccentPurple),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text("Set Default", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 2. ElevenLabs Voice Selection Card
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(ShadowSurfaceDark)
          .border(1.dp, ShadowSurfaceBorder, RoundedCornerShape(14.dp))
          .padding(14.dp)
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = ShadowAccentCyan, modifier = Modifier.size(20.dp))
            Text(
              text = "ElevenLabs Natural Voices",
              color = ShadowGlowWhite,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Select from expressive voices for HyDraXx or enter your ElevenLabs API key for ultra-lifelike audio:",
            color = ShadowGlowMuted,
            fontSize = 11.sp
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Voice selection chips
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ElevenLabsTtsEngine.AVAILABLE_VOICES) { voice ->
              val isSelected = voice.elevenLabsVoiceId == selectedVoiceId
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(if (isSelected) ShadowAccentCyan.copy(alpha = 0.25f) else ShadowSurfaceElevated)
                  .border(
                    width = 1.dp,
                    color = if (isSelected) ShadowAccentCyan else ShadowSurfaceBorder,
                    shape = RoundedCornerShape(12.dp)
                  )
                  .clickable { onSelectVoice(voice.elevenLabsVoiceId) }
                  .padding(horizontal = 12.dp, vertical = 8.dp)
              ) {
                Column {
                  Text(
                    text = voice.name,
                    color = if (isSelected) ShadowAccentCyan else ShadowGlowWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                  Text(
                    text = voice.description,
                    color = ShadowGlowMuted,
                    fontSize = 10.sp
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // ElevenLabs API key input
          OutlinedTextField(
            value = elevenKeyInput,
            onValueChange = { elevenKeyInput = it },
            label = { Text("ElevenLabs API Key (Optional)", fontSize = 11.sp) },
            visualTransformation = if (showElevenKey) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
              IconButton(onClick = { showElevenKey = !showElevenKey }) {
                Icon(Icons.Default.Key, contentDescription = null, tint = if (showElevenKey) ShadowAccentCyan else ShadowGlowMuted)
              }
            },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = ShadowGlowWhite,
              unfocusedTextColor = ShadowGlowWhite,
              focusedBorderColor = ShadowAccentCyan,
              unfocusedBorderColor = ShadowSurfaceBorder
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(8.dp))

          Button(
            onClick = { onSaveElevenLabsKey(elevenKeyInput) },
            colors = ButtonDefaults.buttonColors(containerColor = ShadowAccentCyan),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.align(Alignment.End)
          ) {
            Text("Save Voice Key", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 3. AI Brain & Gemini Status Card
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(ShadowSurfaceDark)
          .border(1.dp, ShadowSurfaceBorder, RoundedCornerShape(14.dp))
          .padding(14.dp)
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(CircleShape)
                  .background(if (isGeminiActive) ShadowAccentGreen else ShadowAccentCyan)
              )
              Text(
                text = if (isGeminiActive) "Gemini 2.5 Flash Online" else "Offline Smart Brain Active",
                color = ShadowGlowWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(ShadowSurfaceElevated)
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(
                text = if (isGeminiActive) "CLOUD AI" else "ON-DEVICE",
                color = if (isGeminiActive) ShadowAccentGreen else ShadowAccentCyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          OutlinedTextField(
            value = geminiKeyInput,
            onValueChange = { geminiKeyInput = it },
            label = { Text("Gemini API Key (Optional)", fontSize = 11.sp) },
            visualTransformation = if (showGeminiKey) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
              IconButton(onClick = { showGeminiKey = !showGeminiKey }) {
                Icon(Icons.Default.Key, contentDescription = null, tint = if (showGeminiKey) ShadowAccentCyan else ShadowGlowMuted)
              }
            },
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = ShadowGlowWhite,
              unfocusedTextColor = ShadowGlowWhite,
              focusedBorderColor = ShadowAccentCyan,
              unfocusedBorderColor = ShadowSurfaceBorder
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(8.dp))

          Button(
            onClick = { onSaveApiKey(geminiKeyInput) },
            colors = ButtonDefaults.buttonColors(containerColor = ShadowAccentCyan),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.align(Alignment.End)
          ) {
            Text("Update Gemini Key", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 4. Floating Overlay Toggle
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(ShadowSurfaceDark)
          .border(1.dp, ShadowSurfaceBorder, RoundedCornerShape(14.dp))
          .padding(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(Icons.Default.Layers, contentDescription = null, tint = ShadowAccentCyan, modifier = Modifier.size(24.dp))
            Column {
              Text(
                text = "Floating Screen Presence",
                color = ShadowGlowWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "Keep HyDraXx's glowing face available while you use other apps.",
                color = ShadowGlowMuted,
                fontSize = 11.sp
              )
            }
          }

          Switch(
            checked = isFloatingActive,
            onCheckedChange = { enable ->
              if (enable && !hasOverlayPermission) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                  val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                  ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                  context.startActivity(intent)
                }
              } else {
                onToggleFloating(enable)
              }
            },
            colors = SwitchDefaults.colors(
              checkedThumbColor = ShadowGlowWhite,
              checkedTrackColor = ShadowAccentCyan,
              uncheckedThumbColor = ShadowGlowMuted,
              uncheckedTrackColor = ShadowSurfaceElevated
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 5. Privacy Notice
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(ShadowSurfaceElevated)
          .padding(12.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Icon(Icons.Default.Security, contentDescription = null, tint = ShadowAccentGreen, modifier = Modifier.size(20.dp))
          Text(
            text = "HyDraXx Privacy: Memories, tasks, and system tool commands remain stored in your phone's local database.",
            color = ShadowGlowMuted,
            fontSize = 11.sp,
            lineHeight = 16.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}
