package com.wikidoc.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.wikidoc.data.external.ExternalDataStore
import com.wikidoc.data.local.database.entity.DocumentEntity
import com.wikidoc.domain.model.Document
import com.wikidoc.presentation.home.CreateDocumentDialog
import com.wikidoc.presentation.navigation.WikiDocNavHost
import com.wikidoc.presentation.theme.WikiDocTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var externalDataStore: ExternalDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            externalDataStore.initialize()
        }

        enableEdgeToEdge()
        setContent {
            WikiDocTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showCreateDialog by remember { mutableStateOf(false) }

                    if (showCreateDialog) {
                        CreateDocumentDialog(
                            onDismiss = { showCreateDialog = false },
                            onConfirm = { title: String ->
                                lifecycleScope.launch {
                                    val document = DocumentEntity(
                                        id = 0,
                                        title = title.ifBlank { "无标题" },
                                        content = "",
                                        folderId = null,
                                        createdAt = System.currentTimeMillis(),
                                        updatedAt = System.currentTimeMillis(),
                                        tags = "",
                                        isFavorite = false,
                                        wordCount = 0
                                    )
                                    externalDataStore.insertDocument(document)
                                    showCreateDialog = false
                                }
                            }
                        )
                    }

                    WikiDocNavHost(
                        onShowCreateDialog = { showCreateDialog = true }
                    )
                }
            }
        }
    }
}
