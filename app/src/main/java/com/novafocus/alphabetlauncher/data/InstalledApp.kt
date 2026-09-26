package com.novafocus.alphabetlauncher.data

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    val firstLetter: Char
) {
    // Lazily converted and cached ImageBitmap for smooth 60fps Jetpack Compose rendering
    val iconBitmap: ImageBitmap by lazy {
        try {
            icon.toBitmap(128, 128, Bitmap.Config.ARGB_8888).asImageBitmap()
        } catch (_: Exception) {
            // Fallback for custom drawables
            val bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            icon.setBounds(0, 0, canvas.width, canvas.height)
            icon.draw(canvas)
            bitmap.asImageBitmap()
        }
    }
}
