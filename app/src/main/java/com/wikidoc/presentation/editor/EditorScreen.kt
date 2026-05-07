package com.wikidoc.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    documentId: Long?,
    onBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved && !uiState.isNewDocument && documentId != null) {
        }
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                title = {
                    Text(
                        text = if (uiState.isNewDocument) "新建文档" else uiState.title.ifBlank { "无标题" },
                        maxLines = 1
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.togglePreview() }) {
                        Icon(
                            imageVector = if (uiState.isPreviewMode) Icons.Default.Edit else Icons.Default.Visibility,
                            contentDescription = "预览"
                        )
                    }
                    IconButton(onClick = { viewModel.saveDocument() }) {
                        Icon(Icons.Default.Save, contentDescription = "保存")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            EditorToolbar()

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.isPreviewMode) {
                MarkdownPreview(
                    content = uiState.content,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    BasicTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.onTitleChange(it) },
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            Box {
                                if (uiState.title.isEmpty()) {
                                    Text(
                                        text = "标题",
                                        style = TextStyle(
                                            fontSize = 24.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    BasicTextField(
                        value = uiState.content,
                        onValueChange = { viewModel.onContentChange(it) },
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                if (uiState.content.isEmpty()) {
                                    Text(
                                        text = "开始写作...",
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
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
    }
}

@Composable
fun EditorToolbar() {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .horizontalScroll(scrollState)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ToolbarButton(icon = Icons.Default.FormatBold, contentDescription = "加粗")
        ToolbarButton(icon = Icons.Default.FormatItalic, contentDescription = "斜体")
        ToolbarButton(icon = Icons.Default.FormatUnderlined, contentDescription = "下划线")
        ToolbarButton(icon = Icons.Default.StrikethroughS, contentDescription = "删除线")
        Spacer(modifier = Modifier.width(8.dp))
        ToolbarButton(icon = Icons.Default.Title, contentDescription = "标题")
        Spacer(modifier = Modifier.width(8.dp))
        ToolbarButton(icon = Icons.Default.FormatListBulleted, contentDescription = "列表")
        ToolbarButton(icon = Icons.Default.FormatListNumbered, contentDescription = "编号列表")
        ToolbarButton(icon = Icons.Default.CheckBoxOutlineBlank, contentDescription = "任务列表")
        Spacer(modifier = Modifier.width(8.dp))
        ToolbarButton(icon = Icons.Default.Code, contentDescription = "代码")
        ToolbarButton(icon = Icons.Default.Image, contentDescription = "图片")
        ToolbarButton(icon = Icons.Default.TableChart, contentDescription = "表格")
        Spacer(modifier = Modifier.width(8.dp))
        ToolbarButton(icon = Icons.Default.Link, contentDescription = "链接")
        ToolbarButton(icon = Icons.Default.FormatQuote, contentDescription = "引用")
    }
}

@Composable
fun ToolbarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit = {}
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MarkdownPreview(
    content: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = content.ifBlank { "预览内容为空" },
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
