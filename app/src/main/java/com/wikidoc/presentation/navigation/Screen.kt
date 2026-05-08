package com.wikidoc.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Editor : Screen("editor/{documentId}") {
        fun createRoute(documentId: Long? = null) = "editor/${documentId ?: -1}"
    }
    data object ImageManager : Screen("image_manager")
    data object Settings : Screen("settings")
    data object Folder : Screen("folder/{folderId}") {
        fun createRoute(folderId: Long? = null) = "folder/${folderId ?: -1}"
    }
    data object SmbImport : Screen("smb_import")
    data object Export : Screen("export")
    data object Favorites : Screen("favorites")
}
