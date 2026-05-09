package com.wikidoc.presentation.import_export

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.wikidoc.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalImportScreen(
    onNavigateBack: () -> Unit,
    onImportComplete: () -> Unit,
    viewModel: LocalImportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.processSelectedFiles(uris)
        }
    }

    LaunchedEffect(Unit) {
        filePickerLauncher.launch(arrayOf("application/zip", "text/markdown", "text/x-markdown", "*/*"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            uiState.isImporting -> "导入中..."
                            uiState.isImportComplete -> "导入完成"
                            else -> "本地导入"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
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
                        onSelectMore = {
                            viewModel.resetImportState()
                            filePickerLauncher.launch(arrayOf("application/zip", "text/markdown", "text/x-markdown", "*/*"))
                        },
                        onDismiss = {
                            viewModel.resetImportState()
                            onImportComplete()
                        }
                    )
                }
                else -> {
                    SelectFilesView(
                        onSelectFiles = {
                            filePickerLauncher.launch(arrayOf("application/zip", "text/markdown", "text/x-markdown", "*/*"))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectFilesView(
    onSelectFiles: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.FolderOpen,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "从本地导入",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "支持以下格式：",
            color = OnSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FormatChip(icon = Icons.Default.Article, label = ".md")
            FormatChip(icon = Icons.Default.FolderZip, label = ".zip")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "ZIP 压缩包会保持内部目录结构",
            color = OnSurfaceVariant,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSelectFiles,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("选择文件")
        }
    }
}

@Composable
private fun FormatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = Primary.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = Primary,
                fontWeight = FontWeight.Medium
            )
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
    onSelectMore: () -> Unit,
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
                onClick = onSelectMore,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("继续导入")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("完成")
            }
        }
    }
}
