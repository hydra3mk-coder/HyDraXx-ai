package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.AssistantExpression
import com.example.ui.theme.ShadowAccentAmber
import com.example.ui.theme.ShadowAccentCyan
import com.example.ui.theme.ShadowAccentPurple
import com.example.ui.theme.ShadowAccentRose
import com.example.ui.theme.ShadowGlowWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ShadowFaceView(
  expression: AssistantExpression,
  modifier: Modifier = Modifier,
  isListening: Boolean = false,
  isSpeaking: Boolean = false,
  rmsLevel: Float = 0f, // 0.0f to 1.0f from mic input
  size: Dp = 260.dp,
  onFaceTapped: (() -> Unit)? = null
) {
  val scope = rememberCoroutineScope()

  // Blink state (0f = closed slit, 1f = fully open)
  val blinkAnim = remember { Animatable(1f) }

  // Gaze offset (moves slightly with touches or gentle random curiosity)
  var gazeOffsetX by remember { mutableFloatStateOf(0f) }
  var gazeOffsetY by remember { mutableFloatStateOf(0f) }

  // Continuous breathing / subtle idle life
  val infiniteTransition = rememberInfiniteTransition(label = "idle_life")
  val idleBreathing by infiniteTransition.animateFloat(
    initialValue = 0.97f,
    targetValue = 1.03f,
    animationSpec = infiniteRepeatable(
      animation = tween(2400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "breathing"
  )

  // Floating accent animation for hearts, '?', 'z Z', dizziness
  val floatOffset by infiniteTransition.animateFloat(
    initialValue = -6f,
    targetValue = 6f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "floating"
  )

  // Rotation animation for dizziness swirl
  val swirlRotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(3000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "swirl"
  )

  // Speaking pulse animation
  val speakingPulse by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.18f,
    animationSpec = infiniteRepeatable(
      animation = tween(220, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "speaking_pulse"
  )

  // Automatic natural blinks in non-sleeping states
  LaunchedEffect(expression) {
    while (true) {
      delay((2800..4500).random().toLong())
      if (expression != AssistantExpression.SLEEPING && expression != AssistantExpression.TIRED) {
        // Quick blink
        blinkAnim.animateTo(0.08f, tween(90, easing = FastOutSlowInEasing))
        blinkAnim.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = 400f))
      }
    }
  }

  // Smooth gaze return
  LaunchedEffect(gazeOffsetX, gazeOffsetY) {
    if (gazeOffsetX != 0f || gazeOffsetY != 0f) {
      delay(1500)
      launch {
        Animatable(gazeOffsetX).animateTo(0f, tween(600)) { gazeOffsetX = value }
      }
      launch {
        Animatable(gazeOffsetY).animateTo(0f, tween(600)) { gazeOffsetY = value }
      }
    }
  }

  // Petting tickle reaction
  var isPetting by remember { mutableStateOf(false) }

  Box(
    modifier = modifier
      .size(size)
      .pointerInput(Unit) {
        detectTapGestures(
          onTap = { offset ->
            onFaceTapped?.invoke()
            scope.launch {
              // Delightful quick double wink on tap
              blinkAnim.animateTo(0.1f, tween(80))
              blinkAnim.animateTo(1f, tween(100))
            }
          }
        )
      }
      .pointerInput(Unit) {
        detectDragGestures(
          onDragStart = { isPetting = true },
          onDragEnd = { isPetting = false },
          onDragCancel = { isPetting = false }
        ) { change, dragAmount ->
          change.consume()
          isPetting = true
          gazeOffsetX = (gazeOffsetX + dragAmount.x * 0.15f).coerceIn(-25f, 25f)
          gazeOffsetY = (gazeOffsetY + dragAmount.y * 0.15f).coerceIn(-20f, 20f)
        }
      },
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val canvasWidth = this.size.width
      val canvasHeight = this.size.height
      val centerX = canvasWidth / 2f + gazeOffsetX
      val centerY = canvasHeight / 2f + gazeOffsetY

      // Compute dynamic voice expansion
      val voiceScale = if (isListening) {
        1f + (rmsLevel * 0.35f)
      } else if (isSpeaking) {
        speakingPulse
      } else {
        idleBreathing
      }

      val effectiveBlink = if (expression == AssistantExpression.SLEEPING || expression == AssistantExpression.TIRED) {
        0.18f
      } else {
        blinkAnim.value
      }

      // Eye dimensions relative to canvas
      val eyeWidth = (canvasWidth * 0.165f) * voiceScale
      val eyeHeight = (canvasHeight * 0.30f) * voiceScale * effectiveBlink
      val eyeDistance = canvasWidth * 0.18f

      val leftEyeCenter = Offset(centerX - eyeDistance, centerY)
      val rightEyeCenter = Offset(centerX + eyeDistance, centerY)

      // Draw subtle ambient glow behind eyes
      val glowRadius = eyeWidth * (if (isListening) 2.2f else 1.8f)
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            if (isListening) ShadowAccentCyan.copy(alpha = 0.28f + (rmsLevel * 0.3f))
            else if (expression == AssistantExpression.LOVE) ShadowAccentRose.copy(alpha = 0.25f)
            else ShadowGlowWhite.copy(alpha = 0.18f),
            Color.Transparent
          ),
          center = leftEyeCenter,
          radius = glowRadius
        ),
        radius = glowRadius,
        center = leftEyeCenter
      )
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            if (isListening) ShadowAccentCyan.copy(alpha = 0.28f + (rmsLevel * 0.3f))
            else if (expression == AssistantExpression.LOVE) ShadowAccentRose.copy(alpha = 0.25f)
            else ShadowGlowWhite.copy(alpha = 0.18f),
            Color.Transparent
          ),
          center = rightEyeCenter,
          radius = glowRadius
        ),
        radius = glowRadius,
        center = rightEyeCenter
      )

      // Draw listening sound ripple ring if active
      if (isListening && rmsLevel > 0.05f) {
        val rippleRadius = (canvasWidth * 0.42f) * (0.85f + rmsLevel * 0.35f)
        drawCircle(
          color = ShadowAccentCyan.copy(alpha = 0.35f * (1f - rmsLevel * 0.5f)),
          radius = rippleRadius,
          center = Offset(centerX, centerY),
          style = Stroke(width = 3.dp.toPx())
        )
      }

      // Draw the eyes based on expression (or cute petting love reaction)
      val activeExp = if (isPetting) AssistantExpression.LOVE else expression
      when (activeExp) {
        AssistantExpression.NEUTRAL -> {
          // Two glowing smooth vertical ovals (Row 1, Col 1 in original grid)
          drawOvalEye(leftEyeCenter, eyeWidth, eyeHeight)
          drawOvalEye(rightEyeCenter, eyeWidth, eyeHeight)
        }

        AssistantExpression.HAPPY -> {
          // Curved inverted U-shaped happy arches (Row 1, Col 2 in original grid)
          drawHappyArch(leftEyeCenter, eyeWidth, eyeHeight)
          drawHappyArch(rightEyeCenter, eyeWidth, eyeHeight)
          // Sparkle accent rays above right eye
          drawSparkleRays(Offset(rightEyeCenter.x + eyeWidth * 0.9f, rightEyeCenter.y - eyeHeight * 1.3f))
        }

        AssistantExpression.SLEEPING -> {
          // Droopy gentle horizontal crescents (Row 1, Col 3 in original grid)
          drawSleepyEye(leftEyeCenter, eyeWidth, eyeHeight)
          drawSleepyEye(rightEyeCenter, eyeWidth, eyeHeight)
          // Floating 'z Z'
          drawZzz(Offset(rightEyeCenter.x + eyeWidth * 0.8f, rightEyeCenter.y - eyeHeight * 1.2f + floatOffset))
        }

        AssistantExpression.CURIOUS -> {
          // Inquisitive eyes with pupils looking up-right (Row 2, Col 1 in original grid)
          drawCuriousEye(leftEyeCenter, eyeWidth, eyeHeight, lookUp = true)
          drawCuriousEye(rightEyeCenter, eyeWidth, eyeHeight, lookUp = true)
          // Floating '?' above right eye
          drawQuestionMark(Offset(rightEyeCenter.x + eyeWidth * 1.1f, rightEyeCenter.y - eyeHeight * 1.4f + floatOffset))
        }

        AssistantExpression.SURPRISED -> {
          // Wide open glowing circular rings with sparkle (Row 2, Col 2 in original grid)
          drawSurprisedRing(leftEyeCenter, eyeWidth * 1.15f)
          drawSurprisedRing(rightEyeCenter, eyeWidth * 1.15f)
          drawSparkleRays(Offset(rightEyeCenter.x + eyeWidth * 1.0f, rightEyeCenter.y - eyeHeight * 1.2f))
        }

        AssistantExpression.ANNOYED -> {
          // Slanted sharp focused angular eyes (Row 2, Col 3 in original grid)
          drawFocusedAngledEye(leftEyeCenter, eyeWidth, eyeHeight, isLeft = true)
          drawFocusedAngledEye(rightEyeCenter, eyeWidth, eyeHeight, isLeft = false)
          // Comic vein mark in corner
          drawAngerMark(Offset(rightEyeCenter.x + eyeWidth * 1.2f, rightEyeCenter.y - eyeHeight * 1.1f))
        }

        AssistantExpression.LOVE -> {
          // Cute happy crescents with blush marks & floating heart (Row 3, Col 1 in original grid)
          drawHappyArch(leftEyeCenter, eyeWidth, eyeHeight)
          drawHappyArch(rightEyeCenter, eyeWidth, eyeHeight)
          // Blush ticks '///'
          drawBlushTicks(Offset(leftEyeCenter.x, leftEyeCenter.y + eyeHeight * 0.85f))
          drawBlushTicks(Offset(rightEyeCenter.x, rightEyeCenter.y + eyeHeight * 0.85f))
          // Floating glowing heart
          drawFloatingHeart(Offset(rightEyeCenter.x + eyeWidth * 1.1f, rightEyeCenter.y - eyeHeight * 1.3f + floatOffset))
        }

        AssistantExpression.TIRED -> {
          // Horizontal rounded pill slits with swirl (Row 3, Col 2 in original grid)
          drawPillSlit(leftEyeCenter, eyeWidth * 1.1f, 14.dp.toPx())
          drawPillSlit(rightEyeCenter, eyeWidth * 1.1f, 14.dp.toPx())
          // Dizzy swirl
          drawDizzySwirl(Offset(rightEyeCenter.x + eyeWidth * 0.8f, rightEyeCenter.y - eyeHeight * 1.2f), swirlRotation)
        }

        AssistantExpression.PEEKING -> {
          // Half circle peeking from bottom (Row 3, Col 3 in original grid)
          drawPeekingEye(leftEyeCenter, eyeWidth, eyeHeight)
          drawPeekingEye(rightEyeCenter, eyeWidth, eyeHeight)
          drawSparkleRays(Offset(rightEyeCenter.x + eyeWidth * 0.9f, rightEyeCenter.y - eyeHeight * 1.2f))
        }

        AssistantExpression.LISTENING -> {
          // Dynamic pulsing attentive eyes reacting to voice
          val pulseW = eyeWidth * (1f + rmsLevel * 0.4f)
          val pulseH = eyeHeight * (1f + rmsLevel * 0.4f)
          drawOvalEye(leftEyeCenter, pulseW, pulseH, tint = ShadowAccentCyan)
          drawOvalEye(rightEyeCenter, pulseW, pulseH, tint = ShadowAccentCyan)
          drawSparkleRays(Offset(rightEyeCenter.x + eyeWidth * 0.9f, rightEyeCenter.y - eyeHeight * 1.2f))
        }

        AssistantExpression.SPEAKING -> {
          // Subtle cadence pulse
          val speakH = eyeHeight * speakingPulse
          drawOvalEye(leftEyeCenter, eyeWidth, speakH)
          drawOvalEye(rightEyeCenter, eyeWidth, speakH)
        }
      }
    }
  }
}

