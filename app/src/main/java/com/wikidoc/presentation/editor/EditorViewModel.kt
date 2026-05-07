package com.wikidoc.presentation.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wikidoc.domain.model.Document
import com.wikidoc.domain.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val documentId: Long = savedStateHandle.get<Long>("documentId") ?: -1L

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private var currentDocumentId: Long? = null

    init {
        if (documentId > 0) {
            loadDocument(documentId)
        } else {
            _uiState.update {
                it.copy(isLoading = false, isNewDocument = true)
            }
        }
    }

    private fun loadDocument(id: Long) {
        viewModelScope.launch {
            val document = documentRepository.getDocumentById(id)
            if (document != null) {
                currentDocumentId = document.id
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isNewDocument = false,
                        title = document.title,
                        content = document.content
                    )
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, isNewDocument = true)
                }
            }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title, isSaved = false) }
    }

    fun onContentChange(content: String) {
        _uiState.update { it.copy(content = content, isSaved = false) }
    }

    fun togglePreview() {
        _uiState.update { it.copy(isPreviewMode = !it.isPreviewMode) }
    }

    fun saveDocument() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.title.isBlank() && state.content.isBlank()) {
                return@launch
            }

            val document = Document(
                id = currentDocumentId ?: 0,
                title = state.title.ifBlank { "无标题" },
                content = state.content,
                updatedAt = System.currentTimeMillis(),
                wordCount = state.content.split(Regex("\\s+")).size
            )

            if (currentDocumentId != null) {
                documentRepository.updateDocument(document)
            } else {
                currentDocumentId = documentRepository.saveDocument(document)
            }

            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun showSaveDialog() {
        _uiState.update { it.copy(showSaveDialog = true) }
    }

    fun dismissSaveDialog() {
        _uiState.update { it.copy(showSaveDialog = false) }
    }
}
