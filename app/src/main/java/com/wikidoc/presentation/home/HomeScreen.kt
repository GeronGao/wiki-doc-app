package com.wikidoc.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wikidoc.domain.model.Document
import com.wikidoc.domain.model.Folder
import com.wikidoc.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (uiState.recentDocuments.isEmpty() && uiState.folders.isEmpty()) {
            EmptyState(onCreateDocument = onCreateDocument)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.folders.isNotEmpty()) {
                    item {
                        Text(
                            text = "文件夹",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    itemsIndexed(uiState.folders, key = { _, folder -> "folder_${folder.id}" }) { index, folder ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(300)) + slideInVertically(
                                animationSpec = spring(stiffness = Spring.StiffnessLow)
                            ) { it / 2 }
                        ) {
                            FolderCard(
                                folder = folder,
                                onClick = { onFolderClick(folder.id) }
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "最近文档",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnBackground,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                itemsIndexed(uiState.recentDocuments, key = { _, doc -> "doc_${doc.id}" }) { index, document ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(300, delayMillis = index * 50)) + slideInVertically(
                            animationSpec = spring(stiffness = Spring.StiffnessLow),
                            initialOffsetY = { it / 2 }
                        )
                    ) {
                        DocumentCard(
                            document = document,
                            onClick = { onDocumentClick(document.id) },
                            onLongClick = { viewModel.toggleFavorite(document) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
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

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = OnSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DocumentCard(
    document: Document,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
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
                if (document.wordCount > 0) {
                    Text(
                        text = "${document.wordCount} 字",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )
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
