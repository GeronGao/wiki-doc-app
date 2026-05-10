package com.wikidoc.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wikidoc.domain.model.Document
import com.wikidoc.domain.model.Folder
import com.wikidoc.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

data class DragState(
    val draggingType: DragType = DragType.None,
    val documentId: Long = -1,
    val folderId: Long = -1,
    val isDragging: Boolean = false,
    val position: Offset = Offset.Zero,
    val cardRootPosition: Offset = Offset.Zero
)

sealed class DragType {
    data object None : DragType()
    data object Document : DragType()
    data object Folder : DragType()
}

sealed class HomeItem {
    data class FolderItem(val folder: Folder) : HomeItem()
    data class DocumentItem(val document: Document) : HomeItem()
}

sealed class DeleteTarget {
    data class Document(val id: Long, val title: String) : DeleteTarget()
    data class Folder(val id: Long, val name: String) : DeleteTarget()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onDocumentClick: (Long) -> Unit,
    onFolderClick: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    onCreateFolder: () -> Unit,
    onCreateDocument: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showCreateDialog by viewModel.showCreateDocumentDialog.collectAsState()

    var dragState by remember { mutableStateOf(DragState()) }
    var folderPositions by remember { mutableStateOf(mapOf<Long, Pair<Offset, Offset>>()) }

