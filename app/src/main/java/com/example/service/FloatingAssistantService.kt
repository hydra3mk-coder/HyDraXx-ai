package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.data.model.AssistantExpression
import com.example.ui.components.ShadowFaceView
import com.example.ui.theme.ShadowAccentCyan
import com.example.ui.theme.ShadowBlack
import com.example.ui.theme.ShadowSurfaceBorder
import kotlin.math.abs

class FloatingAssistantService : Service() {

  private var windowManager: WindowManager? = null
  private var floatingView: View? = null
  private var isViewAttached = false

  companion object {
    const val CHANNEL_ID = "shadow_companion_service"
    const val NOTIFICATION_ID = 1001
    const val ACTION_START = "ACTION_START_FLOATING"
    const val ACTION_STOP = "ACTION_STOP_FLOATING"

    fun startService(context: Context) {
      val intent = Intent(context, FloatingAssistantService::class.java).apply {
        action = ACTION_START
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
      } else {
        context.startService(intent)
      }
    }

    fun stopService(context: Context) {
      val intent = Intent(context, FloatingAssistantService::class.java).apply {
        action = ACTION_STOP
      }
      context.stopService(intent)
    }
  }

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
    startForeground(NOTIFICATION_ID, buildNotification())
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (intent?.action == ACTION_STOP) {
      removeFloatingView()
      stopSelf()
      return START_NOT_STICKY
    }

    if (!isViewAttached) {
      setupFloatingOverlay()
    }
    return START_STICKY
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "Shadow Companion Presence",
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = "Shadow Assistant overlay living on your screen"
        setShowBadge(false)
      }
      val manager = getSystemService(NotificationManager::class.java)
      manager?.createNotificationChannel(channel)
    }
  }

  private fun buildNotification(): Notification {
    val pendingIntent = PendingIntent.getActivity(
      this, 0,
      Intent(this, MainActivity::class.java),
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("HyDraXx is active")
      .setContentText("Tap to summon your companion")
      .setSmallIcon(android.R.drawable.ic_dialog_info)
      .setContentIntent(pendingIntent)
      .setOngoing(true)
      .build()
  }

  private fun setupFloatingOverlay() {
    if (floatingView != null) return

    windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
    if (windowManager == null) return

    val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
    } else {
      @Suppress("DEPRECATION")
      WindowManager.LayoutParams.TYPE_PHONE
    }

    val params = WindowManager.LayoutParams(
      WindowManager.LayoutParams.WRAP_CONTENT,
      WindowManager.LayoutParams.WRAP_CONTENT,
      layoutType,
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
      PixelFormat.TRANSLUCENT
    ).apply {
      gravity = Gravity.TOP or Gravity.START
      x = 40
      y = 300
    }

    val composeView = ComposeView(this).apply {
      setContent {
        FloatingBubble {
          val launchIntent = Intent(this@FloatingAssistantService, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
          }
          startActivity(launchIntent)
        }
      }
    }

    var initialX = 0
    var initialY = 0
    var touchStartX = 0f
    var touchStartY = 0f
    var isClick = false

    composeView.setOnTouchListener { _, event ->
      when (event.action) {
        MotionEvent.ACTION_DOWN -> {
          initialX = params.x
          initialY = params.y
          touchStartX = event.rawX
          touchStartY = event.rawY
          isClick = true
          true
        }
        MotionEvent.ACTION_MOVE -> {
          val dx = (event.rawX - touchStartX).toInt()
          val dy = (event.rawY - touchStartY).toInt()
          if (abs(dx) > 10 || abs(dy) > 10) {
            isClick = false
          }
          params.x = initialX + dx
          params.y = initialY + dy
          if (isViewAttached) {
            windowManager?.updateViewLayout(composeView, params)
          }
          true
        }
        MotionEvent.ACTION_UP -> {
          if (isClick) {
            val launchIntent = Intent(this@FloatingAssistantService, MainActivity::class.java).apply {
              addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(launchIntent)
          }
          true
        }
        else -> false
      }
    }

    try {
      windowManager?.addView(composeView, params)
      floatingView = composeView
      isViewAttached = true
    } catch (_: Exception) {
      // Permission might be revoked
    }
  }

  private fun removeFloatingView() {
    if (isViewAttached && floatingView != null) {
      try {
        windowManager?.removeView(floatingView)
      } catch (_: Exception) {}
      floatingView = null
      isViewAttached = false
    }
  }

  override fun onDestroy() {
    removeFloatingView()
    super.onDestroy()
  }
}

@Composable
private fun FloatingBubble(onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .size(76.dp)
      .clip(CircleShape)
      .background(ShadowBlack)
      .border(1.5.dp, ShadowAccentCyan.copy(alpha = 0.6f), CircleShape)
      .clickable { onClick() }
      .padding(6.dp),
    contentAlignment = Alignment.Center
  ) {
    ShadowFaceView(
      expression = AssistantExpression.NEUTRAL,
      size = 62.dp
    )
  }
}
