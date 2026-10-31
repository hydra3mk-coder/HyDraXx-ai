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
import com.example.data.model.SleepPhase
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
  sleepPhase: SleepPhase = SleepPhase.AWAKE,
  isListening: Boolean = false,
  isSpeaking: Boolean = false,
  rmsLevel: Float = 0f,
  size: Dp = 260.dp,
  onFaceTapped: (() -> Unit)? = null,
  onFacePet: (() -> Unit)? = null
) {
  val scope = rememberCoroutineScope()

  // Blink state (0f = closed, 1f = fully open)
  val blinkAnim = remember { Animatable(1f) }

  // Sleep eyelid droop interpolation (1f = fully awake, 0.15f = closed slit)
  val sleepEyelidAnim = remember { Animatable(1f) }

  // Yawining squash/stretch
  val yawnStretchAnim = remember { Animatable(1f) }

  // Gaze offset
  var gazeOffsetX by remember { mutableFloatStateOf(0f) }
  var gazeOffsetY by remember { mutableFloatStateOf(0f) }

  // Petting tickle reaction
  var isPetting by remember { mutableStateOf(false) }

  // Continuous breathing
  val infiniteTransition = rememberInfiniteTransition(label = "idle_life")
  val idleBreathing by infiniteTransition.animateFloat(
    initialValue = if (sleepPhase == SleepPhase.DEEP_SLEEP) 0.93f else 0.97f,
    targetValue = if (sleepPhase == SleepPhase.DEEP_SLEEP) 1.05f else 1.03f,
    animationSpec = infiniteRepeatable(
      animation = tween(if (sleepPhase == SleepPhase.DEEP_SLEEP) 3400 else 2200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "breathing"
  )

  // Floating accent animation for hearts, '?', 'z Z'
  val floatOffset by infiniteTransition.animateFloat(
    initialValue = -8f,
    targetValue = 8f,
    animationSpec = infiniteRepeatable(
      animation = tween(1900, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "floating"
  )

  // Rotation animation for dizziness swirl
  val swirlRotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(2800, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "swirl"
  )

  // Speaking pulse animation
  val speakingPulse by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.18f,
    animationSpec = infiniteRepeatable(
      animation = tween(210, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "speaking_pulse"
  )

  // Floating ZZZ particle phase tracker
  val zzzProgress by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(3200, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "zzz_progress"
  )

  // Fluid multi-stage sleep transition sequence
  LaunchedEffect(sleepPhase) {
    when (sleepPhase) {
      SleepPhase.AWAKE -> {
        launch { sleepEyelidAnim.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = 300f)) }
        launch { yawnStretchAnim.animateTo(1f, tween(300)) }
      }
      SleepPhase.YAWNING -> {
        // Step 1: Big sleepy yawn (stretch eyes, squeeze, slow blink)
        yawnStretchAnim.animateTo(1.35f, tween(600, easing = FastOutSlowInEasing))
        blinkAnim.animateTo(0.2f, tween(400))
        delay(300)
        yawnStretchAnim.animateTo(0.95f, tween(500))
        blinkAnim.animateTo(0.7f, tween(400))
      }
      SleepPhase.DROWSY -> {
        // Step 2: Drowsy heavy eyelids sinking slowly
        yawnStretchAnim.animateTo(1f, tween(400))
        sleepEyelidAnim.animateTo(0.35f, tween(1200, easing = FastOutSlowInEasing))
      }
      SleepPhase.DEEP_SLEEP -> {
        // Step 3: Gentle soft closing into peaceful sleep
        sleepEyelidAnim.animateTo(0.16f, tween(1400, easing = FastOutSlowInEasing))
      }
      SleepPhase.WAKING_BURST -> {
        // Step 4: Startled adorable wake-up burst!
        blinkAnim.snapTo(0.1f)
        yawnStretchAnim.snapTo(0.8f)
        launch { blinkAnim.animateTo(1.25f, spring(dampingRatio = 0.5f, stiffness = 500f)) }
        launch { yawnStretchAnim.animateTo(1.1f, spring(dampingRatio = 0.55f, stiffness = 450f)) }
        delay(400)
        launch { blinkAnim.animateTo(1f, tween(300)) }
        launch { yawnStretchAnim.animateTo(1f, tween(300)) }
        launch { sleepEyelidAnim.animateTo(1f, tween(300)) }
      }
    }
  }

  // Automatic natural blinks in awake state
  LaunchedEffect(expression, sleepPhase) {
    while (true) {
      delay((3000..5000).random().toLong())
      if (sleepPhase == SleepPhase.AWAKE &&
        expression != AssistantExpression.SLEEPING &&
        expression != AssistantExpression.TIRED
      ) {
        blinkAnim.animateTo(0.08f, tween(80, easing = FastOutSlowInEasing))
        blinkAnim.animateTo(1f, spring(dampingRatio = 0.65f, stiffness = 400f))
      }
    }
  }

  // Smooth gaze return
  LaunchedEffect(gazeOffsetX, gazeOffsetY) {
    if (gazeOffsetX != 0f || gazeOffsetY != 0f) {
      delay(1400)
      launch { Animatable(gazeOffsetX).animateTo(0f, tween(500)) { gazeOffsetX = value } }
      launch { Animatable(gazeOffsetY).animateTo(0f, tween(500)) { gazeOffsetY = value } }
    }
  }

  Box(
    modifier = modifier
      .size(size)
      .pointerInput(Unit) {
        detectTapGestures(
          onTap = {
            onFaceTapped?.invoke()
            scope.launch {
              blinkAnim.animateTo(0.1f, tween(70))
              blinkAnim.animateTo(1f, tween(100))
            }
          }
        )
      }
      .pointerInput(Unit) {
        detectDragGestures(
          onDragStart = {
            isPetting = true
            onFacePet?.invoke()
          },
          onDragEnd = { isPetting = false },
          onDragCancel = { isPetting = false }
        ) { change, dragAmount ->
          change.consume()
          isPetting = true
          gazeOffsetX = (gazeOffsetX + dragAmount.x * 0.16f).coerceIn(-28f, 28f)
          gazeOffsetY = (gazeOffsetY + dragAmount.y * 0.16f).coerceIn(-22f, 22f)
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
        1f + (rmsLevel * 0.38f)
      } else if (isSpeaking) {
        speakingPulse
      } else {
        idleBreathing
      }

      val effectiveBlink = if (sleepPhase == SleepPhase.DEEP_SLEEP || expression == AssistantExpression.SLEEPING) {
        0.18f
      } else if (sleepPhase == SleepPhase.DROWSY || expression == AssistantExpression.TIRED) {
        0.30f * blinkAnim.value
      } else {
        blinkAnim.value * sleepEyelidAnim.value
      }

      // Base Eye Dimensions
      val eyeWidth = (canvasWidth * 0.165f) * voiceScale
      val eyeHeight = (canvasHeight * 0.30f) * voiceScale * effectiveBlink * yawnStretchAnim.value
      val eyeDistance = canvasWidth * 0.18f

      val leftEyeCenter = Offset(centerX - eyeDistance, centerY)
      val rightEyeCenter = Offset(centerX + eyeDistance, centerY)

      // Ambient radial glow behind eyes
      val isAsleep = sleepPhase == SleepPhase.DEEP_SLEEP || expression == AssistantExpression.SLEEPING
      val glowRadius = eyeWidth * (if (isListening) 2.4f else if (isAsleep) 1.5f else 1.9f)
      val glowColor = when {
        isListening -> ShadowAccentCyan.copy(alpha = 0.32f + (rmsLevel * 0.35f))
        isPetting || expression == AssistantExpression.LOVE -> ShadowAccentRose.copy(alpha = 0.30f)
        isAsleep -> ShadowAccentPurple.copy(alpha = 0.16f)
        else -> ShadowGlowWhite.copy(alpha = 0.20f)
      }

      drawCircle(
        brush = Brush.radialGradient(listOf(glowColor, Color.Transparent), center = leftEyeCenter, radius = glowRadius),
        radius = glowRadius,
        center = leftEyeCenter
      )
      drawCircle(
        brush = Brush.radialGradient(listOf(glowColor, Color.Transparent), center = rightEyeCenter, radius = glowRadius),
        radius = glowRadius,
        center = rightEyeCenter
      )

      // Dynamic Listening Wave Rings
      if (isListening && rmsLevel > 0.04f) {
        val rippleRadius = (canvasWidth * 0.44f) * (0.85f + rmsLevel * 0.4f)
        drawCircle(
          color = ShadowAccentCyan.copy(alpha = 0.40f * (1f - rmsLevel * 0.4f)),
          radius = rippleRadius,
          center = Offset(centerX, centerY),
          style = Stroke(width = 3.5.dp.toPx())
        )
      }

      // Draw expression or pet love state
      val activeExp = if (isPetting) AssistantExpression.LOVE else expression
      when (activeExp) {
        AssistantExpression.NEUTRAL -> {
          if (sleepPhase == SleepPhase.DEEP_SLEEP) {
            drawSleepyEye(leftEyeCenter, eyeWidth, eyeHeight)
            drawSleepyEye(rightEyeCenter, eyeWidth, eyeHeight)
            drawFluidZzzSequence(Offset(rightEyeCenter.x + eyeWidth * 0.85f, rightEyeCenter.y - eyeHeight * 1.3f), zzzProgress)
          } else if (sleepPhase == SleepPhase.DROWSY) {
            drawSleepyEye(leftEyeCenter, eyeWidth, eyeHeight)
            drawSleepyEye(rightEyeCenter, eyeWidth, eyeHeight)
          } else {
            drawOvalEye(leftEyeCenter, eyeWidth, eyeHeight)
            drawOvalEye(rightEyeCenter, eyeWidth, eyeHeight)
          }
        }

        AssistantExpression.HAPPY -> {
          drawHappyArch(leftEyeCenter, eyeWidth, eyeHeight)
          drawHappyArch(rightEyeCenter, eyeWidth, eyeHeight)
          drawSparkleRays(Offset(rightEyeCenter.x + eyeWidth * 0.9f, rightEyeCenter.y - eyeHeight * 1.3f))
        }

        AssistantExpression.SLEEPING -> {
          drawSleepyEye(leftEyeCenter, eyeWidth, eyeHeight)
          drawSleepyEye(rightEyeCenter, eyeWidth, eyeHeight)
          drawFluidZzzSequence(Offset(rightEyeCenter.x + eyeWidth * 0.85f, rightEyeCenter.y - eyeHeight * 1.3f), zzzProgress)
        }

        AssistantExpression.CURIOUS -> {
          drawCuriousEye(leftEyeCenter, eyeWidth, eyeHeight, lookUp = true)
          drawCuriousEye(rightEyeCenter, eyeWidth, eyeHeight, lookUp = true)
          drawQuestionMark(Offset(rightEyeCenter.x + eyeWidth * 1.1f, rightEyeCenter.y - eyeHeight * 1.4f + floatOffset))
        }

        AssistantExpression.SURPRISED -> {
          drawSurprisedRing(leftEyeCenter, eyeWidth * 1.15f)
          drawSurprisedRing(rightEyeCenter, eyeWidth * 1.15f)
          drawSparkleRays(Offset(rightEyeCenter.x + eyeWidth * 1.0f, rightEyeCenter.y - eyeHeight * 1.2f))
        }

        AssistantExpression.ANNOYED -> {
          drawFocusedAngledEye(leftEyeCenter, eyeWidth, eyeHeight, isLeft = true)
          drawFocusedAngledEye(rightEyeCenter, eyeWidth, eyeHeight, isLeft = false)
          drawAngerMark(Offset(rightEyeCenter.x + eyeWidth * 1.2f, rightEyeCenter.y - eyeHeight * 1.1f))
        }

        AssistantExpression.LOVE -> {
          drawHappyArch(leftEyeCenter, eyeWidth, eyeHeight)
          drawHappyArch(rightEyeCenter, eyeWidth, eyeHeight)
          drawBlushTicks(Offset(leftEyeCenter.x, leftEyeCenter.y + eyeHeight * 0.85f))
          drawBlushTicks(Offset(rightEyeCenter.x, rightEyeCenter.y + eyeHeight * 0.85f))
          drawFloatingHeart(Offset(rightEyeCenter.x + eyeWidth * 1.1f, rightEyeCenter.y - eyeHeight * 1.3f + floatOffset))
        }

        AssistantExpression.TIRED -> {
          drawPillSlit(leftEyeCenter, eyeWidth * 1.1f, 14.dp.toPx())
          drawPillSlit(rightEyeCenter, eyeWidth * 1.1f, 14.dp.toPx())
          drawDizzySwirl(Offset(rightEyeCenter.x + eyeWidth * 0.8f, rightEyeCenter.y - eyeHeight * 1.2f), swirlRotation)
        }

        AssistantExpression.PEEKING -> {
          drawPeekingEye(leftEyeCenter, eyeWidth, eyeHeight)
          drawPeekingEye(rightEyeCenter, eyeWidth, eyeHeight)
          drawSparkleRays(Offset(rightEyeCenter.x + eyeWidth * 0.9f, rightEyeCenter.y - eyeHeight * 1.2f))
        }

        AssistantExpression.LISTENING -> {
          val pulseW = eyeWidth * (1f + rmsLevel * 0.4f)
          val pulseH = eyeHeight * (1f + rmsLevel * 0.4f)
          drawOvalEye(leftEyeCenter, pulseW, pulseH, tint = ShadowAccentCyan)
          drawOvalEye(rightEyeCenter, pulseW, pulseH, tint = ShadowAccentCyan)
          drawSparkleRays(Offset(rightEyeCenter.x + eyeWidth * 0.9f, rightEyeCenter.y - eyeHeight * 1.2f))
        }

        AssistantExpression.SPEAKING -> {
          val speakH = eyeHeight * speakingPulse
          drawOvalEye(leftEyeCenter, eyeWidth, speakH)
          drawOvalEye(rightEyeCenter, eyeWidth, speakH)
        }
      }
    }
  }
}

// Geometric Drawing Primitives
private fun DrawScope.drawOvalEye(center: Offset, width: Float, height: Float, tint: Color = ShadowGlowWhite) {
  val rect = Rect(
    left = center.x - width / 2f,
    top = center.y - height / 2f,
    right = center.x + width / 2f,
    bottom = center.y + height / 2f
  )
  drawRoundRect(
    color = tint,
    topLeft = rect.topLeft,
    size = rect.size,
    cornerRadius = CornerRadius(width / 2f, height / 2f)
  )
}

private fun DrawScope.drawHappyArch(center: Offset, width: Float, height: Float) {
  val strokeWidth = width * 0.38f
  val path = Path().apply {
    val r = width * 0.65f
    val arcRect = Rect(center.x - r, center.y - r * 0.5f, center.x + r, center.y + r * 1.1f)
    arcTo(arcRect, 180f, 180f, false)
  }
  drawPath(path = path, color = ShadowGlowWhite, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
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
  val rect = Rect(center.x - width / 2f, center.y - height / 2f, center.x + width / 2f, center.y + height / 2f)
  drawRoundRect(
    color = ShadowGlowWhite,
    topLeft = rect.topLeft,
    size = rect.size,
    cornerRadius = CornerRadius(width / 2f, height / 2f)
  )

  val pupilOffset = Offset(center.x + width * 0.16f, center.y - height * 0.14f)
  drawCircle(color = Color.Black, radius = width * 0.28f, center = pupilOffset)
  drawCircle(color = ShadowGlowWhite, radius = width * 0.10f, center = Offset(pupilOffset.x + width * 0.08f, pupilOffset.y - width * 0.08f))
}

private fun DrawScope.drawSurprisedRing(center: Offset, radius: Float) {
  drawCircle(
    color = ShadowGlowWhite,
    radius = radius * 0.65f,
    center = center,
    style = Stroke(width = radius * 0.28f)
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
  val strokeW = 4.dp.toPx()
  drawLine(
    color = ShadowGlowWhite,
    start = Offset(anchor.x, anchor.y),
    end = Offset(anchor.x + 16.dp.toPx(), anchor.y - 14.dp.toPx()),
    strokeWidth = strokeW,
    cap = StrokeCap.Round
  )
  drawLine(
    color = ShadowGlowWhite,
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
    cubicTo(anchor.x + 5.dp.toPx(), anchor.y - 2.dp.toPx(), anchor.x, anchor.y, anchor.x, anchor.y + 8.dp.toPx())
  }
  drawPath(path = path, color = ShadowGlowWhite, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
  drawCircle(color = ShadowGlowWhite, radius = 2.5.dp.toPx(), center = Offset(anchor.x, anchor.y + 16.dp.toPx()))
}

// Fluid animated multi-particle ZZZ sequence with sinusoidal wave drift
private fun DrawScope.drawFluidZzzSequence(anchor: Offset, progress: Float) {
  for (i in 0..2) {
    val phaseOffset = (progress + (i * 0.33f)) % 1f
    val yRise = phaseOffset * 55.dp.toPx()
    val xSway = (sin(phaseOffset * Math.PI * 2) * 14.dp.toPx()).toFloat()
    val alpha = when {
      phaseOffset < 0.2f -> phaseOffset / 0.2f
      phaseOffset > 0.8f -> (1f - phaseOffset) / 0.2f
      else -> 1f
    }.coerceIn(0f, 1f)

    val scale = (0.7f + phaseOffset * 0.5f) * (14.dp.toPx() + i * 3.dp.toPx())
    val pos = Offset(anchor.x + (i * 12.dp.toPx()) + xSway, anchor.y - yRise)

    val path = Path().apply {
      moveTo(pos.x, pos.y)
      lineTo(pos.x + scale, pos.y)
      lineTo(pos.x, pos.y + scale)
      lineTo(pos.x + scale, pos.y + scale)
    }
    drawPath(
      path = path,
      color = ShadowGlowWhite.copy(alpha = alpha * 0.9f),
      style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
  }
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
    cubicTo(anchor.x - 16.dp.toPx() * s, anchor.y - 4.dp.toPx() * s, anchor.x - 8.dp.toPx() * s, anchor.y - 16.dp.toPx() * s, anchor.x, anchor.y - 6.dp.toPx() * s)
    cubicTo(anchor.x + 8.dp.toPx() * s, anchor.y - 16.dp.toPx() * s, anchor.x + 16.dp.toPx() * s, anchor.y - 4.dp.toPx() * s, anchor.x, anchor.y + 10.dp.toPx() * s)
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
  drawLine(color = ShadowGlowWhite, start = Offset(anchor.x - s, anchor.y - s / 2), end = Offset(anchor.x - s, anchor.y + s / 2), strokeWidth = strokeW, cap = StrokeCap.Round)
  drawLine(color = ShadowGlowWhite, start = Offset(anchor.x + s, anchor.y - s / 2), end = Offset(anchor.x + s, anchor.y + s / 2), strokeWidth = strokeW, cap = StrokeCap.Round)
  drawLine(color = ShadowGlowWhite, start = Offset(anchor.x - s / 2, anchor.y - s), end = Offset(anchor.x + s / 2, anchor.y - s), strokeWidth = strokeW, cap = StrokeCap.Round)
  drawLine(color = ShadowGlowWhite, start = Offset(anchor.x - s / 2, anchor.y + s), end = Offset(anchor.x + s / 2, anchor.y + s), strokeWidth = strokeW, cap = StrokeCap.Round)
}
