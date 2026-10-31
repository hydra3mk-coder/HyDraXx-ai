package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssistantExpression
import com.example.ui.theme.ShadowAccentCyan
import com.example.ui.theme.ShadowBlack
import com.example.ui.theme.ShadowGlowMuted
import com.example.ui.theme.ShadowGlowWhite
import com.example.ui.theme.ShadowSurfaceBorder
import com.example.ui.theme.ShadowSurfaceElevated

@Composable
fun DynamicIslandBar(
  isExpanded: Boolean,
  expression: AssistantExpression,
  isListening: Boolean,
  isSpeaking: Boolean,
  rmsLevel: Float,
  transcript: String,
  onToggleExpand: () -> Unit,
  onMicClick: () -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  val targetWidth by animateDpAsState(
    targetValue = if (isExpanded) 330.dp else 120.dp,
    animationSpec = spring(dampingRatio = 0.72f, stiffness = 380f),
    label = "island_width"
  )

  val targetHeight by animateDpAsState(
    targetValue = if (isExpanded) 96.dp else 36.dp,
    animationSpec = spring(dampingRatio = 0.72f, stiffness = 380f),
    label = "island_height"
  )

  val cornerRadius by animateDpAsState(
    targetValue = if (isExpanded) 28.dp else 18.dp,
    animationSpec = tween(200, easing = FastOutSlowInEasing),
    label = "island_corner"
  )

  Box(
    modifier = modifier
      .width(targetWidth)
      .height(targetHeight)
      .shadow(12.dp, RoundedCornerShape(cornerRadius))
      .clip(RoundedCornerShape(cornerRadius))
      .background(ShadowBlack)
      .border(1.2.dp, ShadowAccentCyan.copy(alpha = if (isListening) 0.8f else 0.35f), RoundedCornerShape(cornerRadius))
      .clickable { onToggleExpand() }
      .padding(horizontal = 12.dp, vertical = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    if (!isExpanded) {
      // --- COLLAPSED MINI ISLAND ---
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        ShadowFaceView(
          expression = expression,
          size = 28.dp
        )

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          if (isListening || isSpeaking) {
            VoiceWaveBar(
              isActive = true,
              rmsLevel = rmsLevel,
              barCount = 4,
              maxHeight = 16.dp,
              minHeight = 4.dp
            )
          } else {
            Text(
              text = "HyDraXx",
              color = ShadowGlowWhite,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
          }
        }
      }
    } else {
      // --- EXPANDED CUTE DYNAMIC ISLAND ---
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Cute face on left
        ShadowFaceView(
          expression = expression,
          isListening = isListening,
          isSpeaking = isSpeaking,
          rmsLevel = rmsLevel,
          size = 72.dp
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Center content: live transcription or companion quote
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (isListening) ShadowAccentCyan else ShadowGlowWhite)
            )
            Text(
              text = if (isListening) "HyDraXx is Listening..." else if (isSpeaking) "HyDraXx is Speaking..." else "HyDraXx Companion",
              color = if (isListening) ShadowAccentCyan else ShadowGlowWhite,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = if (transcript.isNotBlank()) "\"$transcript\"" else expression.arabicQuote,
            color = if (transcript.isNotBlank()) ShadowGlowWhite else ShadowGlowMuted,
            fontSize = 12.sp,
            maxLines = 2,
            lineHeight = 16.sp
          )

          Spacer(modifier = Modifier.height(4.dp))

          VoiceWaveBar(
            isActive = isListening || isSpeaking,
            rmsLevel = rmsLevel,
            barCount = 6,
            maxHeight = 14.dp,
            minHeight = 4.dp
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right buttons (Mic toggle & Collapse)
        Column(
          verticalArrangement = Arrangement.SpaceAround,
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          IconButton(
            onClick = onMicClick,
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(if (isListening) ShadowAccentCyan else ShadowSurfaceElevated)
          ) {
            Icon(
              imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
              contentDescription = "Mic",
              tint = if (isListening) ShadowBlack else ShadowGlowWhite,
              modifier = Modifier.size(16.dp)
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          IconButton(
            onClick = onClose,
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              Icons.Default.Close,
              contentDescription = "Collapse",
              tint = ShadowGlowMuted,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
  }
}
