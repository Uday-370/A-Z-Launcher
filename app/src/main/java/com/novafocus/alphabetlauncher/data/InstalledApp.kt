package com.novafocus.alphabetlauncher.data

import android.graphics.drawable.Drawable

data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    val firstLetter: Char
)
