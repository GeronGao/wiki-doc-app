package com.wikidoc.presentation.import_export

data class SmbImportUiState(
    val serverAddress: String = "",
    val username: String = "",
    val password: String = "",
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val sharedFolders: List<String> = emptyList(),
    val selectedFiles: Set<String> = emptySet(),
    val isImporting: Boolean = false,
    val errorMessage: String? = null
)
