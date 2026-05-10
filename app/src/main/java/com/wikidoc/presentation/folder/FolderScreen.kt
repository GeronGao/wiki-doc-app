package com.wikidoc.presentation.folder

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wikidoc.domain.model.Document
import com.wikidoc.domain.model.Folder
import com.wikidoc.presentation.theme.DocumentIconColor
import com.wikidoc.presentation.theme.FavoriteColor
import com.wikidoc.presentation.theme.FolderIconColor
import com.wikidoc.presentation.theme.OnSurface
import com.wikidoc.presentation.theme.Primary
import com.wikidoc.presentation.theme.Surface
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

data class FolderDragState(
    val draggingType: FolderDragType = FolderDragType.None,
    val documentId: Long = -1,
    val folderId: Long = -1,
    val isDragging: Boolean = false,
    val position: Offset = Offset.Zero,
    val isOverPortal: Boolean = false
)

sealed class FolderDragType {
    data object None : FolderDragType()
    data object Document : FolderDragType()
    data object Folder : FolderDragType()
}

sealed class DeleteTarget {
    data class Document(val id: Long, val title: String) : DeleteTarget()
    data class Folder(val id: Long, val name: String) : DeleteTarget()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderScreen(
    folderId: Long?,
    onBack: () -> Unit,
    onDocumentClick: (Long) -> Unit,
    onFolderClick: (Long) -> Unit,
    viewModel: FolderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showCreateDocumentDialog by viewModel.showCreateDocumentDialog.collectAsState()
    val density = LocalDensity.current
    var showCreateFolderDialog by remember { mutableStateOf(folderId == 0L) }
    var showMenu by remember { mutableStateOf<Long?>(null) }

    var dragState by remember { mutableStateOf(FolderDragState()) }
    var subFolderPositions by remember { mutableStateOf(mapOf<Long, Pair<Offset, Offset>>()) }

    var selectedDocument by remember { mutableStateOf<Document?>(null) }
    var selectedFolder by remember { mutableStateOf<Folder?>(null) }
    var showDocumentDetail by remember { mutableStateOf(false) }
    var showFolderDetail by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<DeleteTarget?>(null) }

