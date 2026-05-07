package com.wikidoc.presentation.editor

data class EditorUiState(
    val isLoading: Boolean = true,
    val isNewDocument: Boolean = true,
    val title: String = "",
    val content: String = "",
    val isSaved: Boolean = true,
    val isPreviewMode: Boolean = false,
    val showSaveDialog: Boolean = false
)
