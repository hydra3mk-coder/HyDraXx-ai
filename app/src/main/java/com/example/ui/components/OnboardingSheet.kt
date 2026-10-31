package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.AppLanguage
import com.example.data.model.AssistantExpression
import com.example.data.model.AuthProvider
import com.example.data.model.AuthUser
import com.example.ui.theme.ShadowAccentCyan
import com.example.ui.theme.ShadowAccentGreen
import com.example.ui.theme.ShadowAccentPurple
import com.example.ui.theme.ShadowDarkBackground
import com.example.ui.theme.ShadowGlowMuted
import com.example.ui.theme.ShadowGlowWhite
import com.example.ui.theme.ShadowSurfaceBorder
import com.example.ui.theme.ShadowSurfaceDark
import com.example.ui.theme.ShadowSurfaceElevated

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingSheet(
  language: AppLanguage,
  onLanguageChange: (AppLanguage) -> Unit,
  onUserSignIn: (AuthUser) -> Unit,
  onComplete: () -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var currentStep by remember { mutableIntStateOf(0) } // 0 = Welcome, 1 = Permissions, 2 = Account

  // Permissions state tracker
  var hasMicPermission by remember {
    mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
  }
  var hasCameraPermission by remember {
    mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
  }
  var hasOverlayPermission by remember {
    mutableStateOf(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(context) else true)
  }

  val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
    hasMicPermission = granted
  }
  val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
    hasCameraPermission = granted
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
        .imePadding()
        .navigationBarsPadding()
        .padding(horizontal = 20.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top step indicator
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
          for (i in 0..2) {
            Box(
              modifier = Modifier
                .size(width = if (i == currentStep) 24.dp else 8.dp, height = 6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(if (i == currentStep) ShadowAccentCyan else ShadowSurfaceBorder)
            )
          }
        }

        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = ShadowGlowMuted)
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      AnimatedContent(targetState = currentStep, label = "onboarding_step") { step ->
        when (step) {
          0 -> {
            // STEP 1: WELCOME & LIVING COMPANION
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.fillMaxWidth()
            ) {
              ShadowFaceView(
                expression = AssistantExpression.HAPPY,
                size = 140.dp
              )

              Spacer(modifier = Modifier.height(12.dp))

              Text(
                text = if (language == AppLanguage.ARABIC) "مرحباً بك في HyDraXx" else "Welcome to HyDraXx",
                color = ShadowGlowWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
              )

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = if (language == AppLanguage.ARABIC)
                  "مساعدك الذكي الحي الذي يعيش داخل هاتفك. يسمعك، يفهمك، ينام عند تركه، ويتفاعل معك بلطف."
                else
                  "Your living AI companion residing natively inside your device. Listens, learns, falls asleep, and helps you with everything.",
                color = ShadowGlowMuted,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
              )

              Spacer(modifier = Modifier.height(18.dp))

              // Language selector pill
              Row(
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .background(ShadowSurfaceDark)
                  .border(1.dp, ShadowSurfaceBorder, RoundedCornerShape(20.dp))
                  .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                LanguageChip(
                  label = "العربية",
                  isSelected = language == AppLanguage.ARABIC,
                  onClick = { onLanguageChange(AppLanguage.ARABIC) }
                )
                LanguageChip(
                  label = "English",
                  isSelected = language == AppLanguage.ENGLISH,
                  onClick = { onLanguageChange(AppLanguage.ENGLISH) }
                )
              }

              Spacer(modifier = Modifier.height(24.dp))

              Button(
                onClick = { currentStep = 1 },
                colors = ButtonDefaults.buttonColors(containerColor = ShadowAccentCyan),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
              ) {
                Text(
                  text = if (language == AppLanguage.ARABIC) "بدء الإعداد والصلاحيات" else "Configure Permissions",
                  color = Color.Black,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                )
              }
            }
          }

          1 -> {
            // STEP 2: SYSTEM PERMISSIONS GATE
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = if (language == AppLanguage.ARABIC) "صلاحيات النظام المطلوبة" else "System Permissions Gate",
                color = ShadowGlowWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
              )

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = if (language == AppLanguage.ARABIC)
                  "يحتاج HyDraXx هذه الصلاحيات للتفاعل الصوتي المباشر والتحكم في إضاءة وتطبيقات الهاتف:"
                else
                  "HyDraXx needs these permissions to listen to your voice and control phone features:",
                color = ShadowGlowMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
              )

              Spacer(modifier = Modifier.height(16.dp))

              // Permission rows
              PermissionRow(
                icon = Icons.Default.Mic,
                title = if (language == AppLanguage.ARABIC) "الميكروفون (التحدث الصوتي)" else "Microphone (Voice Interaction)",
                description = if (language == AppLanguage.ARABIC) "لسماع أوامرك والتفاعل مع نبرة صوتك" else "To listen and respond to speech",
                isGranted = hasMicPermission,
                onGrant = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) }
              )

              Spacer(modifier = Modifier.height(10.dp))

              PermissionRow(
                icon = Icons.Default.CameraAlt,
                title = if (language == AppLanguage.ARABIC) "الكاميرا والكشاف" else "Camera & Flashlight",
                description = if (language == AppLanguage.ARABIC) "للتحكم بإضاءة الفلاش ومسح الأشياء" else "To toggle torch and scan",
                isGranted = hasCameraPermission,
                onGrant = { cameraLauncher.launch(Manifest.permission.CAMERA) }
              )

              Spacer(modifier = Modifier.height(10.dp))

              PermissionRow(
                icon = Icons.Default.Layers,
                title = if (language == AppLanguage.ARABIC) "الظهور فوق التطبيقات (الجزيرة)" else "Display Over Other Apps",
                description = if (language == AppLanguage.ARABIC) "لعرض الجزيرة التفاعلية والوجه العائم" else "To show dynamic island & floating eyes",
                isGranted = hasOverlayPermission,
                onGrant = {
                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")).apply {
                      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                  }
                }
              )

              Spacer(modifier = Modifier.height(20.dp))

              Button(
                onClick = { currentStep = 2 },
                colors = ButtonDefaults.buttonColors(containerColor = ShadowAccentCyan),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
              ) {
                Text(
                  text = if (language == AppLanguage.ARABIC) "التالي: الحساب والمزامنة" else "Next: Account Setup",
                  color = Color.Black,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                )
              }
            }
          }

          2 -> {
            // STEP 3: ACCOUNT & SIGN-IN (GOOGLE, GITHUB, GUEST)
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = if (language == AppLanguage.ARABIC) "تسجيل الدخول ومزامنة الذكاء" else "Sign In & Cloud Sync",
                color = ShadowGlowWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
              )

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = if (language == AppLanguage.ARABIC)
                  "احفظ ذكرياتك وتفضيلاتك عبر حسابك في قوقل أو قيت هاب أو تابع كضيف:"
                else
                  "Sync your companion memories and preferences across devices:",
                color = ShadowGlowMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
              )

              Spacer(modifier = Modifier.height(18.dp))

              // Google Sign In Option
              AuthButton(
                providerName = "Sign in with Google",
                arabicName = "تسجيل الدخول بحساب قوقل",
                accentColor = ShadowAccentCyan,
                onClick = {
                  onUserSignIn(
                    AuthUser(
                      uid = "google_user_${System.currentTimeMillis()}",
                      displayName = "Hydra Google",
                      email = "hydra@google.com",
                      provider = AuthProvider.GOOGLE
                    )
                  )
                  onComplete()
                }
              )

              Spacer(modifier = Modifier.height(10.dp))

              // GitHub Sign In Option
              AuthButton(
                providerName = "Sign in with GitHub",
                arabicName = "تسجيل الدخول بحساب GitHub",
                accentColor = ShadowAccentPurple,
                onClick = {
                  onUserSignIn(
                    AuthUser(
                      uid = "github_user_${System.currentTimeMillis()}",
                      displayName = "Hydra GitHub Dev",
                      email = "hydra@github.com",
                      provider = AuthProvider.GITHUB
                    )
                  )
                  onComplete()
                }
              )

              Spacer(modifier = Modifier.height(10.dp))

              // Guest Mode Option
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(14.dp))
                  .background(ShadowSurfaceElevated)
                  .border(1.dp, ShadowSurfaceBorder, RoundedCornerShape(14.dp))
                  .clickable {
                    onUserSignIn(
                      AuthUser(
                        uid = "guest_${System.currentTimeMillis()}",
                        displayName = "Companion Guest",
                        email = "local@offline.device",
                        provider = AuthProvider.GUEST
                      )
                    )
                    onComplete()
                  }
                  .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = if (language == AppLanguage.ARABIC) "المتابعة كضيف على هذا الجهاز فقط" else "Continue as Local Guest",
                  color = ShadowGlowMuted,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun LanguageChip(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(16.dp))
      .background(if (isSelected) ShadowAccentCyan else Color.Transparent)
      .clickable { onClick() }
      .padding(horizontal = 16.dp, vertical = 6.dp)
  ) {
    Text(
      text = label,
      color = if (isSelected) Color.Black else ShadowGlowWhite,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold
    )
  }
}

