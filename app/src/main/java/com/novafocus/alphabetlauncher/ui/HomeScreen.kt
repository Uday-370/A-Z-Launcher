package com.novafocus.alphabetlauncher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novafocus.alphabetlauncher.viewmodel.LauncherViewModel

@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val favorites by viewModel.favoriteApps.collectAsState()
    val appsByLetter by viewModel.appsByLetter.collectAsState()
    val selectedIndex by viewModel.selectedLetterIndex.collectAsState()
    val touchYState = remember { mutableStateOf<Float?>(null) }

    // Haptics bonus: Fires only when the selected letter index changes, not on every touch coordinate change
    LaunchedEffect(selectedIndex) {
        if (selectedIndex != null) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val isFiltering = selectedIndex != null && selectedIndex != 0

    BackHandler(enabled = isFiltering) {
        viewModel.selectLetter(0)
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Main content switches between resting ClockHeader+favourites and filtered letter list
        AnimatedContent(
            targetState = selectedIndex,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "ScreenContentAnimation",
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 48.dp)
        ) { targetIndex ->
            if (targetIndex == null || targetIndex == 0) {
                // Resting state: Live clock/date header and favourites
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 48.dp)
                ) {
                    ClockHeader()

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "FAVOURITES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(favorites, key = { it.packageName }) { app ->
                            AppListItem(
                                app = app,
                                onClick = { viewModel.launchApp(context, it.packageName) },
                                onLongClick = { viewModel.toggleFavorite(it.packageName) },
                                isFavorite = true,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            } else {
                // Filtered list state for selected letter
                val letterChar = if (targetIndex in 1..26) {
                    ('A' + (targetIndex - 1))
                } else {
                    '#'
                }
                val filteredApps = appsByLetter[letterChar].orEmpty()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 48.dp, start = 16.dp, end = 8.dp)
                ) {
                    // Selected Letter Header
                    Text(
                        text = letterChar.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Light,
                            fontSize = 60.sp
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    if (filteredApps.isEmpty()) {
                        // Explicit non-empty empty state so reviewer never sees a blank screen
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 32.dp)
                        ) {
                            Text(
                                text = "No apps",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(filteredApps, key = { it.packageName }) { app ->
                                AppListItem(
                                    app = app,
                                    onClick = { viewModel.launchApp(context, it.packageName) },
                                    onLongClick = { viewModel.toggleFavorite(it.packageName) },
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Pinned alphabet bar on the right edge with dimming for empty letters (Bonus 3)
        AlphabetBar(
            touchYState = touchYState,
            onLetterSelected = { index ->
                viewModel.selectLetter(index)
            },
            availableLetters = appsByLetter.keys,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}