    var selectedDocument by remember { mutableStateOf<Document?>(null) }
    var selectedFolder by remember { mutableStateOf<Folder?>(null) }
    var showDocumentDetail by remember { mutableStateOf(false) }
    var showFolderDetail by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<DeleteTarget?>(null) }

    fun isOverFolder(folderId: Long): Boolean {
        if (!dragState.isDragging) return false
        if (dragState.draggingType == DragType.Folder && dragState.folderId == folderId) return false
        val bounds = folderPositions[folderId] ?: return false
        val (topLeft, bottomRight) = bounds
        return dragState.position.x >= topLeft.x &&
                dragState.position.x <= bottomRight.x &&
                dragState.position.y >= topLeft.y &&
                dragState.position.y <= bottomRight.y
    }

    if (showCreateDialog) {
        CreateDocumentDialog(
            onDismiss = { viewModel.hideCreateDocumentDialog() },
            onConfirm = { title ->
                viewModel.createDocument(title)
            }
        )
    }

    Scaffold(
        topBar = {
            XiaohongshuTopBar(
                onSearchClick = onNavigateToSearch,
                onMenuClick = onCreateFolder
            )
        },
        floatingActionButton = {
            SmallFloatingActionButton(
                onClick = onCreateDocument,
                containerColor = Primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "新建文档")
            }
        },
        containerColor = Background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }
                } else if (uiState.recentDocuments.isEmpty() && uiState.folders.isEmpty()) {
                    item {
                        EmptyState(onCreateDocument = onCreateDocument)
                    }
                } else {
                    val mixedItems = buildList {
                        uiState.folders.forEach { folder ->
                            add(HomeItem.FolderItem(folder))
                        }
                        uiState.recentDocuments.forEach { document ->
                            add(HomeItem.DocumentItem(document))
                        }
                    }.sortedByDescending { item ->
                        when (item) {
                            is HomeItem.FolderItem -> item.folder.createdAt
                            is HomeItem.DocumentItem -> item.document.updatedAt
                        }
                    }

                    itemsIndexed(mixedItems, key = { index, item ->
                        when (item) {
                            is HomeItem.FolderItem -> "folder_${item.folder.id}"
                            is HomeItem.DocumentItem -> "doc_${item.document.id}"
                        }
                    }) { index, item ->
                        when (item) {
                            is HomeItem.FolderItem -> {
                                val folder = item.folder
                                val isDraggingThis = dragState.folderId == folder.id && dragState.draggingType == DragType.Folder
                                val isTargeted = isOverFolder(folder.id)

                                LaunchedEffect(isTargeted) {
                                    viewModel.setDragTargetedFolder(if (isTargeted) folder.id else null)
                                }

                                FolderCard(
                                    folder = folder,
                                    onClick = {
                                        if (dragState.isDragging && isTargeted && dragState.draggingType == DragType.Document) {
                                            viewModel.moveDocumentToFolder(dragState.documentId, folder.id)
                                            dragState = DragState()
                                        } else if (!dragState.isDragging) {
                                            onFolderClick(folder.id)
                                        }
                                    },
                                    isDragTarget = isTargeted,
                                    isBeingDragged = isDraggingThis,
                                    onLongPress = {
                                        selectedFolder = folder
                                        deleteTarget = DeleteTarget.Folder(folder.id, folder.name)
                                        showDeleteConfirm = true
                                    },
                                    onDetail = {
                                        selectedFolder = folder
                                        showFolderDetail = true
                                    },
                                    onDragStart = { offset ->
                                        dragState = DragState(draggingType = DragType.Folder, folderId = folder.id, isDragging = true, position = offset)
                                    },
                                    onDrag = { offset ->
                                        dragState = dragState.copy(position = offset)
                                    },
                                    onDragEnd = {
                                        val targetedFolderId = folderPositions.entries.find { (id, bounds) ->
                                            if (id == folder.id) return@find false
                                            val (topLeft, bottomRight) = bounds
                                            dragState.position.x >= topLeft.x &&
                                                    dragState.position.x <= bottomRight.x &&
                                                    dragState.position.y >= topLeft.y &&
                                                    dragState.position.y <= bottomRight.y
                                        }?.key
                                        if (targetedFolderId != null) {
                                            viewModel.moveFolderToFolder(folder.id, targetedFolderId)
                                        }
                                        dragState = DragState()
                                        viewModel.setDragTargetedFolder(null)
                                    },
                                    onDragCancel = {
                                        dragState = DragState()
                                        viewModel.setDragTargetedFolder(null)
                                    },
                                    onPositioned = { topLeft, bottomRight ->
                                        folderPositions = folderPositions + (folder.id to (topLeft to bottomRight))
                                    }
                                )
                            }
                            is HomeItem.DocumentItem -> {
                                val document = item.document
                                val isDraggingThis = dragState.documentId == document.id && dragState.draggingType == DragType.Document && dragState.isDragging
                                DocumentCard(
                                    document = document,
                                    isBeingDragged = isDraggingThis,
                                    onClick = { if (!dragState.isDragging) onDocumentClick(document.id) },
                                    onFavorite = { viewModel.toggleFavorite(document) },
                                    onDelete = {
                                        selectedDocument = document
                                        deleteTarget = DeleteTarget.Document(document.id, document.title)
                                        showDeleteConfirm = true
                                    },
                                    onDetail = {
                                        selectedDocument = document
                                        showDocumentDetail = true
                                    },
                                    onDragStart = { offset ->
                                        dragState = DragState(draggingType = DragType.Document, documentId = document.id, isDragging = true, position = offset)
                                    },
                                    onDrag = { offset ->
                                        dragState = dragState.copy(position = offset)
                                    },
                                    onDragEnd = {
                                        val targetedFolderId = folderPositions.entries.find { (id, bounds) ->
                                            val (topLeft, bottomRight) = bounds
                                            dragState.position.x >= topLeft.x &&
                                                    dragState.position.x <= bottomRight.x &&
                                                    dragState.position.y >= topLeft.y &&
                                                    dragState.position.y <= bottomRight.y
                                        }?.key
                                        if (targetedFolderId != null) {
                                            viewModel.moveDocumentToFolder(dragState.documentId, targetedFolderId)
                                        }
                                        dragState = DragState()
                                        viewModel.setDragTargetedFolder(null)
                                    },
                                    onDragCancel = {
                                        dragState = DragState()
                                        viewModel.setDragTargetedFolder(null)
                                    },
                                    onPositioned = { }
                                )
                            }
                        }
                    }
                }
            }

            if (dragState.isDragging) {
                when (dragState.draggingType) {
                    DragType.Document -> {
                        val draggingDoc = uiState.recentDocuments.find { it.id == dragState.documentId }
                        draggingDoc?.let { doc ->
                            Box(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            (dragState.position.x - 100.dp.toPx()).roundToInt(),
                                            (dragState.position.y).roundToInt()
                                        )
                                    }
                                    .size(width = 200.dp, height = 80.dp)
                                    .shadow(12.dp, RoundedCornerShape(16.dp))
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Surface),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(DocumentIconColor.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = doc.title.take(1).uppercase(),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DocumentIconColor
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = doc.title.ifBlank { "无标题" },
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = OnSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                    DragType.Folder -> {
                        val draggingFolder = uiState.folders.find { it.id == dragState.folderId }
                        draggingFolder?.let { folder ->
                            Box(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            (dragState.position.x - 100.dp.toPx()).roundToInt(),
                                            (dragState.position.y).roundToInt()
                                        )
                                    }
                                    .size(width = 200.dp, height = 80.dp)
                                    .shadow(12.dp, RoundedCornerShape(16.dp))
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Surface),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(FolderIconColor.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Folder,
                                                contentDescription = null,
                                                tint = FolderIconColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = folder.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = OnSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                    DragType.None -> {}
                }
            }
        }

        if (showDocumentDetail && selectedDocument != null) {
            HomeDocumentDetailSheet(
                document = selectedDocument!!,
                onDismiss = { showDocumentDetail = false },
                onFavorite = {
                    viewModel.toggleFavorite(selectedDocument!!)
                    val updated = uiState.recentDocuments.find { it.id == selectedDocument!!.id }
                        ?: uiState.favoriteDocuments.find { it.id == selectedDocument!!.id }
                    if (updated != null) {
                        selectedDocument = updated
                    }
                },
                onDelete = {
                    showDocumentDetail = false
                    deleteTarget = DeleteTarget.Document(selectedDocument!!.id, selectedDocument!!.title)
                    showDeleteConfirm = true
                },
                onClose = { showDocumentDetail = false }
            )
        }

        if (showFolderDetail && selectedFolder != null) {
            HomeFolderDetailSheet(
                folder = selectedFolder!!,
                onDismiss = { showFolderDetail = false },
                onDelete = {
                    showFolderDetail = false
                    deleteTarget = DeleteTarget.Folder(selectedFolder!!.id, selectedFolder!!.name)
                    showDeleteConfirm = true
                },
                onClose = { showFolderDetail = false }
            )
        }

        if (showDeleteConfirm && deleteTarget != null) {
            HomeDeleteConfirmDialog(
                target = deleteTarget!!,
                onDismiss = {
                    showDeleteConfirm = false
                    deleteTarget = null
                },
                onConfirm = {
                    when (val target = deleteTarget!!) {
                        is DeleteTarget.Document -> viewModel.deleteDocument(target.id)
                        is DeleteTarget.Folder -> viewModel.deleteFolder(target.id)
                    }
                    showDeleteConfirm = false
                    deleteTarget = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XiaohongshuTopBar(
    onSearchClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "WikiDoc",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "搜索",
                    tint = OnSurfaceVariant
                )
            }
            IconButton(onClick = onMenuClick) {
                Icon(
                    Icons.Default.CreateNewFolder,
                    contentDescription = "新建文件夹",
                    tint = OnSurfaceVariant
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Background
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderCard(
    folder: Folder,
    onClick: () -> Unit,
    isDragTarget: Boolean = false,
    isBeingDragged: Boolean = false,
    onLongPress: () -> Unit = {},
    onDetail: () -> Unit = {},
    onDragStart: (Offset) -> Unit = {},
    onDrag: (Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {},
    onPositioned: (Offset, Offset) -> Unit = { _, _ -> }
) {
    var cardPosition by remember { mutableStateOf(Offset.Zero) }
    var showActions by remember { mutableStateOf(false) }
    val backgroundColor = if (isDragTarget) Primary.copy(alpha = 0.1f) else Surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isBeingDragged) 0.3f else 1f)
            .onGloballyPositioned { coordinates ->
                cardPosition = coordinates.positionInRoot()
                val size = coordinates.size
                onPositioned(
                    cardPosition,
                    Offset(cardPosition.x + size.width, cardPosition.y + size.height)
                )
            }
            .pointerInput(folder.id) {
                val longPressTimeout = 1000L
                val moveThreshold = 30f
                var hasMoved = false
                var longPressTriggered = false
                var dragStarted = false

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    hasMoved = false
                    longPressTriggered = false
                    dragStarted = false
                    showActions = false

                    val gestureStartTime = System.currentTimeMillis()

                    while (true) {
                        val event = awaitPointerEvent()
                        val changes = event.changes

                        if (changes.isEmpty()) break

                        val currentTime = System.currentTimeMillis()
                        val elapsed = currentTime - gestureStartTime
                        val firstChange = changes.first()
                        val currentPos = firstChange.position
                        val distance = (currentPos - down.position).getDistance()

                        if (distance > moveThreshold) {
                            hasMoved = true
                        }

                        if (elapsed >= longPressTimeout && !longPressTriggered) {
                            longPressTriggered = true
                            showActions = true
                            onLongPress()
                        }

                        if (longPressTriggered && distance > moveThreshold && !dragStarted) {
                            dragStarted = true
                            showActions = false
                            val absolutePos = Offset(
                                cardPosition.x + down.position.x,
                                cardPosition.y + down.position.y
                            )
                            onDragStart(absolutePos)
                        }

                        if (dragStarted) {
                            changes.forEach { it.consume() }
                            val absoluteCurrentPos = Offset(
                                cardPosition.x + currentPos.x,
                                cardPosition.y + currentPos.y
                            )
                            onDrag(absoluteCurrentPos)
                        }

                        if (!changes.any { it.pressed }) {
                            if (dragStarted) {
                                onDragEnd()
                            }
                            showActions = false
                            break
                        }
                    }
                }
            }
            .padding(if (isDragTarget) 4.dp else 0.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragTarget) 4.dp else 1.dp
        ),
        shape = RoundedCornerShape(16.dp),
        border = if (isDragTarget) androidx.compose.foundation.BorderStroke(2.dp, Primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FolderIconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = FolderIconColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${folder.documentCount} 篇文档",
                    fontSize = 13.sp,
                    color = OnSurfaceVariant
                )
            }

            if (showActions) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = {
                            showActions = false
                            onDetail()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "详情",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            showActions = false
                            onLongPress()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = OnSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DocumentCard(
    document: Document,
    isBeingDragged: Boolean = false,
    onClick: () -> Unit = {},
    onFavorite: () -> Unit = {},
    onDelete: () -> Unit = {},
    onDetail: () -> Unit = {},
    onDragStart: (Offset) -> Unit = {},
    onDrag: (Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {},
    onPositioned: (Offset) -> Unit = {}
) {
    var cardPosition by remember { mutableStateOf(Offset.Zero) }
    var showActions by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isBeingDragged) 0.3f else 1f)
            .onGloballyPositioned { coordinates ->
                cardPosition = coordinates.positionInRoot()
                onPositioned(cardPosition)
            }
            .pointerInput(document.id) {
                val longPressTimeout = 1000L
                val moveThreshold = 30f
                var hasMoved = false
                var longPressTriggered = false
                var dragStarted = false

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    hasMoved = false
                    longPressTriggered = false
                    dragStarted = false
                    showActions = false

                    val gestureStartTime = System.currentTimeMillis()
                    val initialCardPos = cardPosition

                    while (true) {
                        val event = awaitPointerEvent()
                        val changes = event.changes

                        if (changes.isEmpty()) break

                        val currentTime = System.currentTimeMillis()
                        val elapsed = currentTime - gestureStartTime
                        val firstChange = changes.first()
                        val currentPos = firstChange.position
                        val distance = (currentPos - down.position).getDistance()

                        if (distance > moveThreshold) {
                            hasMoved = true
                        }

                        if (elapsed >= longPressTimeout && !longPressTriggered) {
                            longPressTriggered = true
                            showActions = true
                            onFavorite()
                        }

                        if (longPressTriggered && distance > moveThreshold && !dragStarted) {
                            dragStarted = true
                            showActions = false
                            val absolutePos = Offset(
                                initialCardPos.x + down.position.x,
                                initialCardPos.y + down.position.y
                            )
                            onDragStart(absolutePos)
                        }

                        if (dragStarted) {
                            changes.forEach { it.consume() }
                            val absoluteCurrentPos = Offset(
                                cardPosition.x + currentPos.x,
                                cardPosition.y + currentPos.y
                            )
                            onDrag(absoluteCurrentPos)
                        }

                        if (!changes.any { it.pressed }) {
                            if (dragStarted) {
                                onDragEnd()
                            }
                            showActions = false
                            break
                        }
                    }
                }
            },
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DocumentIconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = document.title.take(1).uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DocumentIconColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = document.title.ifBlank { "无标题" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (document.isFavorite) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "收藏",
                                tint = FavoriteColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            if (document.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = document.content.take(100).replace("\n", " "),
                    fontSize = 13.sp,
                    color = OnSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
            }

            if (document.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    document.tags.take(3).forEach { tag ->
                        TagChip(tag = tag)
                    }
                    if (document.tags.size > 3) {
                        Text(
                            text = "+${document.tags.size - 3}",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatSmartDate(document.updatedAt),
                    fontSize = 12.sp,
                    color = OnSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (document.wordCount > 0) {
                        Text(
                            text = "${document.wordCount} 字",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }

                    if (showActions) {
                        IconButton(
                            onClick = {
                                showActions = false
                                onFavorite()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (document.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "收藏",
                                tint = if (document.isFavorite) FavoriteColor else OnSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                showActions = false
                                onDetail()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "详情",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                showActions = false
                                onDelete()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TagChip(tag: String) {
    Surface(
        color = TagBackgroundColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = tag,
            fontSize = 11.sp,
            color = TagTextColor,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDocumentDetailSheet(
    document: Document,
    onDismiss: () -> Unit,
    onFavorite: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "文档详情",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "关闭")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = document.title.ifBlank { "无标题" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            HomeDetailRow(label = "创建时间", value = dateFormat.format(Date(document.createdAt)))
            Spacer(modifier = Modifier.height(12.dp))
            HomeDetailRow(label = "最近修改", value = dateFormat.format(Date(document.updatedAt)))
            Spacer(modifier = Modifier.height(12.dp))
            HomeDetailRow(label = "字符数", value = "${document.content.length}")
            Spacer(modifier = Modifier.height(12.dp))
            HomeDetailRow(label = "收藏状态", value = if (document.isFavorite) "已收藏" else "未收藏")

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onFavorite,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (document.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (document.isFavorite) "取消收藏" else "收藏")
                }

                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("删除")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeFolderDetailSheet(
    folder: Folder,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "文件夹详情",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "关闭")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = folder.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            HomeDetailRow(label = "创建时间", value = dateFormat.format(Date(folder.createdAt)))
            Spacer(modifier = Modifier.height(12.dp))
            HomeDetailRow(label = "文档数量", value = "${folder.documentCount}")

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("删除文件夹")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun HomeDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun HomeDeleteConfirmDialog(
    target: DeleteTarget,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val title = when (target) {
        is DeleteTarget.Document -> "确定要删除文档「${target.title}」吗？"
        is DeleteTarget.Folder -> "确定要删除文件夹「${target.name}」吗？"
    }
    val subtitle = when (target) {
        is DeleteTarget.Document -> "删除后可以在回收站恢复"
        is DeleteTarget.Folder -> "文件夹内的内容也会被删除"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认删除") },
        text = {
            Column {
                Text(title)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun EmptyState(onCreateDocument: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = OnSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "还没有文档",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = OnSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "开始创建你的第一篇文档吧",
            fontSize = 14.sp,
            color = OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onCreateDocument,
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(24.dp),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("新建文档", fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun CreateDocumentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "新建文档",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("文档标题") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title.ifBlank { "无标题" }) }
            ) {
                Text("创建", color = Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = OnSurfaceVariant)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

private fun formatSmartDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val calendar = Calendar.getInstance()
    val today = calendar.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0) }.timeInMillis

    return when {
        timestamp >= today -> "今天"
        timestamp >= today - 86400000 -> "昨天"
        diff < 604800000 -> "${diff / 86400000}天前"
        else -> SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date(timestamp))
    }
}