@Composable
private fun PermissionRow(
  icon: ImageVector,
  title: String,
  description: String,
  isGranted: Boolean,
  onGrant: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(ShadowSurfaceDark)
      .border(1.dp, if (isGranted) ShadowAccentGreen.copy(alpha = 0.5f) else ShadowSurfaceBorder, RoundedCornerShape(12.dp))
      .padding(12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(ShadowSurfaceElevated),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = if (isGranted) ShadowAccentGreen else ShadowAccentCyan, modifier = Modifier.size(18.dp))
      }
      Column {
        Text(text = title, color = ShadowGlowWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text(text = description, color = ShadowGlowMuted, fontSize = 10.sp)
      }
    }

    if (isGranted) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(ShadowAccentGreen.copy(alpha = 0.2f))
          .padding(horizontal = 8.dp, vertical = 4.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          Icon(Icons.Default.Check, contentDescription = null, tint = ShadowAccentGreen, modifier = Modifier.size(12.dp))
          Text(text = "Granted", color = ShadowAccentGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    } else {
      Button(
        onClick = onGrant,
        colors = ButtonDefaults.buttonColors(containerColor = ShadowSurfaceElevated),
        shape = RoundedCornerShape(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp)
      ) {
        Text(text = "Allow", color = ShadowAccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
private fun AuthButton(
  providerName: String,
  arabicName: String,
  accentColor: Color,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .background(ShadowSurfaceDark)
      .border(1.2.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
      .clickable { onClick() }
      .padding(horizontal = 16.dp, vertical = 13.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Icon(Icons.Default.Person, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
      Text(
        text = providerName,
        color = ShadowGlowWhite,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}