// Draw helpers for crisp geometric rendering
private fun DrawScope.drawOvalEye(center: Offset, width: Float, height: Float, tint: Color = ShadowGlowWhite) {
  val rect = Rect(
    left = center.x - width / 2f,
    top = center.y - height / 2f,
    right = center.x + width / 2f,
    bottom = center.y + height / 2f
  )
  val corner = CornerRadius(width / 2f, height / 2f)
  drawRoundRect(
    color = tint,
    topLeft = rect.topLeft,
    size = rect.size,
    cornerRadius = corner
  )
}

private fun DrawScope.drawHappyArch(center: Offset, width: Float, height: Float) {
  val strokeWidth = width * 0.38f
  val path = Path().apply {
    val r = width * 0.65f
    val arcRect = Rect(center.x - r, center.y - r * 0.5f, center.x + r, center.y + r * 1.1f)
    arcTo(arcRect, 180f, 180f, false)
  }
  drawPath(
    path = path,
    color = ShadowGlowWhite,
    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
  )
}

private fun DrawScope.drawSleepyEye(center: Offset, width: Float, height: Float) {
  val path = Path().apply {
    moveTo(center.x - width * 0.6f, center.y + height * 0.1f)
    quadraticTo(
      center.x, center.y + height * 0.45f,
      center.x + width * 0.6f, center.y + height * 0.1f
    )
    lineTo(center.x + width * 0.55f, center.y - height * 0.05f)
    quadraticTo(
      center.x, center.y + height * 0.1f,
      center.x - width * 0.55f, center.y - height * 0.05f
    )
    close()
  }
  drawPath(path = path, color = ShadowGlowWhite, style = Fill)
}

