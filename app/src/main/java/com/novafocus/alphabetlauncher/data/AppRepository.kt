package com.novafocus.alphabetlauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(private val packageManager: PackageManager) {
    private var cachedApps: List<InstalledApp>? = null

    suspend fun loadLaunchableApps(forceReload: Boolean = false): List<InstalledApp> = withContext(Dispatchers.Default) {
        if (!forceReload && cachedApps != null) {
            return@withContext cachedApps!!
        }
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = if (Build.VERSION.SDK_INT >= 33) {
            packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }
        val apps = resolveInfos.map { ri ->
            InstalledApp(
                packageName = ri.activityInfo.packageName,
                label = ri.loadLabel(packageManager).toString(),
                icon = ri.loadIcon(packageManager),
                firstLetter = ri.loadLabel(packageManager).toString().firstOrNull()?.uppercaseChar() ?: '#'
            )
        }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
        cachedApps = apps
        apps
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providePackageManager(@ApplicationContext context: Context): PackageManager = context.packageManager
}
