package com.wikidoc.presentation.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wikidoc.presentation.component.EditorMode
import com.wikidoc.presentation.component.MarkdownEditor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(
    documentId: Long?,
    onBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showMoreMenu by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved) {
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        if (!uiState.isSaved) {
                            viewModel.showSaveDialog()
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                title = {
                    Text(
                        text = if (uiState.isNewDocument) "新建文档" else uiState.title.ifBlank { "无标题" },
                        maxLines = 1
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = "收藏",
                            tint = if (uiState.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { viewModel.showTagDialog() }) {
                        Icon(Icons.Filled.Tag, contentDescription = "标签")
                    }

                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "更多")
                        }

                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("删除文档") },
                                onClick = {
                                    showMoreMenu = false
                                    viewModel.showDeleteDialog()
                                },
                                leadingIcon = {
                                    Icon(Icons.Filled.Delete, contentDescription = null)
                                }
                            )
                        }
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { padding ->
        MarkdownEditor(
            title = uiState.title,
            content = uiState.content,
            editorMode = when (uiState.editorMode) {
                EditorMode.EDIT -> com.wikidoc.presentation.component.EditorMode.EDIT
                EditorMode.PREVIEW -> com.wikidoc.presentation.component.EditorMode.PREVIEW
                EditorMode.SPLIT -> com.wikidoc.presentation.component.EditorMode.SPLIT
            },
            onTitleChange = { viewModel.onTitleChange(it) },
            onContentChange = { viewModel.onContentChange(it) },
            onModeChange = { mode ->
                val editorMode = when (mode) {
                    com.wikidoc.presentation.component.EditorMode.EDIT -> EditorMode.EDIT
                    com.wikidoc.presentation.component.EditorMode.PREVIEW -> EditorMode.PREVIEW
                    com.wikidoc.presentation.component.EditorMode.SPLIT -> EditorMode.SPLIT
                }
                viewModel.setEditorMode(editorMode)
            },
            onBack = {
                if (!uiState.isSaved) {
                    viewModel.showSaveDialog()
                } else {
                    onBack()
                }
            },
            onSave = { viewModel.saveDocument() },
            isSaved = uiState.isSaved,
            modifier = Modifier.padding(padding)
        )
    }

    if (uiState.showSaveDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSaveDialog() },
            title = { Text("保存更改") },
            text = { Text("是否保存当前文档？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.saveDocument()
                        viewModel.dismissSaveDialog()
                        onBack()
                    }
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissSaveDialog()
                        onBack()
                    }
                ) {
                    Text("不保存")
                }
            }
        )
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = { Text("删除文档") },
            text = { Text("确定要删除这个文档吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDocument { onBack() }
                        viewModel.dismissDeleteDialog()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("取消")
                }
            }
        )
    }

    if (uiState.showTagDialog) {
        TagDialog(
            tags = uiState.tags,
            onDismiss = { viewModel.dismissTagDialog() },
            onAddTag = { viewModel.addTag(it) },
            onRemoveTag = { viewModel.removeTag(it) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TagDialog(
    tags: List<String>,
    onDismiss: () -> Unit,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit
) {
    var newTag by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("文档标签") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTag,
                        onValueChange = { newTag = it },
                        label = { Text("添加标签") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newTag.isNotBlank()) {
                                onAddTag(newTag)
                                newTag = ""
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "添加")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (tags.isEmpty()) {
                    Text(
                        text = "暂无标签",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tags.forEach { tag ->
                            InputChip(
                                selected = false,
                                onClick = { },
                                label = { Text(tag) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { onRemoveTag(tag) },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "删除",
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完成")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = { content() }
    )
}
