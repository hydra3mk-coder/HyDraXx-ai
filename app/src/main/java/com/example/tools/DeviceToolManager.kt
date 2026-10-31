package com.example.tools

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.provider.AlarmClock
import android.provider.Settings
import com.example.data.model.DeviceStatus
import com.example.data.model.InstalledAppInfo

class DeviceToolManager(private val context: Context) {

  private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
  private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
  private var isTorchEnabled = false

  fun getInstalledApps(): List<InstalledAppInfo> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN, null).apply {
      addCategory(Intent.CATEGORY_LAUNCHER)
    }
    val resolveInfos = pm.queryIntentActivities(intent, 0)
    return resolveInfos.mapNotNull { resolveInfo ->
      val label = resolveInfo.loadLabel(pm).toString()
      val packageName = resolveInfo.activityInfo.packageName
      if (packageName != context.packageName) {
        InstalledAppInfo(label = label, packageName = packageName)
      } else null
    }.sortedBy { it.label }
  }

  fun openApp(query: String): Pair<Boolean, String> {
    val pm = context.packageManager
    val cleanQuery = query.trim().lowercase()
    val apps = getInstalledApps()

    // Map common Arabic and English names
    val matched = apps.firstOrNull { app ->
      val l = app.label.lowercase()
      val p = app.packageName.lowercase()
      l.contains(cleanQuery) || p.contains(cleanQuery) || matchesKnownAlias(cleanQuery, l, p)
    }

    if (matched != null) {
      val launchIntent = pm.getLaunchIntentForPackage(matched.packageName)
      if (launchIntent != null) {
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
        return true to "Opened ${matched.label}"
      }
    }

    // Direct system intents fallback
    if (cleanQuery.contains("camera") || cleanQuery.contains("كاميرا")) {
      val camIntent = Intent("android.media.action.IMAGE_CAPTURE").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      return try {
        context.startActivity(camIntent)
        true to "Opened Camera"
      } catch (e: Exception) {
        false to "Camera app not found"
      }
    }

    return false to "App matching '$query' not found"
  }

  private fun matchesKnownAlias(query: String, label: String, packageName: String): Boolean {
    val aliases = mapOf(
      "واتساب" to listOf("whatsapp"),
      "يوتيوب" to listOf("youtube"),
      "انستقرام" to listOf("instagram"),
      "تيك توك" to listOf("tiktok"),
      "كروم" to listOf("chrome", "browser"),
      "خرائط" to listOf("maps"),
      "اعدادات" to listOf("settings"),
      "إعدادات" to listOf("settings"),
      "معرض" to listOf("gallery", "photos"),
      "صور" to listOf("gallery", "photos"),
      "ساعة" to listOf("clock"),
      "منبه" to listOf("clock", "alarm"),
      "حاسبة" to listOf("calculator"),
      "هاتف" to listOf("dialer", "phone"),
      "اتصال" to listOf("dialer", "phone"),
      "رسائل" to listOf("messaging", "messages", "sms"),
      "موسيقى" to listOf("music", "spotify"),
      "سبوتيفاي" to listOf("spotify")
    )

    for ((key, targets) in aliases) {
      if (query.contains(key)) {
        if (targets.any { label.contains(it) || packageName.contains(it) }) {
          return true
        }
      }
    }
    return false
  }

  fun toggleTorch(enable: Boolean? = null): Pair<Boolean, String> {
    if (cameraManager == null) return false to "Flashlight not supported on this device"
    return try {
      val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
        val chars = cameraManager.getCameraCharacteristics(id)
        chars.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
      } ?: cameraManager.cameraIdList.firstOrNull()

      if (cameraId == null) {
        return false to "No camera with flash available"
      }

      val targetState = enable ?: !isTorchEnabled
      cameraManager.setTorchMode(cameraId, targetState)
      isTorchEnabled = targetState
      true to if (targetState) "Flashlight turned ON" else "Flashlight turned OFF"
    } catch (e: Exception) {
      false to "Flashlight error: ${e.localizedMessage}"
    }
  }

  fun getDeviceStatus(): DeviceStatus {
    // Battery
    val batteryStatusIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    val batteryPct = if (level >= 0 && scale > 0) ((level / scale.toFloat()) * 100).toInt() else 50
    val status = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

    // RAM
    val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    actManager?.getMemoryInfo(memInfo)
    val freeRam = memInfo.availMem / (1024 * 1024)
    val totalRam = memInfo.totalMem / (1024 * 1024)

    // Storage
    val statFs = StatFs(Environment.getDataDirectory().path)
    val freeStorage = (statFs.availableBlocksLong * statFs.blockSizeLong) / (1024 * 1024 * 1024)
    val totalStorage = (statFs.blockCountLong * statFs.blockSizeLong) / (1024 * 1024 * 1024)

    // Network
    val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    val network = connManager?.activeNetwork
    val caps = connManager?.getNetworkCapabilities(network)
    val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

    return DeviceStatus(
      batteryLevel = batteryPct,
      isCharging = isCharging,
      freeRamMb = freeRam,
      totalRamMb = totalRam,
      freeStorageGb = freeStorage,
      totalStorageGb = totalStorage,
      isWifiConnected = isWifi,
      isTorchOn = isTorchEnabled
    )
  }

  fun setRingerMode(mode: String): Pair<Boolean, String> {
    if (audioManager == null) return false to "Audio service unavailable"
    return try {
      val targetMode = when (mode.trim().lowercase()) {
        "silent", "صامت" -> AudioManager.RINGER_MODE_SILENT
        "vibrate", "اهتزاز" -> AudioManager.RINGER_MODE_VIBRATE
        else -> AudioManager.RINGER_MODE_NORMAL
      }
      audioManager.ringerMode = targetMode
      val label = when (targetMode) {
        AudioManager.RINGER_MODE_SILENT -> "Silent"
        AudioManager.RINGER_MODE_VIBRATE -> "Vibrate"
        else -> "Normal"
      }
      true to "Ringer mode set to $label"
    } catch (e: Exception) {
      false to "Could not change ringer mode: ${e.localizedMessage}"
    }
  }

  fun openSystemSettings(type: String): Boolean {
    val action = when (type.lowercase()) {
      "wifi", "واي فاي" -> Settings.ACTION_WIFI_SETTINGS
      "bluetooth", "بلوتوث" -> Settings.ACTION_BLUETOOTH_SETTINGS
      "display", "شاشة", "السطوع" -> Settings.ACTION_DISPLAY_SETTINGS
      "sound", "صوت" -> Settings.ACTION_SOUND_SETTINGS
      "battery", "بطارية" -> Settings.ACTION_BATTERY_SAVER_SETTINGS
      "apps", "تطبيقات" -> Settings.ACTION_APPLICATION_SETTINGS
      else -> Settings.ACTION_SETTINGS
    }
    return try {
      val intent = Intent(action).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
      context.startActivity(intent)
      true
    } catch (e: Exception) {
      false
    }
  }

  fun setTimer(seconds: Int, label: String = "Shadow Timer"): Pair<Boolean, String> {
    return try {
      val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
        putExtra(AlarmClock.EXTRA_LENGTH, seconds)
        putExtra(AlarmClock.EXTRA_MESSAGE, label)
        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      true to "Timer set for $seconds seconds ($label)"
    } catch (e: Exception) {
      false to "Timer error: ${e.localizedMessage}"
    }
  }

  fun setAlarm(hour: Int, minute: Int, label: String = "Shadow Alarm"): Pair<Boolean, String> {
    return try {
      val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
        putExtra(AlarmClock.EXTRA_HOUR, hour)
        putExtra(AlarmClock.EXTRA_MINUTES, minute)
        putExtra(AlarmClock.EXTRA_MESSAGE, label)
        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
      true to "Alarm set for %02d:%02d".format(hour, minute)
    } catch (e: Exception) {
      false to "Alarm error: ${e.localizedMessage}"
    }
  }

  fun searchWeb(query: String) {
    try {
      val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
        putExtra("query", query)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (e: Exception) {
      val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(browserIntent)
    }
  }

  fun openMaps(destination: String) {
    try {
      val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(destination)}")
      val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
        setPackage("com.google.android.apps.maps")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      if (mapIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapIntent)
      } else {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(destination)}")).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(browserIntent)
      }
    } catch (e: Exception) {
      // Fallback
    }
  }

  fun openDialer(phoneNumber: String = "") {
    try {
      val uri = if (phoneNumber.isNotBlank()) Uri.parse("tel:${phoneNumber.trim()}") else Uri.parse("tel:")
      val intent = Intent(Intent.ACTION_DIAL, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (_: Exception) {}
  }

  fun sendSms(phoneNumber: String = "", body: String = "") {
    try {
      val uri = Uri.parse("smsto:${phoneNumber.trim()}")
      val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
        putExtra("sms_body", body)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (_: Exception) {}
  }
}
