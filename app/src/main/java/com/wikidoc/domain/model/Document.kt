package com.wikidoc.domain.model

data class Document(
    val id: Long = 0,
    val title: String,
    val content: String,
    val folderId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val wordCount: Int = 0
)
