package com.example.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AssistantExpression
import com.example.ui.theme.ShadowAccentCyan
import com.example.ui.theme.ShadowDarkBackground
import com.example.ui.theme.ShadowGlowMuted
import com.example.ui.theme.ShadowGlowWhite
import com.example.ui.theme.ShadowSurfaceBorder
import com.example.ui.theme.ShadowSurfaceDark
import com.example.ui.theme.ShadowSurfaceElevated

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressionGallerySheet(
  currentExpression: AssistantExpression,
  onSelectExpression: (AssistantExpression) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var showArtworkReference by remember { mutableStateOf(false) }

  val expressions = listOf(
    AssistantExpression.NEUTRAL,
    AssistantExpression.HAPPY,
    AssistantExpression.SLEEPING,
    AssistantExpression.CURIOUS,
    AssistantExpression.SURPRISED,
    AssistantExpression.ANNOYED,
    AssistantExpression.LOVE,
    AssistantExpression.TIRED,
    AssistantExpression.PEEKING
  )

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = ShadowDarkBackground,
    tonalElevation = 8.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Expression Grid",
            color = ShadowGlowWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "معرض تعابير الوجه والتفاعل الحي",
            color = ShadowGlowMuted,
            fontSize = 13.sp
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(if (showArtworkReference) ShadowAccentCyan.copy(alpha = 0.2f) else ShadowSurfaceElevated)
              .clickable { showArtworkReference = !showArtworkReference }
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Text(
              text = if (showArtworkReference) "Interactive Grid" else "Original Grid",
              color = if (showArtworkReference) ShadowAccentCyan else ShadowGlowWhite,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = ShadowGlowWhite)
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (showArtworkReference) {
        // Show the user's uploaded master artwork
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, ShadowSurfaceBorder, RoundedCornerShape(16.dp))
            .background(ShadowSurfaceDark)
            .padding(8.dp),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(id = R.drawable.shadow_expressions),
            contentDescription = "Original Shadow Expressions Grid",
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Fit
          )
        }
      } else {
        // Interactive live animated face cards
        LazyVerticalGrid(
          columns = GridCells.Fixed(3),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(expressions) { exp ->
            val isSelected = exp == currentExpression
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) ShadowSurfaceElevated else ShadowSurfaceDark)
                .border(
                  width = if (isSelected) 1.5.dp else 1.dp,
                  color = if (isSelected) ShadowAccentCyan else ShadowSurfaceBorder,
                  shape = RoundedCornerShape(16.dp)
                )
                .clickable {
                  onSelectExpression(exp)
                }
                .padding(vertical = 12.dp, horizontal = 6.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                ShadowFaceView(
                  expression = exp,
                  size = 80.dp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(ShadowDarkBackground)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.AutoAwesome,
                      contentDescription = null,
                      tint = ShadowGlowWhite,
                      modifier = Modifier.size(10.dp)
                    )
                    Text(
                      text = exp.quote,
                      color = ShadowGlowWhite,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Medium
                    )
                  }
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
