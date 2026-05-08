package com.wikidoc.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wikidoc.domain.model.Document
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

    private val _showCreateDocumentDialog = MutableStateFlow(false)
    val showCreateDocumentDialog: StateFlow<Boolean> = _showCreateDocumentDialog.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                documentRepository.getRecentDocuments(50),
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

    fun showCreateDocumentDialog() {
        _showCreateDocumentDialog.value = true
    }

    fun hideCreateDocumentDialog() {
        _showCreateDocumentDialog.value = false
    }

    fun toggleFavorite(document: Document) {
        viewModelScope.launch {
            val updated = document.copy(isFavorite = !document.isFavorite)
            documentRepository.updateDocument(updated)
        }
    }

    fun createDocument(title: String) {
        viewModelScope.launch {
            val document = Document(
                title = title,
                content = "",
                folderId = null,
                tags = emptyList(),
                isFavorite = false,
                wordCount = 0,
                updatedAt = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis()
            )
            documentRepository.saveDocument(document)
            hideCreateDocumentDialog()
        }
    }
}
