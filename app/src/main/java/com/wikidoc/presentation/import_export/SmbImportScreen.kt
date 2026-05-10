package com.wikidoc.presentation.import_export

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wikidoc.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmbImportScreen(
    onNavigateBack: () -> Unit,
    onImportComplete: () -> Unit,
    viewModel: SmbImportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var serverAddress by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isImportComplete) {
        if (uiState.isImportComplete) {
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            uiState.isImporting -> "导入中..."
                            uiState.isConnected && uiState.currentPath.isNotEmpty() -> uiState.currentPath.substringAfterLast("/")
                            uiState.isConnected -> "选择文件"
                            else -> "SMB 导入"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.isConnected && uiState.currentPath.isNotEmpty()) {
                                viewModel.navigateUp()
                            } else if (uiState.isConnected) {
                                viewModel.disconnect()
                            } else {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (uiState.isConnected && !uiState.isImporting) {
                        if (uiState.selectedItems.isNotEmpty()) {
                            TextButton(onClick = { viewModel.deselectAll() }) {
                                Text("取消全选")
                            }
                        } else {
                            TextButton(onClick = { viewModel.selectAll() }) {
                                Text("全选")
                            }
                        }
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isConnecting -> {
                    LoadingView(message = "正在连接...")
                }
                uiState.isImporting -> {
                    ImportProgressView(
                        progress = uiState.importProgress,
                        status = uiState.importStatus,
                        importedCount = uiState.importedCount
                    )
                }
                uiState.isImportComplete -> {
                    ImportCompleteView(
                        count = uiState.importedCount,
                        onDismiss = {
                            viewModel.resetImportState()
                            onImportComplete()
                        }
                    )
                }
                !uiState.isConnected -> {
                    ConnectionForm(
                        serverAddress = serverAddress,
                        onServerAddressChange = { serverAddress = it },
                        username = username,
                        onUsernameChange = { username = it },
                        password = password,
                        onPasswordChange = { password = it },
                        showPassword = showPassword,
                        onShowPasswordChange = { showPassword = it },
                        isConnecting = uiState.isConnecting,
                        error = uiState.connectionError,
                        onConnect = {
                            focusManager.clearFocus()
                            viewModel.connect(serverAddress, username, password)
                        }
                    )
                }
                else -> {
                    FileListView(
                        items = uiState.items,
                        selectedItems = uiState.selectedItems,
                        onItemClick = { item ->
                            if (item.isDirectory) {
                                viewModel.expandFolder(item.path)
                            }
                        },
                        onItemSelect = { item ->
                            if (item.isSupported || item.isDirectory) {
                                viewModel.toggleItemSelection(item.path)
                            }
                        },
                        isLoading = uiState.isLoading
                    )

                    if (uiState.selectedItems.isNotEmpty()) {
                        FloatingActionButton(
                            onClick = { viewModel.importSelected() },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                            containerColor = Primary
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = "导入",
                                tint = OnPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionForm(
    serverAddress: String,
    onServerAddressChange: (String) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    showPassword: Boolean,
    onShowPasswordChange: (Boolean) -> Unit,
    isConnecting: Boolean,
    error: String?,
    onConnect: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Cloud,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "连接 SMB 服务器",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = serverAddress,
            onValueChange = onServerAddressChange,
            label = { Text("服务器地址") },
            placeholder = { Text("例如: 192.168.1.100") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("用户名") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("密码") },
            singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onConnect() }),
            trailingIcon = {
                IconButton(onClick = { onShowPasswordChange(!showPassword) }) {
                    Icon(
                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showPassword) "隐藏密码" else "显示密码"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onConnect,
            enabled = !isConnecting && serverAddress.isNotBlank() && username.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = OnPrimary
                )
            } else {
                Icon(Icons.Default.Login, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("连接")
            }
        }
    }
}

@Composable
private fun FileListView(
    items: List<SmbFileItem>,
    selectedItems: Set<String>,
    onItemClick: (SmbFileItem) -> Unit,
    onItemSelect: (SmbFileItem) -> Unit,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (items.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "此目录为空",
                color = OnSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.path }) { item ->
                FileItem(
                    item = item,
                    isSelected = selectedItems.contains(item.path),
                    onClick = { onItemClick(item) },
                    onSelect = { onItemSelect(item) }
                )
            }
        }
    }
}

@Composable
private fun FileItem(
    item: SmbFileItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onSelect: () -> Unit
) {
    val isSelectable = item.isSupported || item.isDirectory

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isSelectable) { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> Primary.copy(alpha = 0.1f)
                else -> Surface
            }
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelect() },
                enabled = isSelectable
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when {
                            item.isDirectory -> FolderIconColor.copy(alpha = 0.15f)
                            item.isMarkdownFile -> DocumentIconColor.copy(alpha = 0.1f)
                            item.isZipFile -> Primary.copy(alpha = 0.1f)
                            else -> OnSurfaceVariant.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        item.isDirectory -> Icons.Default.Folder
                        item.isMarkdownFile -> Icons.Default.Article
                        item.isZipFile -> Icons.Default.FolderZip
                        else -> Icons.Default.InsertDriveFile
                    },
                    contentDescription = null,
                    tint = when {
                        item.isDirectory -> FolderIconColor
                        item.isMarkdownFile -> DocumentIconColor
                        item.isZipFile -> Primary
                        else -> OnSurfaceVariant
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!item.isSupported && item.isDirectory) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(无支持文件)",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant
                        )
                    }
                }
                if (item.isMarkdownFile || item.isZipFile) {
                    Text(
                        text = if (item.isMarkdownFile) "Markdown 文件" else "ZIP 压缩包",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )
                }
            }

            if (item.isDirectory) {
                if (item.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(onClick = onClick) {
                        Icon(
                            imageVector = if (item.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (item.isExpanded) "收起" else "展开"
                        )
                    }
                }
            }
        }
    }

    AnimatedVisibility(
        visible = item.isDirectory && item.isExpanded && item.children.isNotEmpty(),
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Column(
            modifier = Modifier.padding(start = 24.dp)
        ) {
            item.children.forEach { child ->
                FileItem(
                    item = child,
                    isSelected = false,
                    onClick = { },
                    onSelect = { onSelect() }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun LoadingView(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = message)
        }
    }
}

@Composable
private fun ImportProgressView(
    progress: Float,
    status: String,
    importedCount: Int
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(100.dp),
                strokeWidth = 8.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = status,
                color = OnSurfaceVariant
            )

            if (importedCount > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "已导入 $importedCount 个文件",
                    color = Primary
                )
            }
        }
    }
}

@Composable
private fun ImportCompleteView(
    count: Int,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = Primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "导入完成",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "成功导入 $count 个文件",
                color = OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("完成")
            }
        }
    }
}
