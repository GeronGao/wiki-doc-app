package com.wikidoc.presentation.component

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.wikidoc.core.util.MarkdownSyntax
import com.wikidoc.core.util.MarkdownUtils

enum class EditorMode {
    EDIT,
    PREVIEW,
    SPLIT
}

@Composable
fun MarkdownEditor(
    title: String,
    content: String,
    editorMode: EditorMode,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onModeChange: (EditorMode) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    isSaved: Boolean,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember(content) {
        mutableStateOf(TextFieldValue(content, TextRange(content.length)))
    }

    val previewHtml = remember(content) {
        mutableStateOf(MarkdownUtils.parseToHtml(content))
    }

    LaunchedEffect(textFieldValue.text) {
        previewHtml.value = MarkdownUtils.parseToHtml(textFieldValue.text)
        if (textFieldValue.text != content) {
            onContentChange(textFieldValue.text)
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        if (editorMode != EditorMode.PREVIEW) {
            EditorToolbarWithMode(
                editorMode = editorMode,
                textFieldValue = textFieldValue,
                onModeChange = onModeChange,
                onTextChange = { textFieldValue = it }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (editorMode != EditorMode.PREVIEW) {
                TitleAndContentEditor(
                    title = title,
                    textFieldValue = textFieldValue,
                    onTitleChange = onTitleChange,
                    onTextChange = { textFieldValue = it },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }

            if (editorMode != EditorMode.EDIT) {
                MarkdownWebPreview(
                    html = previewHtml.value,
                    editorMode = editorMode,
                    onModeChange = onModeChange,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
fun EditorToolbarWithMode(
    editorMode: EditorMode,
    textFieldValue: TextFieldValue,
    onModeChange: (EditorMode) -> Unit,
    onTextChange: (TextFieldValue) -> Unit
) {
    var showModeMenu by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            ToolbarButton(
                icon = Icons.Default.FormatBold,
                contentDescription = "加粗",
                onClick = {
                    val (newText, cursorPos) = MarkdownUtils.insertMarkdownSyntax(
                        textFieldValue.text,
                        textFieldValue.selection.start,
                        textFieldValue.selection.end,
                        MarkdownSyntax.BOLD
                    )
                    onTextChange(TextFieldValue(newText, TextRange(cursorPos)))
                }
            )

            ToolbarButton(
                icon = Icons.Default.FormatItalic,
                contentDescription = "斜体",
                onClick = {
                    val (newText, cursorPos) = MarkdownUtils.insertMarkdownSyntax(
                        textFieldValue.text,
                        textFieldValue.selection.start,
                        textFieldValue.selection.end,
                        MarkdownSyntax.ITALIC
                    )
                    onTextChange(TextFieldValue(newText, TextRange(cursorPos)))
                }
            )

            ToolbarButton(
                icon = Icons.Default.StrikethroughS,
                contentDescription = "删除线",
                onClick = {
                    val (newText, cursorPos) = MarkdownUtils.insertMarkdownSyntax(
                        textFieldValue.text,
                        textFieldValue.selection.start,
                        textFieldValue.selection.end,
                        MarkdownSyntax.STRIKETHROUGH
                    )
                    onTextChange(TextFieldValue(newText, TextRange(cursorPos)))
                }
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .padding(horizontal = 4.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )

            ToolbarButton(
                icon = Icons.Default.Title,
                contentDescription = "标题",
                onClick = {
                    val (newText, cursorPos) = MarkdownUtils.insertMarkdownSyntax(
                        textFieldValue.text,
                        textFieldValue.selection.start,
                        textFieldValue.selection.end,
                        MarkdownSyntax.HEADING_2
                    )
                    onTextChange(TextFieldValue(newText, TextRange(cursorPos)))
                }
            )

            ToolbarButton(
                icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                contentDescription = "列表",
                onClick = {
                    val (newText, cursorPos) = MarkdownUtils.insertMarkdownSyntax(
                        textFieldValue.text,
                        textFieldValue.selection.start,
                        textFieldValue.selection.end,
                        MarkdownSyntax.BULLET_LIST
                    )
                    onTextChange(TextFieldValue(newText, TextRange(cursorPos)))
                }
            )

            ToolbarButton(
                icon = Icons.Default.FormatListNumbered,
                contentDescription = "编号列表",
                onClick = {
                    val (newText, cursorPos) = MarkdownUtils.insertMarkdownSyntax(
                        textFieldValue.text,
                        textFieldValue.selection.start,
                        textFieldValue.selection.end,
                        MarkdownSyntax.NUMBERED_LIST
                    )
                    onTextChange(TextFieldValue(newText, TextRange(cursorPos)))
                }
            )

            ToolbarButton(
                icon = Icons.Default.CheckBoxOutlineBlank,
                contentDescription = "任务列表",
                onClick = {
                    val (newText, cursorPos) = MarkdownUtils.insertMarkdownSyntax(
                        textFieldValue.text,
                        textFieldValue.selection.start,
                        textFieldValue.selection.end,
                        MarkdownSyntax.TASK_LIST
                    )
                    onTextChange(TextFieldValue(newText, TextRange(cursorPos)))
                }
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(24.dp)
                    .padding(horizontal = 4.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            )

            ToolbarButton(
                icon = Icons.Default.Code,
                contentDescription = "行内代码",
                onClick = {
                    val (newText, cursorPos) = MarkdownUtils.insertMarkdownSyntax(
                        textFieldValue.text,
                        textFieldValue.selection.start,
                        textFieldValue.selection.end,
                        MarkdownSyntax.CODE
                    )
                    onTextChange(TextFieldValue(newText, TextRange(cursorPos)))
                }
            )

            ToolbarButton(
                icon = Icons.Default.DataObject,
                contentDescription = "代码块",
                onClick = {
                    val (newText, cursorPos) = MarkdownUtils.insertMarkdownSyntax(
                        textFieldValue.text,
                        textFieldValue.selection.start,
                        textFieldValue.selection.end,
                        MarkdownSyntax.CODE_BLOCK
                    )
                    onTextChange(TextFieldValue(newText, TextRange(cursorPos)))
                }
            )

            ToolbarButton(
                icon = Icons.Default.Link,
                contentDescription = "链接",
                onClick = {
                    val (newText, cursorPos) = MarkdownUtils.insertMarkdownSyntax(
                        textFieldValue.text,
                        textFieldValue.selection.start,
                        textFieldValue.selection.end,
                        MarkdownSyntax.LINK
                    )
                    onTextChange(TextFieldValue(newText, TextRange(cursorPos)))
                }
            )

            ToolbarButton(
                icon = Icons.Default.Image,
                contentDescription = "图片",
                onClick = {
                    val (newText, cursorPos) = MarkdownUtils.insertMarkdownSyntax(
                        textFieldValue.text,
                        textFieldValue.selection.start,
                        textFieldValue.selection.end,
                        MarkdownSyntax.IMAGE
                    )
                    onTextChange(TextFieldValue(newText, TextRange(cursorPos)))
                }
            )

            ToolbarButton(
                icon = Icons.Default.TableChart,
                contentDescription = "表格",
                onClick = {
                    val (newText, cursorPos) = MarkdownUtils.insertMarkdownSyntax(
                        textFieldValue.text,
                        textFieldValue.selection.start,
                        textFieldValue.selection.end,
                        MarkdownSyntax.TABLE
                    )
                    onTextChange(TextFieldValue(newText, TextRange(cursorPos)))
                }
            )

            ToolbarButton(
                icon = Icons.Default.AccountTree,
                contentDescription = "流程图",
                onClick = {
                    val (newText, cursorPos) = MarkdownUtils.insertMarkdownSyntax(
                        textFieldValue.text,
                        textFieldValue.selection.start,
                        textFieldValue.selection.end,
                        MarkdownSyntax.MERMAID
                    )
                    onTextChange(TextFieldValue(newText, TextRange(cursorPos)))
                }
            )

            ToolbarButton(
                icon = Icons.Default.FormatQuote,
                contentDescription = "引用",
                onClick = {
                    val (newText, cursorPos) = MarkdownUtils.insertMarkdownSyntax(
                        textFieldValue.text,
                        textFieldValue.selection.start,
                        textFieldValue.selection.end,
                        MarkdownSyntax.QUOTE
                    )
                    onTextChange(TextFieldValue(newText, TextRange(cursorPos)))
                }
            )

            ToolbarButton(
                icon = Icons.Default.HorizontalRule,
                contentDescription = "分割线",
                onClick = {
                    val (newText, cursorPos) = MarkdownUtils.insertMarkdownSyntax(
                        textFieldValue.text,
                        textFieldValue.selection.start,
                        textFieldValue.selection.end,
                        MarkdownSyntax.HORIZONTAL_RULE
                    )
                    onTextChange(TextFieldValue(newText, TextRange(cursorPos)))
                }
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box {
            IconButton(onClick = { showModeMenu = true }) {
                Icon(
                    imageVector = when (editorMode) {
                        EditorMode.EDIT -> Icons.Default.Edit
                        EditorMode.PREVIEW -> Icons.Default.Visibility
                        EditorMode.SPLIT -> Icons.Default.VerticalSplit
                    },
                    contentDescription = "编辑模式"
                )
            }

            DropdownMenu(
                expanded = showModeMenu,
                onDismissRequest = { showModeMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("编辑模式") },
                    onClick = {
                        onModeChange(EditorMode.EDIT)
                        showModeMenu = false
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                )
                DropdownMenuItem(
                    text = { Text("预览模式") },
                    onClick = {
                        onModeChange(EditorMode.PREVIEW)
                        showModeMenu = false
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Visibility, contentDescription = null)
                    }
                )
                DropdownMenuItem(
                    text = { Text("分屏模式") },
                    onClick = {
                        onModeChange(EditorMode.SPLIT)
                        showModeMenu = false
                    },
                    leadingIcon = {
                        Icon(Icons.Default.VerticalSplit, contentDescription = null)
                    }
                )
            }
        }
    }
}

@Composable
fun TitleAndContentEditor(
    title: String,
    textFieldValue: TextFieldValue,
    onTitleChange: (String) -> Unit,
    onTextChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        BasicTextField(
            value = title,
            onValueChange = onTitleChange,
            textStyle = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box {
                    if (title.isEmpty()) {
                        Text(
                            text = "标题",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
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

        Divider()

        Spacer(modifier = Modifier.height(16.dp))

        BasicTextField(
            value = textFieldValue,
            onValueChange = onTextChange,
            textStyle = TextStyle(
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.heightIn(min = 400.dp)) {
                    if (textFieldValue.text.isEmpty()) {
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
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun MarkdownWebPreview(
    html: String,
    editorMode: EditorMode,
    onModeChange: (EditorMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentZoom by remember { mutableFloatStateOf(1.0f) }
    var showModeMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "预览",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    IconButton(
                        onClick = { showModeMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = when (editorMode) {
                                EditorMode.EDIT -> Icons.Default.Edit
                                EditorMode.PREVIEW -> Icons.Default.Visibility
                                EditorMode.SPLIT -> Icons.Default.VerticalSplit
                            },
                            contentDescription = "编辑模式",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showModeMenu,
                        onDismissRequest = { showModeMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("编辑模式") },
                            onClick = {
                                onModeChange(EditorMode.EDIT)
                                showModeMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("预览模式") },
                            onClick = {
                                onModeChange(EditorMode.PREVIEW)
                                showModeMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Visibility, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("分屏模式") },
                            onClick = {
                                onModeChange(EditorMode.SPLIT)
                                showModeMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.VerticalSplit, contentDescription = null)
                            }
                        )
                    }
                }

                IconButton(
                    onClick = { if (currentZoom > 0.5f) currentZoom -= 0.25f },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomOut,
                        contentDescription = "缩小",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "${(currentZoom * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = { if (currentZoom < 3.0f) currentZoom += 0.25f },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "放大",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { currentZoom = 1.0f },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "重置",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        builtInZoomControls = true
                        displayZoomControls = false
                        setSupportZoom(true)
                        layoutAlgorithm = android.webkit.WebSettings.LayoutAlgorithm.NORMAL
                    }
                }
            },
            update = { webView ->
                val zoom = currentZoom
                val scaledHtml = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes">
                        <style>
                            html, body {
                                margin: 0;
                                padding: 0;
                                width: 100%;
                            }
                            body {
                                zoom: $zoom;
                            }
                        </style>
                    </head>
                    <body>$html</body>
                    </html>
                """.trimIndent()
                webView.loadDataWithBaseURL(
                    null,
                    scaledHtml,
                    "text/html",
                    "UTF-8",
                    null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

@Composable
fun ToolbarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
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
