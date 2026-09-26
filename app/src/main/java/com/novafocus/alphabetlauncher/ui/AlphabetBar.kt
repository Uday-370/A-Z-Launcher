package com.novafocus.alphabetlauncher.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novafocus.alphabetlauncher.domain.CurveMath
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

val ALPHABET_ITEMS = listOf("★") + ('A'..'Z').map { it.toString() } + listOf("•")

@Composable
fun AlphabetBar(
    touchYState: MutableState<Float?>,
    onLetterSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    availableLetters: Set<Char> = emptySet(),
    onRelease: (() -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val letterRestYs = remember { FloatArray(28) }
    val barHeightPx = remember { mutableFloatStateOf(0f) }
    val currentLetterHoverIndex = remember { mutableIntStateOf(0) }
    val isTouching = remember { mutableStateOf(false) }

    val maxBulgePx = with(density) { 76.dp.toPx() }
    val animBulge = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(56.dp)
            .padding(vertical = 32.dp)
            .onGloballyPositioned { coordinates ->
                barHeightPx.floatValue = coordinates.size.height.toFloat()
            }
            .pointerInput(availableLetters) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    isTouching.value = true
                    touchYState.value = down.position.y
                    coroutineScope.launch {
                        animBulge.snapTo(maxBulgePx)
                    }
                    val initialIndex = CurveMath.nearestLetterIndex(down.position.y, letterRestYs)
                    currentLetterHoverIndex.intValue = initialIndex
                    onLetterSelected(initialIndex)

                    var pressed = true
                    while (pressed) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id }
                        if (change != null && change.pressed) {
                            touchYState.value = change.position.y
                            val index = CurveMath.nearestLetterIndex(change.position.y, letterRestYs)
                            currentLetterHoverIndex.intValue = index
                            onLetterSelected(index)
                            change.consume()
                        } else {
                            pressed = false
                        }
                    }

                    // Release handling: spring physics with overshoot
                    isTouching.value = false
                    coroutineScope.launch {
                        animBulge.animateTo(
                            targetValue = 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                        touchYState.value = null
                        onRelease?.invoke()
                    }
                }
            },
        contentAlignment = Alignment.CenterEnd
    ) {
        val letterSpacingPx = if (barHeightPx.floatValue > 0) barHeightPx.floatValue / 28f else 60f
        val sigmaPx = letterSpacingPx * 2.8f

        // 28 items column
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(40.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ALPHABET_ITEMS.forEachIndexed { index, item ->
                // Dim letters that have no installed apps (Bonus 3)
                val hasApps = when (index) {
                    0 -> true // Star (favourites / resting state) is always available
                    in 1..26 -> {
                        val char = 'A' + (index - 1)
                        char in availableLetters
                    }
                    else -> true // Symbol/dot category
                }
                val alpha = if (hasApps) 0.85f else 0.20f
                val fontWeight = if (hasApps) FontWeight.SemiBold else FontWeight.Normal

                Text(
                    text = item,
                    fontSize = 11.sp,
                    fontWeight = fontWeight,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = alpha),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .onGloballyPositioned { coordinates ->
                            val bounds = coordinates.boundsInParent()
                            letterRestYs[index] = bounds.center.y
                        }
                        .offset {
                            val touchY = touchYState.value
                            val currentBulge = animBulge.value
                            val restY = letterRestYs[index]
                            val x = if (touchY == null || currentBulge == 0f) {
                                0f
                            } else {
                                CurveMath.offsetFor(restY - touchY, currentBulge, sigmaPx)
                            }
                            IntOffset(x.roundToInt(), 0)
                        }
                )
            }
        }

        // Circular LetterBubble shown while actively touching
        val touchY = touchYState.value
        val hoverLetter = ALPHABET_ITEMS.getOrElse(currentLetterHoverIndex.intValue) { "A" }
        LetterBubble(
            letter = hoverLetter,
            touchY = if (isTouching.value) touchY else null,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}
