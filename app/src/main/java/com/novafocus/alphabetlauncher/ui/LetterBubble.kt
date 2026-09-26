package com.novafocus.alphabetlauncher.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun LetterBubble(
    letter: String,
    touchY: Float?,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val bubbleSize = 64.dp
    val bubbleSizePx = with(density) { bubbleSize.toPx() }
    val bubbleOffsetFromBarPx = with(density) { 76.dp.toPx() }

    AnimatedVisibility(
        visible = touchY != null,
        enter = fadeIn() + scaleIn(initialScale = 0.7f),
        exit = fadeOut() + scaleOut(targetScale = 0.7f),
        modifier = modifier
    ) {
        if (touchY != null) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = -bubbleOffsetFromBarPx.roundToInt(),
                            y = (touchY - bubbleSizePx / 2f).roundToInt()
                        )
                    }
                    .size(bubbleSize)
                    .shadow(elevation = 8.dp, shape = CircleShape)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
