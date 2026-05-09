package com.wikidoc.presentation.settings

import androidx.lifecycle.ViewModel
import com.wikidoc.data.external.ExternalDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val externalDataStore: ExternalDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setFontSize(size: Int) {
        _uiState.update { it.copy(fontSize = size) }
    }

    fun setAutoSave(enabled: Boolean) {
        _uiState.update { it.copy(autoSave = enabled) }
    }

    fun setTheme(theme: String) {
        _uiState.update { it.copy(theme = theme) }
    }

    fun clearAllData() {
        runBlocking {
            externalDataStore.clearAllData()
        }
    }
}
