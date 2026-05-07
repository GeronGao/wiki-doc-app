package com.wikidoc.presentation.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wikidoc.domain.model.Document
import com.wikidoc.domain.repository.DocumentRepository
import com.wikidoc.presentation.component.EditorMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditorUiState(
    val isLoading: Boolean = true,
    val isNewDocument: Boolean = true,
    val documentId: Long? = null,
    val title: String = "",
    val content: String = "",
    val folderId: Long? = null,
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val isSaved: Boolean = true,
    val editorMode: EditorMode = EditorMode.SPLIT,
    val showSaveDialog: Boolean = false,
    val showTagDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val wordCount: Int = 0,
    val lastSavedAt: Long? = null
)

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val documentRepository: DocumentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val documentId: Long = savedStateHandle.get<Long>("documentId") ?: -1L

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private var autoSaveJob: Job? = null

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
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isNewDocument = false,
                        documentId = document.id,
                        title = document.title,
                        content = document.content,
                        folderId = document.folderId,
                        tags = document.tags,
                        isFavorite = document.isFavorite,
                        wordCount = document.content.split(Regex("\\s+")).filter { it.isNotBlank() }.size,
                        lastSavedAt = document.updatedAt
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
        _uiState.update {
            it.copy(title = title, isSaved = false, wordCount = it.content.split(Regex("\\s+")).filter { w -> w.isNotBlank() }.size)
        }
        scheduleAutoSave()
    }

    fun onContentChange(content: String) {
        _uiState.update {
            it.copy(
                content = content,
                isSaved = false,
                wordCount = content.split(Regex("\\s+")).filter { w -> w.isNotBlank() }.size
            )
        }
        scheduleAutoSave()
    }

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(2000)
            saveDocument()
        }
    }

    fun setEditorMode(mode: EditorMode) {
        _uiState.update { it.copy(editorMode = mode) }
    }

    fun toggleFavorite() {
        _uiState.update { it.copy(isFavorite = !it.isFavorite, isSaved = false) }
        scheduleAutoSave()
    }

    fun saveDocument() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.title.isBlank() && state.content.isBlank()) {
                return@launch
            }

            val currentTime = System.currentTimeMillis()

            val document = Document(
                id = state.documentId ?: 0,
                title = state.title.ifBlank { "无标题" },
                content = state.content,
                folderId = state.folderId,
                tags = state.tags,
                isFavorite = state.isFavorite,
                wordCount = state.wordCount,
                updatedAt = currentTime,
                createdAt = if (state.isNewDocument) currentTime else state.lastSavedAt ?: currentTime
            )

            if (state.isNewDocument || state.documentId == null) {
                val newId = documentRepository.saveDocument(document)
                _uiState.update {
                    it.copy(
                        documentId = newId,
                        isNewDocument = false,
                        isSaved = true,
                        lastSavedAt = currentTime
                    )
                }
            } else {
                documentRepository.updateDocument(document)
                _uiState.update {
                    it.copy(isSaved = true, lastSavedAt = currentTime)
                }
            }
        }
    }

    fun showSaveDialog() {
        _uiState.update { it.copy(showSaveDialog = true) }
    }

    fun dismissSaveDialog() {
        _uiState.update { it.copy(showSaveDialog = false) }
    }

    fun showDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun deleteDocument(onDeleted: () -> Unit) {
        viewModelScope.launch {
            val documentId = _uiState.value.documentId
            if (documentId != null) {
                documentRepository.deleteDocument(documentId)
                onDeleted()
            }
        }
    }

    fun showTagDialog() {
        _uiState.update { it.copy(showTagDialog = true) }
    }

    fun dismissTagDialog() {
        _uiState.update { it.copy(showTagDialog = false) }
    }

    fun addTag(tag: String) {
        if (tag.isNotBlank() && tag !in _uiState.value.tags) {
            _uiState.update {
                it.copy(tags = it.tags + tag.trim(), isSaved = false)
            }
            scheduleAutoSave()
        }
    }

    fun removeTag(tag: String) {
        _uiState.update {
            it.copy(tags = it.tags - tag, isSaved = false)
        }
        scheduleAutoSave()
    }

    override fun onCleared() {
        super.onCleared()
        if (!_uiState.value.isSaved) {
            saveDocument()
        }
    }
}
