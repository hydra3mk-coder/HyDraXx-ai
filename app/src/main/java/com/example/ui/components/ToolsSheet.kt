package com.example.ui.components

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.NetworkWifi
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeviceStatus
import com.example.data.model.InstalledAppInfo
import com.example.ui.theme.ShadowAccentAmber
import com.example.ui.theme.ShadowAccentCyan
import com.example.ui.theme.ShadowAccentGreen
import com.example.ui.theme.ShadowDarkBackground
import com.example.ui.theme.ShadowGlowMuted
import com.example.ui.theme.ShadowGlowWhite
import com.example.ui.theme.ShadowSurfaceBorder
import com.example.ui.theme.ShadowSurfaceDark
import com.example.ui.theme.ShadowSurfaceElevated

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsSheet(
  deviceStatus: DeviceStatus,
  installedApps: List<InstalledAppInfo>,
  onToggleTorch: () -> Unit,
  onSetRinger: (String) -> Unit,
  onLaunchApp: (String) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
  var appSearchQuery by remember { mutableStateOf("") }

  val filteredApps = remember(appSearchQuery, installedApps) {
    if (appSearchQuery.isBlank()) installedApps.take(16)
    else installedApps.filter { it.label.contains(appSearchQuery, ignoreCase = true) }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = ShadowDarkBackground,
    tonalElevation = 8.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
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
            text = "Device Cockpit",
            color = ShadowGlowWhite,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "لوحة التحكم بالهاتف والتطبيقات",
            color = ShadowGlowMuted,
            fontSize = 12.sp
          )
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = ShadowGlowWhite)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Telemetry Cards Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Battery Card
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(ShadowSurfaceDark)
            .border(1.dp, ShadowSurfaceBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
        ) {
          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                if (deviceStatus.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                contentDescription = null,
                tint = if (deviceStatus.batteryLevel > 20) ShadowAccentGreen else ShadowAccentAmber,
                modifier = Modifier.size(18.dp)
              )
              Text("Battery", color = ShadowGlowMuted, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "${deviceStatus.batteryLevel}%",
              color = ShadowGlowWhite,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = if (deviceStatus.isCharging) "Charging" else "On Battery",
              color = ShadowGlowMuted,
              fontSize = 10.sp
            )
          }
        }

        // RAM Card
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(ShadowSurfaceDark)
            .border(1.dp, ShadowSurfaceBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
        ) {
          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(Icons.Default.Apps, contentDescription = null, tint = ShadowAccentCyan, modifier = Modifier.size(18.dp))
              Text("RAM", color = ShadowGlowMuted, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "${deviceStatus.freeRamMb} MB",
              color = ShadowGlowWhite,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Free memory",
              color = ShadowGlowMuted,
              fontSize = 10.sp
            )
          }
        }

        // Storage Card
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(ShadowSurfaceDark)
            .border(1.dp, ShadowSurfaceBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
        ) {
          Column {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(Icons.Default.Storage, contentDescription = null, tint = ShadowGlowWhite, modifier = Modifier.size(18.dp))
              Text("Storage", color = ShadowGlowMuted, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "${deviceStatus.freeStorageGb} GB",
              color = ShadowGlowWhite,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Available",
              color = ShadowGlowMuted,
              fontSize = 10.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Fast Controls Row
      Text(text = "Quick Actions:", color = ShadowGlowMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        QuickActionButton(
          label = if (deviceStatus.isTorchOn) "Torch ON" else "Torch OFF",
          icon = if (deviceStatus.isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
          isActive = deviceStatus.isTorchOn,
          onClick = onToggleTorch,
          modifier = Modifier.weight(1f)
        )

        QuickActionButton(
          label = "Normal",
          icon = Icons.Default.VolumeUp,
          isActive = false,
          onClick = { onSetRinger("normal") },
          modifier = Modifier.weight(1f)
        )

        QuickActionButton(
          label = "Vibrate",
          icon = Icons.Default.Vibration,
          isActive = false,
          onClick = { onSetRinger("vibrate") },
          modifier = Modifier.weight(1f)
        )

        QuickActionButton(
          label = "Silent",
          icon = Icons.Default.NotificationsOff,
          isActive = false,
          onClick = { onSetRinger("silent") },
          modifier = Modifier.weight(1f)
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Installed Apps Section
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = "App Launcher (${installedApps.size} apps found):", color = ShadowGlowMuted, fontSize = 13.sp)
      }

      Spacer(modifier = Modifier.height(8.dp))

      OutlinedTextField(
        value = appSearchQuery,
        onValueChange = { appSearchQuery = it },
        placeholder = { Text("Filter apps (e.g. YouTube, Camera, WhatsApp)...", fontSize = 12.sp, color = ShadowGlowMuted) },
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = ShadowGlowWhite,
          unfocusedTextColor = ShadowGlowWhite,
          focusedBorderColor = ShadowAccentCyan,
          unfocusedBorderColor = ShadowSurfaceBorder
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))

      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .height(200.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        items(filteredApps) { app ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(ShadowSurfaceDark)
              .border(1.dp, ShadowSurfaceBorder, RoundedCornerShape(10.dp))
              .clickable { onLaunchApp(app.packageName) }
              .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(text = app.label, color = ShadowGlowWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
              Text(text = app.packageName, color = ShadowGlowMuted, fontSize = 10.sp, maxLines = 1)
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(ShadowSurfaceElevated)
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(text = "Launch", color = ShadowAccentCyan, fontSize = 11.sp)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun QuickActionButton(
  label: String,
  icon: ImageVector,
  isActive: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(if (isActive) ShadowAccentCyan.copy(alpha = 0.2f) else ShadowSurfaceDark)
      .border(
        width = 1.dp,
        color = if (isActive) ShadowAccentCyan else ShadowSurfaceBorder,
        shape = RoundedCornerShape(12.dp)
      )
      .clickable { onClick() }
      .padding(vertical = 10.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (isActive) ShadowAccentCyan else ShadowGlowWhite,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = label,
        color = if (isActive) ShadowAccentCyan else ShadowGlowWhite,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium
      )
    }
  }
}
