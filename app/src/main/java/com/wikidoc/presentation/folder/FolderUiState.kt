package com.wikidoc.presentation.folder

import com.wikidoc.domain.model.Document
import com.wikidoc.domain.model.Folder

data class FolderUiState(
    val isLoading: Boolean = true,
    val folder: Folder? = null,
    val documents: List<Document> = emptyList(),
    val childFolders: List<Folder> = emptyList()
)