private fun DrawScope.drawCuriousEye(center: Offset, width: Float, height: Float, lookUp: Boolean) {
  // Base oval
  val rect = Rect(center.x - width / 2f, center.y - height / 2f, center.x + width / 2f, center.y + height / 2f)
  drawRoundRect(
    color = ShadowGlowWhite,
    topLeft = rect.topLeft,
    size = rect.size,
    cornerRadius = CornerRadius(width / 2f, height / 2f)
  )

  // Pupil cut/glint looking top-right
  val pupilOffset = Offset(center.x + width * 0.16f, center.y - height * 0.14f)
  drawCircle(
    color = Color.Black,
    radius = width * 0.28f,
    center = pupilOffset
  )
  // Sparkle highlight inside pupil
  drawCircle(
    color = ShadowGlowWhite,
    radius = width * 0.10f,
    center = Offset(pupilOffset.x + width * 0.08f, pupilOffset.y - width * 0.08f)
  )
}

private fun DrawScope.drawSurprisedRing(center: Offset, radius: Float) {
  val strokeW = radius * 0.28f
  drawCircle(
    color = ShadowGlowWhite,
    radius = radius * 0.65f,
    center = center,
    style = Stroke(width = strokeW)
  )
}

private fun DrawScope.drawFocusedAngledEye(center: Offset, width: Float, height: Float, isLeft: Boolean) {
  val path = Path().apply {
    val dir = if (isLeft) 1f else -1f
    moveTo(center.x - width * 0.6f, center.y - height * 0.1f * dir)
    lineTo(center.x + width * 0.6f, center.y + height * 0.25f * dir)
    lineTo(center.x + width * 0.4f, center.y + height * 0.65f)
    lineTo(center.x - width * 0.5f, center.y + height * 0.55f)
    close()
  }
  drawPath(path = path, color = ShadowGlowWhite, style = Fill)
}

