package com.novafocus.alphabetlauncher.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novafocus.alphabetlauncher.ui.theme.AccentCyan
import com.novafocus.alphabetlauncher.ui.theme.AccentPrimary
import com.novafocus.alphabetlauncher.ui.theme.AccentStar
import com.novafocus.alphabetlauncher.ui.theme.AccentViolet
import kotlin.math.roundToInt

@Composable
fun LetterBubble(
    letter: String,
    touchY: Float?,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val bubbleSize = 68.dp
    val bubbleSizePx = with(density) { bubbleSize.toPx() }
    val bubbleOffsetFromBarPx = with(density) { 82.dp.toPx() }

    AnimatedVisibility(
        visible = touchY != null,
        enter = fadeIn() + scaleIn(initialScale = 0.6f),
        exit = fadeOut() + scaleOut(targetScale = 0.6f),
        modifier = modifier
    ) {
        if (touchY != null) {
            val isStar = letter == "★"
            val gradientBrush = if (isStar) {
                Brush.linearGradient(
                    listOf(AccentStar, Color(0xFFFF9100))
                )
            } else {
                Brush.linearGradient(
                    listOf(AccentPrimary, AccentViolet, AccentCyan)
                )
            }

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = -bubbleOffsetFromBarPx.roundToInt(),
                            y = (touchY - bubbleSizePx / 2f).roundToInt()
                        )
                    }
                    .size(bubbleSize)
                    .shadow(elevation = 14.dp, shape = CircleShape, ambientColor = AccentPrimary, spotColor = AccentPrimary)
                    .clip(CircleShape)
                    .background(brush = gradientBrush)
                    .border(width = 1.5.dp, color = Color.White.copy(alpha = 0.4f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    fontSize = if (isStar) 26.sp else 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
            }
        }
    }
}
