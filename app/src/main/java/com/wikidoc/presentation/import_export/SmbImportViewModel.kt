package com.wikidoc.presentation.import_export

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SmbImportViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SmbImportUiState())
    val uiState: StateFlow<SmbImportUiState> = _uiState.asStateFlow()

    fun updateServerAddress(address: String) {
        _uiState.update { it.copy(serverAddress = address) }
    }

    fun updateUsername(username: String) {
        _uiState.update { it.copy(username = username) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun connect() {
        _uiState.update { it.copy(isConnecting = true, errorMessage = null) }
    }

    fun toggleFileSelection(file: String) {
        _uiState.update { state ->
            val newSelected = if (file in state.selectedFiles) {
                state.selectedFiles - file
            } else {
                state.selectedFiles + file
            }
            state.copy(selectedFiles = newSelected)
        }
    }

    fun importSelected() {
        _uiState.update { it.copy(isImporting = true) }
    }
}
