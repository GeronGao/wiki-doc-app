package com.wikidoc.presentation.settings

data class SettingsUiState(
    val fontSize: Int = 16,
    val autoSave: Boolean = true,
    val theme: String = "system"
)
