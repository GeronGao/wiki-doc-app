package com.wikidoc.presentation.image

import com.wikidoc.domain.model.WikiImage

data class ImageManagerUiState(
    val isLoading: Boolean = true,
    val images: List<WikiImage> = emptyList(),
    val selectedTab: Int = 0,
    val isSelectionMode: Boolean = false,
    val selectedImages: Set<Long> = emptySet()
)
