package com.wikidoc.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wikidoc.presentation.editor.EditorScreen
import com.wikidoc.presentation.folder.FolderScreen
import com.wikidoc.presentation.home.HomeScreen
import com.wikidoc.presentation.image.ImageManagerScreen
import com.wikidoc.presentation.import_export.SmbImportScreen
import com.wikidoc.presentation.search.SearchScreen
import com.wikidoc.presentation.settings.SettingsScreen

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route, "首页", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Search.route, "搜索", Icons.Filled.Search, Icons.Outlined.Search),
    BottomNavItem(Screen.Settings.route, "我的", Icons.Filled.Person, Icons.Outlined.Person)
)

@Composable
fun WikiDocNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onDocumentClick = { documentId ->
                        navController.navigate(Screen.Editor.createRoute(documentId))
                    },
                    onFolderClick = { folderId ->
                        navController.navigate(Screen.Folder.createRoute(folderId))
                    },
                    onNavigateToSearch = {
                        navController.navigate(Screen.Search.route)
                    },
                    onCreateFolder = {
                        navController.navigate(Screen.Folder.createRoute(0))
                    }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    onDocumentClick = { documentId ->
                        navController.navigate(Screen.Editor.createRoute(documentId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.Editor.route,
                arguments = listOf(navArgument("documentId") { type = NavType.LongType })
            ) { backStackEntry ->
                val documentId = backStackEntry.arguments?.getLong("documentId") ?: -1L
                EditorScreen(
                    documentId = if (documentId == -1L) null else documentId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ImageManager.route) {
                ImageManagerScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToSmbImport = { navController.navigate(Screen.SmbImport.route) },
                    onNavigateToExport = { navController.navigate(Screen.Export.route) }
                )
            }

            composable(
                route = Screen.Folder.route,
                arguments = listOf(navArgument("folderId") { type = NavType.LongType })
            ) { backStackEntry ->
                val folderId = backStackEntry.arguments?.getLong("folderId") ?: -1L
                FolderScreen(
                    folderId = if (folderId == -1L) null else folderId,
                    onBack = { navController.popBackStack() },
                    onDocumentClick = { documentId ->
                        navController.navigate(Screen.Editor.createRoute(documentId))
                    }
                )
            }

            composable(Screen.SmbImport.route) {
                SmbImportScreen(
                    onBack = { navController.popBackStack() },
                    onImportComplete = { navController.popBackStack() }
                )
            }

            composable(Screen.Export.route) {
                com.wikidoc.presentation.import_export.ExportScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
