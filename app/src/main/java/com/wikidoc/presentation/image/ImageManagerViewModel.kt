package com.wikidoc.presentation.image

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wikidoc.domain.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImageManagerViewModel @Inject constructor(
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImageManagerUiState())
    val uiState: StateFlow<ImageManagerUiState> = _uiState.asStateFlow()

    init {
        loadImages()
    }

    private fun loadImages() {
        viewModelScope.launch {
            imageRepository.getAllImages().collect { images ->
                _uiState.update {
                    it.copy(isLoading = false, images = images)
                }
            }
        }
    }

    fun onTabSelected(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
        viewModelScope.launch {
            val flow = when (tabIndex) {
                0 -> imageRepository.getAllImages()
                1 -> imageRepository.getUsedImages()
                2 -> imageRepository.getUnusedImages()
                else -> imageRepository.getAllImages()
            }
            flow.collect { images ->
                _uiState.update { it.copy(images = images) }
            }
        }
    }

    fun toggleSelection(imageId: Long) {
        _uiState.update { state ->
            val newSelected = if (imageId in state.selectedImages) {
                state.selectedImages - imageId
            } else {
                state.selectedImages + imageId
            }
            state.copy(
                selectedImages = newSelected,
                isSelectionMode = newSelected.isNotEmpty()
            )
        }
    }

    fun deleteSelectedImages() {
        viewModelScope.launch {
            _uiState.value.selectedImages.forEach { id ->
                imageRepository.deleteImage(id)
            }
            _uiState.update {
                it.copy(selectedImages = emptySet(), isSelectionMode = false)
            }
        }
    }
}
