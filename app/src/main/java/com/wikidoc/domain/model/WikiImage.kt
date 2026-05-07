package com.wikidoc.domain.model

data class WikiImage(
    val id: Long = 0,
    val fileName: String,
    val originalName: String,
    val path: String,
    val mimeType: String,
    val size: Long,
    val width: Int? = null,
    val height: Int? = null,
    val documentId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
