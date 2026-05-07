package com.wikidoc.presentation.editor

import androidx.lifecycle.SavedStateHandle
import com.wikidoc.domain.model.Document
import com.wikidoc.domain.repository.DocumentRepository
import com.wikidoc.presentation.component.EditorMode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockRepository: DocumentRepository
    private lateinit var mockSavedStateHandle: SavedStateHandle

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk(relaxed = true)
        mockSavedStateHandle = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(documentId: Long? = null): EditorViewModel {
        every { mockSavedStateHandle.get<Long>("documentId") } returns documentId ?: -1L
        return EditorViewModel(mockRepository, mockSavedStateHandle)
    }

    @Test
    fun `initial state for new document`() = runTest {
        coEvery { mockRepository.getDocumentById(any()) } returns null

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceTimeBy(1000)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isNewDocument)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.documentId)
    }

    @Test
    fun `load existing document`() = runTest {
        val docId = 1L
        val document = Document(
            id = docId,
            title = "Test Document",
            content = "Test content",
            folderId = null,
            tags = listOf("tag1", "tag2"),
            isFavorite = true,
            wordCount = 5,
            updatedAt = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis()
        )
        coEvery { mockRepository.getDocumentById(docId) } returns document

        val viewModel = createViewModel(docId)
        testDispatcher.scheduler.advanceTimeBy(1000)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isNewDocument)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(docId, viewModel.uiState.value.documentId)
        assertEquals("Test Document", viewModel.uiState.value.title)
        assertEquals("Test content", viewModel.uiState.value.content)
        assertEquals(listOf("tag1", "tag2"), viewModel.uiState.value.tags)
        assertTrue(viewModel.uiState.value.isFavorite)
    }

    @Test
    fun `title change updates state`() = runTest {
        coEvery { mockRepository.getDocumentById(any()) } returns null
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceTimeBy(1000)
        advanceUntilIdle()

        viewModel.onTitleChange("New Title")

        assertEquals("New Title", viewModel.uiState.value.title)
        assertFalse(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `content change updates state and word count`() = runTest {
        coEvery { mockRepository.getDocumentById(any()) } returns null
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceTimeBy(1000)
        advanceUntilIdle()

        viewModel.onContentChange("This is test content")

        assertEquals("This is test content", viewModel.uiState.value.content)
        assertEquals(4, viewModel.uiState.value.wordCount)
        assertFalse(viewModel.uiState.value.isSaved)
    }

    @Test
    fun `word count handles multiple spaces`() = runTest {
        coEvery { mockRepository.getDocumentById(any()) } returns null
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceTimeBy(1000)
        advanceUntilIdle()

        viewModel.onContentChange("hello   world  test")

        assertEquals(3, viewModel.uiState.value.wordCount)
    }

    @Test
    fun `word count handles empty content`() = runTest {
        coEvery { mockRepository.getDocumentById(any()) } returns null
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceTimeBy(1000)
        advanceUntilIdle()

        viewModel.onContentChange("")

        assertEquals(0, viewModel.uiState.value.wordCount)
    }

    @Test
    fun `set editor mode`() = runTest {
        coEvery { mockRepository.getDocumentById(any()) } returns null
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceTimeBy(1000)
        advanceUntilIdle()

        viewModel.setEditorMode(EditorMode.PREVIEW)
        assertEquals(EditorMode.PREVIEW, viewModel.uiState.value.editorMode)

        viewModel.setEditorMode(EditorMode.EDIT)
        assertEquals(EditorMode.EDIT, viewModel.uiState.value.editorMode)

        viewModel.setEditorMode(EditorMode.SPLIT)
        assertEquals(EditorMode.SPLIT, viewModel.uiState.value.editorMode)
    }

    @Test
    fun `toggle favorite`() = runTest {
        coEvery { mockRepository.getDocumentById(any()) } returns null
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceTimeBy(1000)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isFavorite)

        viewModel.toggleFavorite()
        assertTrue(viewModel.uiState.value.isFavorite)

        viewModel.toggleFavorite()
        assertFalse(viewModel.uiState.value.isFavorite)
    }

    @Test
    fun `add tag`() = runTest {
        coEvery { mockRepository.getDocumentById(any()) } returns null
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceTimeBy(1000)
        advanceUntilIdle()

        viewModel.addTag("newTag")

        assertTrue(viewModel.uiState.value.tags.contains("newTag"))
    }

    @Test
    fun `add duplicate tag ignored`() = runTest {
        coEvery { mockRepository.getDocumentById(any()) } returns null
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceTimeBy(1000)
        advanceUntilIdle()

        viewModel.addTag("tag1")
        viewModel.addTag("tag1")

        assertEquals(1, viewModel.uiState.value.tags.count { it == "tag1" })
    }

    @Test
    fun `add blank tag ignored`() = runTest {
        coEvery { mockRepository.getDocumentById(any()) } returns null
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceTimeBy(1000)
        advanceUntilIdle()

        viewModel.addTag("   ")

        assertTrue(viewModel.uiState.value.tags.isEmpty())
    }

    @Test
    fun `remove tag`() = runTest {
        coEvery { mockRepository.getDocumentById(any()) } returns null
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceTimeBy(1000)
        advanceUntilIdle()

        viewModel.addTag("tag1")
        viewModel.removeTag("tag1")

        assertFalse(viewModel.uiState.value.tags.contains("tag1"))
    }

    @Test
    fun `show save dialog`() = runTest {
        coEvery { mockRepository.getDocumentById(any()) } returns null
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceTimeBy(1000)
        advanceUntilIdle()

        viewModel.showSaveDialog()
        assertTrue(viewModel.uiState.value.showSaveDialog)

        viewModel.dismissSaveDialog()
        assertFalse(viewModel.uiState.value.showSaveDialog)
    }

    @Test
    fun `show delete dialog`() = runTest {
        coEvery { mockRepository.getDocumentById(any()) } returns null
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceTimeBy(1000)
        advanceUntilIdle()

        viewModel.showDeleteDialog()
        assertTrue(viewModel.uiState.value.showDeleteDialog)

        viewModel.dismissDeleteDialog()
        assertFalse(viewModel.uiState.value.showDeleteDialog)
    }

    @Test
    fun `show tag dialog`() = runTest {
        coEvery { mockRepository.getDocumentById(any()) } returns null
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceTimeBy(1000)
        advanceUntilIdle()

        viewModel.showTagDialog()
        assertTrue(viewModel.uiState.value.showTagDialog)

        viewModel.dismissTagDialog()
        assertFalse(viewModel.uiState.value.showTagDialog)
    }

    @Test
    fun `save new document updates documentId`() = runTest {
        coEvery { mockRepository.getDocumentById(any()) } returns null
        coEvery { mockRepository.saveDocument(any()) } returns 123L
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceTimeBy(1000)
        advanceUntilIdle()

        viewModel.onTitleChange("New Document Title")
        viewModel.saveDocument()
        advanceUntilIdle()

        assertEquals(123L, viewModel.uiState.value.documentId)
        assertFalse(viewModel.uiState.value.isNewDocument)
        assertTrue(viewModel.uiState.value.isSaved)
        coVerify { mockRepository.saveDocument(any()) }
    }

    @Test
    fun `blank content not saved`() = runTest {
        coEvery { mockRepository.getDocumentById(any()) } returns null
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceTimeBy(1000)
        advanceUntilIdle()

        viewModel.saveDocument()
        advanceUntilIdle()

        coVerify(exactly = 0) { mockRepository.saveDocument(any()) }
        coVerify(exactly = 0) { mockRepository.updateDocument(any()) }
    }

    @Test
    fun `no title document uses default title when saving`() = runTest {
        coEvery { mockRepository.getDocumentById(any()) } returns null
        coEvery { mockRepository.saveDocument(any()) } returns 1L
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceTimeBy(1000)
        advanceUntilIdle()

        viewModel.onContentChange("Content without title")
        viewModel.saveDocument()
        advanceUntilIdle()

        coVerify { mockRepository.saveDocument(match { it.title == "无标题" }) }
    }
}