    if (showCreateDocumentDialog) {
        FolderCreateDocumentDialog(
            onDismiss = { viewModel.hideCreateDocumentDialog() },
            onConfirm = { title ->
                viewModel.createDocument(title)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (folderId == 0L) "新建文件夹" else (uiState.folder?.name ?: "文件夹"),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    if (folderId != 0L) {
                        IconButton(onClick = { showCreateFolderDialog = true }) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = "新建文件夹")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (folderId != 0L) {
                FloatingActionButton(
                    onClick = { viewModel.showCreateDocumentDialog() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "新建文档")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (folderId == 0L) {
                CreateFolderContent(
                    onCreateFolder = { name ->
                        viewModel.createFolder(name)
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.childFolders, key = { "folder_${it.id}" }) { folder ->
                        val isDraggingThis = dragState.folderId == folder.id && dragState.draggingType == FolderDragType.Folder
                        val isTargeted = dragState.isDragging &&
                                dragState.draggingType == FolderDragType.Document &&
                                subFolderPositions[folder.id]?.let { (topLeft, bottomRight) ->
                                    dragState.position.x >= topLeft.x &&
                                            dragState.position.x <= bottomRight.x &&
                                            dragState.position.y >= topLeft.y &&
                                            dragState.position.y <= bottomRight.y
                                } == true

                        FolderTreeItem(
                            folder = folder,
                            onClick = {
                                if (dragState.isDragging && isTargeted && dragState.draggingType == FolderDragType.Document) {
                                    viewModel.moveDocumentToFolder(dragState.documentId, folder.id)
                                    dragState = FolderDragState()
                                } else if (!dragState.isDragging) {
                                    onFolderClick(folder.id)
                                }
                            },
                            onMenuClick = { showMenu = folder.id },
                            isDragTarget = isTargeted,
                            isBeingDragged = isDraggingThis,
                            onDelete = {
                                selectedFolder = folder
                                deleteTarget = DeleteTarget.Folder(folder.id, folder.name)
                                showDeleteConfirm = true
                            },
                            onDetail = {
                                selectedFolder = folder
                                showFolderDetail = true
                            },
                            onDragStart = { offset ->
                                dragState = FolderDragState(draggingType = FolderDragType.Folder, folderId = folder.id, isDragging = true, position = offset)
                            },
                            onDrag = { offset ->
                                val isOverPortal = offset.x < with(density) { 80.dp.toPx() }
                                dragState = dragState.copy(position = offset, isOverPortal = isOverPortal)
                            },
                            onDragEnd = {
                                val targetedFolder = subFolderPositions.entries.find { (id, bounds) ->
                                    if (id == folder.id) return@find false
                                    val (topLeft, bottomRight) = bounds
                                    dragState.position.x >= topLeft.x &&
                                            dragState.position.x <= bottomRight.x &&
                                            dragState.position.y >= topLeft.y &&
                                            dragState.position.y <= bottomRight.y
                                }
                                when {
                                    dragState.isOverPortal && folderId != 0L -> {
                                        viewModel.moveSubFolderToParent(folder.id)
                                    }
                                    targetedFolder != null -> {
                                        viewModel.moveSubFolderToFolder(folder.id, targetedFolder.key)
                                    }
                                }
                                dragState = FolderDragState()
                            },
                            onDragCancel = {
                                dragState = FolderDragState()
                            },
                            onPositioned = { topLeft, bottomRight ->
                                subFolderPositions = subFolderPositions + (folder.id to (topLeft to bottomRight))
                            }
                        )
                    }

                    items(uiState.documents, key = { "doc_${it.id}" }) { document ->
                        val isDraggingThis = dragState.documentId == document.id && dragState.draggingType == FolderDragType.Document && dragState.isDragging
                        DocumentTreeItemFolder(
                            document = document,
                            isBeingDragged = isDraggingThis,
                            onClick = {
                                if (!dragState.isDragging) onDocumentClick(document.id)
                            },
                            onFavorite = {
                                viewModel.toggleFavorite(document)
                            },
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
                                dragState = FolderDragState(draggingType = FolderDragType.Document, documentId = document.id, isDragging = true, position = offset)
                            },
                            onDrag = { offset ->
                                val isOverPortal = offset.x < with(density) { 80.dp.toPx() }
                                dragState = dragState.copy(position = offset, isOverPortal = isOverPortal)
                            },
                            onDragEnd = {
                                val targetedFolder = subFolderPositions.entries.find { (_, bounds) ->
                                    val (topLeft, bottomRight) = bounds
                                    dragState.position.x >= topLeft.x &&
                                            dragState.position.x <= bottomRight.x &&
                                            dragState.position.y >= topLeft.y &&
                                            dragState.position.y <= bottomRight.y
                                }
                                when {
                                    dragState.isOverPortal && folderId != 0L -> {
                                        viewModel.moveDocumentToParentFolder(dragState.documentId)
                                    }
                                    targetedFolder != null -> {
                                        viewModel.moveDocumentToFolder(dragState.documentId, targetedFolder.key)
                                    }
                                }
                                dragState = FolderDragState()
                            },
                            onDragCancel = {
                                dragState = FolderDragState()
                            }
                        )
                    }

                    if (uiState.childFolders.isEmpty() && uiState.documents.isEmpty()) {
                        item {
                            EmptyFolderState()
                        }
                    }
                }
            }

            if (dragState.isDragging) {
                val isOverPortal = dragState.isOverPortal

                if (isOverPortal) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(80.dp)
                            .align(Alignment.CenterStart)
                    ) {
                        PortalEffect()
                    }
                }

                when (dragState.draggingType) {
                    FolderDragType.Document -> {
                        val draggingDoc = uiState.documents.find { it.id == dragState.documentId }
                        draggingDoc?.let { doc ->
                            Box(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            (dragState.position.x - 100.dp.toPx()).roundToInt(),
                                            (dragState.position.y - 30.dp.toPx()).roundToInt()
                                        )
                                    }
                                    .size(width = 200.dp, height = 60.dp)
                                    .shadow(if (isOverPortal) 20.dp else 12.dp, RoundedCornerShape(16.dp))
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isOverPortal) Primary.copy(alpha = 0.9f) else Surface
                                    ),
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
                                                .background(
                                                    if (isOverPortal) Color.White.copy(alpha = 0.3f)
                                                    else DocumentIconColor.copy(alpha = 0.1f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isOverPortal) Icons.Default.ExitToApp else Icons.Default.Article,
                                                contentDescription = null,
                                                tint = if (isOverPortal) Color.White else DocumentIconColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isOverPortal) "移出文件夹" else doc.title.ifBlank { "无标题" },
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isOverPortal) Color.White else OnSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                    FolderDragType.Folder -> {
                        val draggingFolder = uiState.childFolders.find { it.id == dragState.folderId }
                        draggingFolder?.let { folder ->
                            Box(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            (dragState.position.x - 100.dp.toPx()).roundToInt(),
                                            (dragState.position.y - 30.dp.toPx()).roundToInt()
                                        )
                                    }
                                    .size(width = 200.dp, height = 60.dp)
                                    .shadow(if (isOverPortal) 20.dp else 12.dp, RoundedCornerShape(16.dp))
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isOverPortal) Primary.copy(alpha = 0.9f) else Surface
                                    ),
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
                                                .background(
                                                    if (isOverPortal) Color.White.copy(alpha = 0.3f)
                                                    else FolderIconColor.copy(alpha = 0.15f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isOverPortal) Icons.Default.ExitToApp else Icons.Default.Folder,
                                                contentDescription = null,
                                                tint = if (isOverPortal) Color.White else FolderIconColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isOverPortal) "移出文件夹" else folder.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isOverPortal) Color.White else OnSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                    FolderDragType.None -> {}
                }
            }
        }
    }

    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = { showCreateFolderDialog = false },
            onConfirm = { name ->
                viewModel.createFolder(name)
                showCreateFolderDialog = false
            }
        )
    }

    if (showDocumentDetail && selectedDocument != null) {
        DocumentDetailSheet(
            document = selectedDocument!!,
            onDismiss = { showDocumentDetail = false },
            onFavorite = {
                selectedDocument = selectedDocument!!.copy(isFavorite = !selectedDocument!!.isFavorite)
                viewModel.toggleFavorite(selectedDocument!!)
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
        FolderDetailSheet(
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
        DeleteConfirmDialog(
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FolderTreeItem(
    folder: Folder,
    onClick: () -> Unit,
    onMenuClick: () -> Unit,
    isDragTarget: Boolean = false,
    isBeingDragged: Boolean = false,
    onDelete: () -> Unit = {},
    onDetail: () -> Unit = {},
    onDragStart: (Offset) -> Unit = {},
    onDrag: (Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {},
    onPositioned: (Offset, Offset) -> Unit = { _, _ -> }
) {
    var cardPosition by remember { mutableStateOf(Offset.Zero) }
    var showActions by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }
    val backgroundColor = if (isDragTarget) Primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface

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
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (!showActions && !isDragging) {
                        onClick()
                    }
                },
                onLongClick = {
                    if (!isDragging) {
                        showActions = true
                    }
                }
            )
            .pointerInput(folder.id) {
                val longPressTimeout = 1000L
                val moveThreshold = 30f

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val gestureStartTime = System.currentTimeMillis()
                    var dragged = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val changes = event.changes
                        if (changes.isEmpty()) break

                        val currentTime = System.currentTimeMillis()
                        val elapsed = currentTime - gestureStartTime
                        val currentPos = changes.first().position
                        val distance = (currentPos - down.position).getDistance()

                        if (elapsed >= longPressTimeout && distance > moveThreshold && !dragged && showActions) {
                            dragged = true
                            isDragging = true
                            showActions = false
                            changes.forEach { it.consume() }
                            val absolutePos = Offset(
                                cardPosition.x + down.position.x,
                                cardPosition.y + down.position.y
                            )
                            onDragStart(absolutePos)
                        }

                        if (dragged) {
                            changes.forEach { it.consume() }
                            val absoluteCurrentPos = Offset(
                                cardPosition.x + currentPos.x,
                                cardPosition.y + currentPos.y
                            )
                            onDrag(absoluteCurrentPos)
                        }

                        if (!changes.any { it.pressed }) {
                            if (dragged) {
                                onDragEnd()
                                isDragging = false
                            }
                            break
                        }
                    }
                }
            },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDragTarget) 4.dp else 0.dp),
        shape = RoundedCornerShape(12.dp),
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
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(FolderIconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = FolderIconColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${folder.documentCount} 篇文档",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (showActions) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = {
                        showActions = false
                        onDetail()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "详情",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = {
                        showActions = false
                        onDelete()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DocumentTreeItemFolder(
    document: Document,
    isBeingDragged: Boolean = false,
    onClick: () -> Unit,
    onFavorite: () -> Unit = {},
    onDelete: () -> Unit = {},
    onDetail: () -> Unit = {},
    onDragStart: (Offset) -> Unit = {},
    onDrag: (Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {}
) {
    var cardPosition by remember { mutableStateOf(Offset.Zero) }
    var showActions by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isBeingDragged) 0.3f else 1f)
            .onGloballyPositioned { coordinates ->
                cardPosition = coordinates.positionInRoot()
            }
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (!showActions && !isDragging) {
                        onClick()
                    }
                },
                onLongClick = {
                    if (!isDragging) {
                        showActions = true
                    }
                }
            )
            .pointerInput(document.id) {
                val longPressTimeout = 1000L
                val moveThreshold = 30f

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val gestureStartTime = System.currentTimeMillis()
                    val initialCardPos = cardPosition
                    var dragged = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val changes = event.changes
                        if (changes.isEmpty()) break

                        val currentTime = System.currentTimeMillis()
                        val elapsed = currentTime - gestureStartTime
                        val currentPos = changes.first().position
                        val distance = (currentPos - down.position).getDistance()

                        if (elapsed >= longPressTimeout && distance > moveThreshold && !dragged && showActions) {
                            dragged = true
                            isDragging = true
                            showActions = false
                            changes.forEach { it.consume() }
                            val absolutePos = Offset(
                                initialCardPos.x + down.position.x,
                                initialCardPos.y + down.position.y
                            )
                            onDragStart(absolutePos)
                        }

                        if (dragged) {
                            changes.forEach { it.consume() }
                            val absoluteCurrentPos = Offset(
                                cardPosition.x + currentPos.x,
                                cardPosition.y + currentPos.y
                            )
                            onDrag(absoluteCurrentPos)
                        }

                        if (!changes.any { it.pressed }) {
                            if (dragged) {
                                onDragEnd()
                                isDragging = false
                            }
                            break
                        }
                    }
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
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
                        .clip(RoundedCornerShape(8.dp))
                        .background(DocumentIconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Article,
                        contentDescription = null,
                        tint = DocumentIconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = document.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
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
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (document.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            document.tags.take(3).forEach { tag ->
                                FolderTagChip(tag = tag)
                            }
                        }
                    }
                }
            }

            if (document.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = document.content.take(100).replace("\n", " "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(document.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                if (showActions) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                showActions = false
                                onFavorite()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "收藏",
                                tint = if (document.isFavorite) FavoriteColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = {
                                showActions = false
                                onDetail()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "详情",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = {
                                showActions = false
                                onDelete()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FolderTagChip(tag: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = tag,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun CreateFolderContent(
    onCreateFolder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var folderName by remember { mutableStateOf("") }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CreateNewFolder,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = FolderIconColor
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "创建新文件夹",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = folderName,
            onValueChange = { folderName = it },
            label = { Text("文件夹名称") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onCreateFolder(folderName) },
            enabled = folderName.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("创建", fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建文件夹") },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("文件夹名称") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(folderName) },
                enabled = folderName.isNotBlank()
            ) {
                Text("创建")
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
fun FolderCreateDocumentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建文档") },
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
                Text("创建", color = MaterialTheme.colorScheme.primary)
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
fun EmptyFolderState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "文件夹为空",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击下方 + 按钮创建新文档",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailSheet(
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
                text = document.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            DetailRow(label = "创建时间", value = dateFormat.format(Date(document.createdAt)))
            Spacer(modifier = Modifier.height(12.dp))
            DetailRow(label = "最近修改", value = dateFormat.format(Date(document.updatedAt)))
            Spacer(modifier = Modifier.height(12.dp))
            DetailRow(label = "字符数", value = "${document.content.length}")
            Spacer(modifier = Modifier.height(12.dp))
            DetailRow(label = "收藏状态", value = if (document.isFavorite) "已收藏" else "未收藏")

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
fun FolderDetailSheet(
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

            DetailRow(label = "创建时间", value = dateFormat.format(Date(folder.createdAt)))
            Spacer(modifier = Modifier.height(12.dp))
            DetailRow(label = "文档数量", value = "${folder.documentCount}")

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
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DeleteConfirmDialog(
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
fun PortalEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "portal")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(80.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Primary.copy(alpha = alpha * 0.3f),
                        Primary.copy(alpha = alpha * 0.6f),
                        Primary.copy(alpha = alpha * 0.3f)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Primary.copy(alpha = alpha),
                        Primary.copy(alpha = alpha * 0.5f),
                        Primary.copy(alpha = alpha)
                    )
                ),
                shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                tint = Primary.copy(alpha = alpha),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "移出",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Primary.copy(alpha = alpha)
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60000 -> "刚刚"
        diff < 3600000 -> "${diff / 60000} 分钟前"
        diff < 86400000 -> "${diff / 3600000} 小时前"
        diff < 604800000 -> "${diff / 86400000} 天前"
        else -> SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date(timestamp))
    }
}
