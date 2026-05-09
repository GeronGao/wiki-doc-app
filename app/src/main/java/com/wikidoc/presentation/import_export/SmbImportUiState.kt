package com.wikidoc.presentation.import_export

import com.wikidoc.data.smb.SmbFileInfo

data class SmbImportUiState(
    val isLoading: Boolean = false,
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val connectionError: String? = null,
    val serverAddress: String = "",
    val username: String = "",
    val password: String = "",
    val currentPath: String = "",
    val items: List<SmbFileItem> = emptyList(),
    val selectedItems: Set<String> = emptySet(),
    val isImporting: Boolean = false,
    val importProgress: Float = 0f,
    val importStatus: String = "",
    val isImportComplete: Boolean = false,
    val importedCount: Int = 0
)

data class SmbFileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val isSelected: Boolean = false,
    val isExpanded: Boolean = false,
    val children: List<SmbFileItem> = emptyList(),
    val isLoading: Boolean = false
) {
    val isMarkdownFile: Boolean
        get() = name.endsWith(".md", ignoreCase = true) || name.endsWith(".markdown", ignoreCase = true)

    val isZipFile: Boolean
        get() = name.endsWith(".zip", ignoreCase = true)

    val isSupported: Boolean
        get() = isMarkdownFile || isZipFile
}

fun SmbFileInfo.toSmbFileItem(): SmbFileItem {
    return SmbFileItem(
        name = name,
        path = path,
        isDirectory = isDirectory,
        size = size
    )
}
