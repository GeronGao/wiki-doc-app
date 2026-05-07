package com.wikidoc.presentation.search

import com.wikidoc.domain.model.Document

data class SearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val searchResults: List<Document> = emptyList(),
    val recentSearches: List<String> = emptyList()
)
