package com.wikidoc.presentation.home

import com.wikidoc.domain.model.Document
import com.wikidoc.domain.model.Folder

data class HomeUiState(
    val isLoading: Boolean = true,
    val recentDocuments: List<Document> = emptyList(),
    val favoriteDocuments: List<Document> = emptyList(),
    val folders: List<Folder> = emptyList()
)
