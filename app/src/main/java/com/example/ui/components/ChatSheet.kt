package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.theme.ShadowAccentCyan
import com.example.ui.theme.ShadowAccentPurple
import com.example.ui.theme.ShadowDarkBackground
import com.example.ui.theme.ShadowGlowMuted
import com.example.ui.theme.ShadowGlowWhite
import com.example.ui.theme.ShadowSurfaceBorder
import com.example.ui.theme.ShadowSurfaceDark
import com.example.ui.theme.ShadowSurfaceElevated
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSheet(
  messages: List<ChatMessage>,
  onSendMessage: (String) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
  var textInput by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
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
            text = "Dialogue & Actions",
            color = ShadowGlowWhite,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "سجل المحادثة والأوامر المنفذة",
            color = ShadowGlowMuted,
            fontSize = 12.sp
          )
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = ShadowGlowWhite)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Messages list
      if (messages.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f, fill = false)
            .height(200.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No messages yet. Speak or type to Shadow!",
            color = ShadowGlowMuted,
            fontSize = 14.sp
          )
        }
      } else {
        LazyColumn(
          state = listState,
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f, fill = false)
            .height(380.dp),
          contentPadding = PaddingValues(vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(messages) { msg ->
            MessageItem(message = msg)
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Text Input Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = textInput,
          onValueChange = { textInput = it },
          placeholder = { Text("Ask Shadow or instruct device...", color = ShadowGlowMuted, fontSize = 14.sp) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = ShadowSurfaceDark,
            unfocusedContainerColor = ShadowSurfaceDark,
            focusedTextColor = ShadowGlowWhite,
            unfocusedTextColor = ShadowGlowWhite,
            focusedBorderColor = ShadowAccentCyan,
            unfocusedBorderColor = ShadowSurfaceBorder
          ),
          shape = RoundedCornerShape(24.dp),
          modifier = Modifier.weight(1f),
          singleLine = true
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (textInput.isNotBlank()) ShadowAccentCyan else ShadowSurfaceElevated)
            .border(1.dp, ShadowSurfaceBorder, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          IconButton(
            onClick = {
              if (textInput.isNotBlank()) {
                onSendMessage(textInput)
                textInput = ""
              }
            },
            enabled = textInput.isNotBlank()
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Send,
              contentDescription = "Send",
              tint = if (textInput.isNotBlank()) Color.Black else ShadowGlowMuted,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
    }
  }
}

@Composable
private fun MessageItem(message: ChatMessage) {
  val isUser = message.isUser
  val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
  val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
  ) {
    Box(
      modifier = Modifier
        .clip(
          RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = if (isUser) 16.dp else 4.dp,
            bottomEnd = if (isUser) 4.dp else 16.dp
          )
        )
        .background(if (isUser) ShadowAccentPurple.copy(alpha = 0.25f) else ShadowSurfaceElevated)
        .border(
          width = 1.dp,
          color = if (isUser) ShadowAccentPurple.copy(alpha = 0.5f) else ShadowSurfaceBorder,
          shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = if (isUser) 16.dp else 4.dp,
            bottomEnd = if (isUser) 4.dp else 16.dp
          )
        )
        .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
      Column {
        if (!isUser) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(ShadowAccentCyan)
            )
            Text(
              text = "Shadow • ${message.expression.displayName}",
              color = ShadowAccentCyan,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
        }

        Text(
          text = message.text,
          color = ShadowGlowWhite,
          fontSize = 14.sp,
          lineHeight = 20.sp
        )

        message.actionSummary?.let { action ->
          Spacer(modifier = Modifier.height(6.dp))
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(ShadowDarkBackground)
              .border(1.dp, ShadowAccentCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = ShadowAccentCyan,
                modifier = Modifier.size(12.dp)
              )
              Text(
                text = action,
                color = ShadowAccentCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = formattedTime,
          color = ShadowGlowMuted,
          fontSize = 10.sp,
          modifier = Modifier.align(Alignment.End)
        )
      }
    }
  }
}
