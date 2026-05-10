package com.wikidoc.presentation.import_export

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    onBack: () -> Unit
) {
    var exportFormat by remember { mutableStateOf("zip") }
    var includeImages by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                title = { Text("导出文档") },
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "导出格式",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = exportFormat == "zip",
                    onClick = { exportFormat = "zip" },
                    label = { Text("ZIP压缩包") },
                    leadingIcon = if (exportFormat == "zip") {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
                FilterChip(
                    selected = exportFormat == "md",
                    onClick = { exportFormat = "md" },
                    label = { Text("Markdown") },
                    leadingIcon = if (exportFormat == "md") {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "包含图片",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "将文档中的图片一起导出",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = includeImages,
                    onCheckedChange = { includeImages = it }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Upload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("导出所有文档")
            }
        }
    }
}