private fun DrawScope.drawPillSlit(center: Offset, width: Float, height: Float) {
  drawRoundRect(
    color = ShadowGlowWhite,
    topLeft = Offset(center.x - width / 2f, center.y - height / 2f),
    size = Size(width, height),
    cornerRadius = CornerRadius(height / 2f, height / 2f)
  )
}

private fun DrawScope.drawPeekingEye(center: Offset, width: Float, height: Float) {
  val path = Path().apply {
    val r = width * 0.65f
    val arcRect = Rect(center.x - r, center.y - r * 0.8f, center.x + r, center.y + r * 1.2f)
    arcTo(arcRect, 180f, 180f, false)
    close()
  }
  drawPath(path = path, color = ShadowGlowWhite, style = Fill)
}

private fun DrawScope.drawSparkleRays(anchor: Offset) {
  val rayColor = ShadowGlowWhite
  val strokeW = 4.dp.toPx()
  // Ray 1
  drawLine(
    color = rayColor,
    start = Offset(anchor.x, anchor.y),
    end = Offset(anchor.x + 16.dp.toPx(), anchor.y - 14.dp.toPx()),
    strokeWidth = strokeW,
    cap = StrokeCap.Round
  )
  // Ray 2
  drawLine(
    color = rayColor,
    start = Offset(anchor.x + 12.dp.toPx(), anchor.y + 6.dp.toPx()),
    end = Offset(anchor.x + 28.dp.toPx(), anchor.y - 4.dp.toPx()),
    strokeWidth = strokeW,
    cap = StrokeCap.Round
  )
}

