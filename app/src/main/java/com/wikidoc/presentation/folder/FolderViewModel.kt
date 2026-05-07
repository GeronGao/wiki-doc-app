package com.wikidoc.presentation.folder

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
}
