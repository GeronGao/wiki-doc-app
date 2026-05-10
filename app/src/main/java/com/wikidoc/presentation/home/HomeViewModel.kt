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

    private val _dragTargetedFolder = MutableStateFlow<Long?>(null)
    val dragTargetedFolder: StateFlow<Long?> = _dragTargetedFolder.asStateFlow()

    init {
        loadData()
    }

    fun setDragTargetedFolder(folderId: Long?) {
        _dragTargetedFolder.value = folderId
    }

    fun moveDocumentToFolder(documentId: Long, folderId: Long) {
        viewModelScope.launch {
            val document = documentRepository.getDocumentById(documentId)
            if (document != null) {
                val updated = document.copy(folderId = folderId, updatedAt = System.currentTimeMillis())
                documentRepository.updateDocument(updated)
            }
            _dragTargetedFolder.value = null
        }
    }

    fun moveFolderToFolder(folderId: Long, targetFolderId: Long) {
        viewModelScope.launch {
            val folder = folderRepository.getFolderById(folderId)
            if (folder != null) {
                val updated = folder.copy(parentId = targetFolderId)
                folderRepository.updateFolder(updated)
            }
            _dragTargetedFolder.value = null
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                documentRepository.getRootDocuments(),
                documentRepository.getFavoriteDocuments(),
                folderRepository.getRootFolders()
            ) { rootDocs, favorites, folders ->
                HomeUiState(
                    isLoading = false,
                    recentDocuments = rootDocs,
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

    fun deleteDocument(documentId: Long) {
        viewModelScope.launch {
            documentRepository.deleteDocument(documentId)
        }
    }

    fun deleteFolder(folderId: Long) {
        viewModelScope.launch {
            folderRepository.deleteFolder(folderId)
        }
    }
}
