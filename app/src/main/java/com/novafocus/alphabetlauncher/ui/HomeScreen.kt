package com.novafocus.alphabetlauncher.ui

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val favorites by viewModel.favoriteApps.collectAsState()
    val touchYState = remember { mutableStateOf<Float?>(null) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Main content area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 48.dp, top = 48.dp)
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

        // Pinned alphabet bar with touch interaction and Gaussian bending curve
        AlphabetBar(
            touchYState = touchYState,
            onLetterSelected = { index ->
                viewModel.selectLetter(index)
            },
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}
