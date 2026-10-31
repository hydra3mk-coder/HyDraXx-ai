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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssistantTaskNote
import com.example.data.model.MemoryItem
import com.example.ui.theme.ShadowAccentCyan
import com.example.ui.theme.ShadowAccentGreen
import com.example.ui.theme.ShadowAccentRose
import com.example.ui.theme.ShadowDarkBackground
import com.example.ui.theme.ShadowGlowMuted
import com.example.ui.theme.ShadowGlowWhite
import com.example.ui.theme.ShadowSurfaceBorder
import com.example.ui.theme.ShadowSurfaceDark
import com.example.ui.theme.ShadowSurfaceElevated

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoriesSheet(
  memories: List<MemoryItem>,
  notes: List<AssistantTaskNote>,
  onAddMemory: (String, String) -> Unit,
  onDeleteMemory: (MemoryItem) -> Unit,
  onAddNote: (String) -> Unit,
  onToggleNote: (AssistantTaskNote) -> Unit,
  onDeleteNote: (AssistantTaskNote) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
  var selectedTab by remember { mutableIntStateOf(0) } // 0 = Memories, 1 = Notes

  var newMemKey by remember { mutableStateOf("") }
  var newMemVal by remember { mutableStateOf("") }
  var isAddingMemory by remember { mutableStateOf(false) }

  var newNoteContent by remember { mutableStateOf("") }
  var isAddingNote by remember { mutableStateOf(false) }

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
            text = "Companion Mind",
            color = ShadowGlowWhite,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "ذاكرة الظل والملاحظات الدائمة",
            color = ShadowGlowMuted,
            fontSize = 12.sp
          )
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = ShadowGlowWhite)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Tab selector
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = ShadowDarkBackground,
        contentColor = ShadowGlowWhite,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
            color = ShadowAccentCyan
          )
        }
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(Icons.Default.Memory, contentDescription = null, modifier = Modifier.size(16.dp))
              Text("Memories (${memories.size})", fontSize = 13.sp)
            }
          }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(Icons.Default.Note, contentDescription = null, modifier = Modifier.size(16.dp))
              Text("Notes (${notes.size})", fontSize = 13.sp)
            }
          }
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      if (selectedTab == 0) {
        // --- MEMORIES TAB ---
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "What Shadow learned about you:",
            color = ShadowGlowMuted,
            fontSize = 13.sp
          )
          Button(
            onClick = { isAddingMemory = !isAddingMemory },
            colors = ButtonDefaults.buttonColors(containerColor = ShadowSurfaceElevated),
            shape = RoundedCornerShape(20.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = ShadowAccentCyan, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Memory", color = ShadowAccentCyan, fontSize = 12.sp)
          }
        }

        if (isAddingMemory) {
          Spacer(modifier = Modifier.height(10.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(ShadowSurfaceDark)
              .border(1.dp, ShadowSurfaceBorder, RoundedCornerShape(12.dp))
              .padding(12.dp)
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = newMemKey,
                onValueChange = { newMemKey = it },
                label = { Text("Topic (e.g. Favorite Drink, Work)", fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = ShadowGlowWhite,
                  unfocusedTextColor = ShadowGlowWhite,
                  focusedBorderColor = ShadowAccentCyan,
                  unfocusedBorderColor = ShadowSurfaceBorder
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )
              OutlinedTextField(
                value = newMemVal,
                onValueChange = { newMemVal = it },
                label = { Text("Fact (e.g. Black coffee with no sugar)", fontSize = 12.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                  focusedTextColor = ShadowGlowWhite,
                  unfocusedTextColor = ShadowGlowWhite,
                  focusedBorderColor = ShadowAccentCyan,
                  unfocusedBorderColor = ShadowSurfaceBorder
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )
              Button(
                onClick = {
                  if (newMemKey.isNotBlank() && newMemVal.isNotBlank()) {
                    onAddMemory(newMemKey, newMemVal)
                    newMemKey = ""
                    newMemVal = ""
                    isAddingMemory = false
                  }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ShadowAccentCyan),
                modifier = Modifier.align(Alignment.End)
              ) {
                Text("Save to Memory", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (memories.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp),
            contentAlignment = Alignment.Center
          ) {
            Text("No memories yet. Tell Shadow things like: 'Remember that I love astronomy'", color = ShadowGlowMuted, fontSize = 13.sp)
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height(320.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(memories) { mem ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(ShadowSurfaceDark)
                  .border(1.dp, ShadowSurfaceBorder, RoundedCornerShape(12.dp))
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(text = mem.key, color = ShadowAccentCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(text = mem.value, color = ShadowGlowWhite, fontSize = 14.sp)
                }
                IconButton(onClick = { onDeleteMemory(mem) }) {
                  Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ShadowGlowMuted, modifier = Modifier.size(18.dp))
                }
              }
            }
          }
        }

      } else {
        // --- NOTES TAB ---
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "Quick Tasks & Voice Notes:", color = ShadowGlowMuted, fontSize = 13.sp)
          Button(
            onClick = { isAddingNote = !isAddingNote },
            colors = ButtonDefaults.buttonColors(containerColor = ShadowSurfaceElevated),
            shape = RoundedCornerShape(20.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = ShadowAccentGreen, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("New Note", color = ShadowAccentGreen, fontSize = 12.sp)
          }
        }

        if (isAddingNote) {
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = newNoteContent,
              onValueChange = { newNoteContent = it },
              placeholder = { Text("Note content...", fontSize = 13.sp) },
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ShadowGlowWhite,
                unfocusedTextColor = ShadowGlowWhite,
                focusedBorderColor = ShadowAccentGreen,
                unfocusedBorderColor = ShadowSurfaceBorder
              ),
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = {
                if (newNoteContent.isNotBlank()) {
                  onAddNote(newNoteContent)
                  newNoteContent = ""
                  isAddingNote = false
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = ShadowAccentGreen)
            ) {
              Text("Save", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (notes.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp),
            contentAlignment = Alignment.Center
          ) {
            Text("No notes saved. Say 'Take a note' to record one quickly!", color = ShadowGlowMuted, fontSize = 13.sp)
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height(320.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(notes) { note ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(ShadowSurfaceDark)
                  .border(1.dp, ShadowSurfaceBorder, RoundedCornerShape(12.dp))
                  .clickable { onToggleNote(note) }
                  .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = if (note.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                  contentDescription = null,
                  tint = if (note.isCompleted) ShadowAccentGreen else ShadowGlowMuted,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = note.content,
                  color = if (note.isCompleted) ShadowGlowMuted else ShadowGlowWhite,
                  fontSize = 14.sp,
                  textDecoration = if (note.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                  modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onDeleteNote(note) }) {
                  Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ShadowGlowMuted, modifier = Modifier.size(18.dp))
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