private fun DrawScope.drawQuestionMark(anchor: Offset) {
  val path = Path().apply {
    moveTo(anchor.x - 10.dp.toPx(), anchor.y - 12.dp.toPx())
    cubicTo(
      anchor.x - 10.dp.toPx(), anchor.y - 24.dp.toPx(),
      anchor.x + 12.dp.toPx(), anchor.y - 24.dp.toPx(),
      anchor.x + 8.dp.toPx(), anchor.y - 10.dp.toPx()
    )
    cubicTo(
      anchor.x + 5.dp.toPx(), anchor.y - 2.dp.toPx(),
      anchor.x, anchor.y,
      anchor.x, anchor.y + 8.dp.toPx()
    )
  }
  drawPath(path = path, color = ShadowGlowWhite, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
  drawCircle(color = ShadowGlowWhite, radius = 2.5.dp.toPx(), center = Offset(anchor.x, anchor.y + 16.dp.toPx()))
}

private fun DrawScope.drawZzz(anchor: Offset) {
  // First 'z'
  drawTextZ(anchor, 14.dp.toPx())
  // Second 'Z' (larger)
  drawTextZ(Offset(anchor.x + 14.dp.toPx(), anchor.y - 14.dp.toPx()), 20.dp.toPx())
}

private fun DrawScope.drawTextZ(anchor: Offset, size: Float) {
  val path = Path().apply {
    moveTo(anchor.x, anchor.y)
    lineTo(anchor.x + size, anchor.y)
    lineTo(anchor.x, anchor.y + size)
    lineTo(anchor.x + size, anchor.y + size)
  }
  drawPath(path = path, color = ShadowGlowWhite, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawBlushTicks(anchor: Offset) {
  val strokeW = 2.5.dp.toPx()
  val len = 8.dp.toPx()
  for (i in -1..1) {
    val x = anchor.x + (i * 7.dp.toPx())
    drawLine(
      color = ShadowGlowWhite.copy(alpha = 0.85f),
      start = Offset(x - 2.dp.toPx(), anchor.y - len / 2f),
      end = Offset(x + 2.dp.toPx(), anchor.y + len / 2f),
      strokeWidth = strokeW,
      cap = StrokeCap.Round
    )
  }
}

private fun DrawScope.drawFloatingHeart(anchor: Offset) {
  val path = Path().apply {
    val s = 1.1f
    moveTo(anchor.x, anchor.y + 10.dp.toPx() * s)
    cubicTo(
      anchor.x - 16.dp.toPx() * s, anchor.y - 4.dp.toPx() * s,
      anchor.x - 8.dp.toPx() * s, anchor.y - 16.dp.toPx() * s,
      anchor.x, anchor.y - 6.dp.toPx() * s
    )
    cubicTo(
      anchor.x + 8.dp.toPx() * s, anchor.y - 16.dp.toPx() * s,
      anchor.x + 16.dp.toPx() * s, anchor.y - 4.dp.toPx() * s,
      anchor.x, anchor.y + 10.dp.toPx() * s
    )
    close()
  }
  drawPath(path = path, color = ShadowGlowWhite, style = Fill)
}

private fun DrawScope.drawDizzySwirl(anchor: Offset, rotationDeg: Float) {
  val path = Path()
  var first = true
  val steps = 30
  for (i in 0..steps) {
    val t = i.toFloat() / steps
    val angle = (t * 4 * Math.PI) + Math.toRadians(rotationDeg.toDouble())
    val r = t * 14.dp.toPx()
    val x = anchor.x + (r * cos(angle)).toFloat()
    val y = anchor.y + (r * sin(angle)).toFloat()
    if (first) {
      path.moveTo(x, y)
      first = false
    } else {
      path.lineTo(x, y)
    }
  }
  drawPath(path = path, color = ShadowGlowWhite, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
}

private fun DrawScope.drawAngerMark(anchor: Offset) {
  val strokeW = 3.dp.toPx()
  val s = 10.dp.toPx()
  // 4 corner curved arcs
  drawLine(color = ShadowGlowWhite, start = Offset(anchor.x - s, anchor.y - s / 2), end = Offset(anchor.x - s, anchor.y + s / 2), strokeWidth = strokeW, cap = StrokeCap.Round)
  drawLine(color = ShadowGlowWhite, start = Offset(anchor.x + s, anchor.y - s / 2), end = Offset(anchor.x + s, anchor.y + s / 2), strokeWidth = strokeW, cap = StrokeCap.Round)
  drawLine(color = ShadowGlowWhite, start = Offset(anchor.x - s / 2, anchor.y - s), end = Offset(anchor.x + s / 2, anchor.y - s), strokeWidth = strokeW, cap = StrokeCap.Round)
  drawLine(color = ShadowGlowWhite, start = Offset(anchor.x - s / 2, anchor.y + s), end = Offset(anchor.x + s / 2, anchor.y + s), strokeWidth = strokeW, cap = StrokeCap.Round)
}
