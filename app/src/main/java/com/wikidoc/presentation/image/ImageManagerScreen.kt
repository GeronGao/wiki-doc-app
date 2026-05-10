package com.wikidoc.presentation.image

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.wikidoc.domain.model.WikiImage
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageManagerScreen(
    viewModel: ImageManagerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("图片管理") },
                actions = {
                    if (uiState.isSelectionMode) {
                        IconButton(onClick = { viewModel.deleteSelectedImages() }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除")
                        }
                    }
                },
                windowInsets = WindowInsets(0.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = uiState.selectedTab) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.onTabSelected(0) },
                    text = { Text("全部") }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.onTabSelected(1) },
                    text = { Text("已使用") }
                )
                Tab(
                    selected = uiState.selectedTab == 2,
                    onClick = { viewModel.onTabSelected(2) },
                    text = { Text("未使用") }
                )
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.images.isEmpty()) {
                EmptyImageState()
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.images) { image ->
                        ImageGridItem(
                            image = image,
                            isSelected = image.id in uiState.selectedImages,
                            onClick = { viewModel.toggleSelection(image.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImageGridItem(
    image: WikiImage,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) {
        Box {
            AsyncImage(
                model = image.path,
                contentDescription = image.originalName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "已选择",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(24.dp)
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Text(
                    text = image.originalName,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatFileSize(image.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyImageState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "暂无图片",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "在文档中插入图片即可在此管理",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatFileSize(size: Long): String {
    val df = DecimalFormat("#.##")
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${df.format(size / 1024.0)} KB"
        else -> "${df.format(size / (1024.0 * 1024.0))} MB"
    }
}
