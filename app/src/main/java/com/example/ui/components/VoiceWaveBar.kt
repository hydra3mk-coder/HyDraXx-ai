package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ShadowAccentCyan
import com.example.ui.theme.ShadowGlowWhite

@Composable
fun VoiceWaveBar(
  isActive: Boolean,
  rmsLevel: Float = 0f,
  modifier: Modifier = Modifier,
  barCount: Int = 7,
  activeColor: Color = ShadowAccentCyan,
  idleColor: Color = ShadowGlowWhite.copy(alpha = 0.3f),
  maxHeight: Dp = 32.dp,
  minHeight: Dp = 6.dp
) {
  val transition = rememberInfiniteTransition(label = "wave")

  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    for (i in 0 until barCount) {
      val delayMs = i * 140
      val animScale by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
          animation = tween(450, delayMillis = delayMs, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse
        ),
        label = "bar_$i"
      )

      val dynamicHeight = if (isActive) {
        val multiplier = if (rmsLevel > 0.05f) {
          (0.3f + rmsLevel * 0.7f).coerceIn(0.2f, 1.2f)
        } else {
          animScale
        }
        minHeight + (maxHeight - minHeight) * multiplier
      } else {
        minHeight
      }

      Box(
        modifier = Modifier
          .width(3.5.dp)
          .height(dynamicHeight)
          .background(
            color = if (isActive) activeColor else idleColor,
            shape = RoundedCornerShape(2.dp)
          )
      )
    }
  }
}
