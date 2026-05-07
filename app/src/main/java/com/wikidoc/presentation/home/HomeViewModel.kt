package com.wikidoc.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wikidoc.domain.repository.DocumentRepository
import com.wikidoc.domain.repository.FolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val folderRepository: FolderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                documentRepository.getRecentDocuments(10),
                documentRepository.getFavoriteDocuments(),
                folderRepository.getRootFolders()
            ) { recent, favorites, folders ->
                HomeUiState(
                    isLoading = false,
                    recentDocuments = recent,
                    favoriteDocuments = favorites,
                    folders = folders
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
