package com.novafocus.alphabetlauncher.viewmodel

import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novafocus.alphabetlauncher.data.AppRepository
import com.novafocus.alphabetlauncher.data.InstalledApp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "alphabet_launcher_prefs")
private val FAVORITES_KEY = stringSetPreferencesKey("favorite_package_names")

@HiltViewModel
class LauncherViewModel @Inject constructor(
    private val appRepository: AppRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _allApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val allApps: StateFlow<List<InstalledApp>> = _allApps.asStateFlow()

    private val _appsByLetter = MutableStateFlow<Map<Char, List<InstalledApp>>>(emptyMap())
    val appsByLetter: StateFlow<Map<Char, List<InstalledApp>>> = _appsByLetter.asStateFlow()

    private val _favoritePackages = MutableStateFlow<Set<String>>(emptySet())

    val favoriteApps: StateFlow<List<InstalledApp>> = combine(_allApps, _favoritePackages) { apps, favs ->
        if (favs.isNotEmpty()) {
            val favList = apps.filter { it.packageName in favs }
            if (favList.isNotEmpty()) favList else apps.take(6)
        } else {
            apps.take(6)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedLetterIndex = MutableStateFlow<Int?>(null)
    val selectedLetterIndex: StateFlow<Int?> = _selectedLetterIndex.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            loadFavorites()
            loadApps()
        }
    }

    private suspend fun loadFavorites() {
        try {
            val prefs = context.dataStore.data.first()
            val savedFavs = prefs[FAVORITES_KEY] ?: emptySet()
            _favoritePackages.value = savedFavs
        } catch (_: Exception) {
            _favoritePackages.value = emptySet()
        }
    }

    fun loadApps(forceReload: Boolean = false) {
        viewModelScope.launch {
            val loaded = appRepository.loadLaunchableApps(forceReload)
            _allApps.value = loaded
            _appsByLetter.value = loaded.groupBy { it.firstLetter }
        }
    }

    fun selectLetter(index: Int?) {
        _selectedLetterIndex.value = index
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(packageName: String) {
        viewModelScope.launch {
            val current = _favoritePackages.value.toMutableSet()
            if (current.contains(packageName)) {
                current.remove(packageName)
            } else {
                current.add(packageName)
            }
            _favoritePackages.value = current
            try {
                context.dataStore.edit { prefs ->
                    prefs[FAVORITES_KEY] = current
                }
            } catch (_: Exception) {
            }
        }
    }

    fun launchApp(context: Context, packageName: String) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } catch (_: Exception) {
        }
    }
}
