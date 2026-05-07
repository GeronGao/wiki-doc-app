package com.wikidoc.presentation.import_export

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmbImportScreen(
    onBack: () -> Unit,
    onImportComplete: () -> Unit,
    viewModel: SmbImportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                title = { Text("SMB导入") },
                actions = {
                    if (uiState.isConnected) {
                        TextButton(
                            onClick = { viewModel.importSelected() },
                            enabled = uiState.selectedFiles.isNotEmpty()
                        ) {
                            Text("导入")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (!uiState.isConnected) {
                ConnectionForm(
                    serverAddress = uiState.serverAddress,
                    username = uiState.username,
                    password = uiState.password,
                    isConnecting = uiState.isConnecting,
                    errorMessage = uiState.errorMessage,
                    onServerAddressChange = { viewModel.updateServerAddress(it) },
                    onUsernameChange = { viewModel.updateUsername(it) },
                    onPasswordChange = { viewModel.updatePassword(it) },
                    onConnect = { viewModel.connect() }
                )
            } else {
                FileSelector(
                    sharedFolders = uiState.sharedFolders,
                    selectedFiles = uiState.selectedFiles,
                    onFileToggle = { viewModel.toggleFileSelection(it) }
                )
            }
        }
    }
}

@Composable
fun ConnectionForm(
    serverAddress: String,
    username: String,
    password: String,
    isConnecting: Boolean,
    errorMessage: String?,
    onServerAddressChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConnect: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = serverAddress,
            onValueChange = onServerAddressChange,
            label = { Text("服务器地址") },
            placeholder = { Text("smb://192.168.1.100") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isConnecting
        )

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("用户名") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isConnecting
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("密码") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isConnecting
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onConnect,
            modifier = Modifier.fillMaxWidth(),
            enabled = serverAddress.isNotBlank() && !isConnecting
        ) {
            if (isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isConnecting) "连接中..." else "连接")
        }
    }
}

@Composable
fun FileSelector(
    sharedFolders: List<String>,
    selectedFiles: Set<String>,
    onFileToggle: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (sharedFolders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("正在加载共享目录...")
            }
        } else {
            Text(
                text = "共享目录",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(sharedFolders) { folder ->
                    FileItem(
                        name = folder,
                        isFolder = true,
                        isSelected = folder in selectedFiles,
                        onClick = { onFileToggle(folder) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "已选择: ${selectedFiles.size} 项",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun FileItem(
    name: String,
    isFolder: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (isFolder) Icons.Default.Folder else Icons.Default.Description,
                contentDescription = null,
                tint = if (isFolder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
