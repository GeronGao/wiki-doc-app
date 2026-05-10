package com.wikidoc.presentation.folder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wikidoc.domain.model.Document
import com.wikidoc.domain.model.Folder
import com.wikidoc.domain.repository.DocumentRepository
import com.wikidoc.domain.repository.FolderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FolderViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    private val folderRepository: FolderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val folderId: Long = savedStateHandle.get<Long>("folderId") ?: -1L

    private val _uiState = MutableStateFlow(FolderUiState())
    val uiState: StateFlow<FolderUiState> = _uiState.asStateFlow()

    val currentFolderId: Long = folderId

    private val _showCreateDocumentDialog = MutableStateFlow(false)
    val showCreateDocumentDialog: StateFlow<Boolean> = _showCreateDocumentDialog.asStateFlow()

    init {
        loadFolder()
    }

    private fun loadFolder() {
        viewModelScope.launch {
            if (folderId > 0) {
                val folder = folderRepository.getFolderById(folderId)
                _uiState.update { it.copy(folder = folder) }
            }

            combine(
                documentRepository.getDocumentsByFolder(folderId),
                folderRepository.getChildFolders(folderId)
            ) { documents, childFolders ->
                FolderUiState(
                    isLoading = false,
                    folder = _uiState.value.folder,
                    documents = documents,
                    childFolders = childFolders
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

    fun createDocument(title: String) {
        viewModelScope.launch {
            val document = Document(
                title = title.ifBlank { "无标题" },
                content = "",
                folderId = if (folderId > 0) folderId else null,
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

    fun createFolder(name: String) {
        viewModelScope.launch {
            val folder = Folder(
                name = name,
                parentId = if (folderId > 0) folderId else null,
                documentCount = 0,
                createdAt = System.currentTimeMillis()
            )
            folderRepository.saveFolder(folder)
        }
    }

    fun toggleFavorite(document: Document) {
        viewModelScope.launch {
            val updated = document.copy(isFavorite = !document.isFavorite)
            documentRepository.updateDocument(updated)
        }
    }

    fun moveDocumentToFolder(documentId: Long, targetFolderId: Long) {
        viewModelScope.launch {
            val document = documentRepository.getDocumentById(documentId)
            if (document != null) {
                val updated = document.copy(folderId = targetFolderId, updatedAt = System.currentTimeMillis())
                documentRepository.updateDocument(updated)
            }
        }
    }

    fun removeDocumentFromFolder(documentId: Long) {
        viewModelScope.launch {
            val document = documentRepository.getDocumentById(documentId)
            if (document != null) {
                val updated = document.copy(folderId = null, updatedAt = System.currentTimeMillis())
                documentRepository.updateDocument(updated)
            }
        }
    }

    fun moveDocumentToParentFolder(documentId: Long) {
        viewModelScope.launch {
            val document = documentRepository.getDocumentById(documentId)
            if (document != null && folderId > 0) {
                val currentFolder = folderRepository.getFolderById(folderId)
                val parentFolderId = currentFolder?.parentId
                val updated = document.copy(folderId = parentFolderId, updatedAt = System.currentTimeMillis())
                documentRepository.updateDocument(updated)
            }
        }
    }

    fun moveSubFolderToParent(subFolderId: Long) {
        viewModelScope.launch {
            val subFolder = folderRepository.getFolderById(subFolderId)
            if (subFolder != null && folderId > 0) {
                val currentFolder = folderRepository.getFolderById(folderId)
                val parentFolderId = currentFolder?.parentId
                val updated = subFolder.copy(parentId = parentFolderId)
                folderRepository.updateFolder(updated)
            }
        }
    }

    fun moveSubFolderToFolder(subFolderId: Long, targetFolderId: Long) {
        viewModelScope.launch {
            val subFolder = folderRepository.getFolderById(subFolderId)
            if (subFolder != null) {
                val updated = subFolder.copy(parentId = targetFolderId)
                folderRepository.saveFolder(updated)
            }
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
