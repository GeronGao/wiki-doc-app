package com.wikidoc.presentation.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.wikidoc.presentation.search.SearchScreen
import com.wikidoc.presentation.settings.SettingsScreen
import com.wikidoc.presentation.theme.Primary

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val isCenter: Boolean = false
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Home.route,
        title = "首页",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        route = Screen.Search.route,
        title = "搜索",
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    ),
    BottomNavItem(
        route = "create",
        title = "发布",
        selectedIcon = Icons.Filled.Add,
        unselectedIcon = Icons.Filled.Add,
        isCenter = true
    ),
    BottomNavItem(
        route = Screen.Favorites.route,
        title = "收藏",
        selectedIcon = Icons.Filled.Star,
        unselectedIcon = Icons.Outlined.Star
    ),
    BottomNavItem(
        route = Screen.Settings.route,
        title = "我的",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
)

@Composable
fun WikiDocNavHost(
    onShowCreateDialog: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            XiaohongshuBottomNav(
                currentRoute = currentDestination?.route,
                onNavigate = { route ->
                    if (route == "create") {
                        onShowCreateDialog()
                    } else {
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
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
                    },
                    onCreateDocument = {
                        onShowCreateDialog()
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

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToSmbImport = { navController.navigate(Screen.SmbImport.route) },
                    onNavigateToLocalImport = { navController.navigate(Screen.LocalImport.route) },
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
                    },
                    onFolderClick = { fId ->
                        navController.navigate(Screen.Folder.createRoute(fId))
                    }
                )
            }

            composable(Screen.SmbImport.route) {
                com.wikidoc.presentation.import_export.SmbImportScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onImportComplete = { navController.popBackStack() }
                )
            }

            composable(Screen.LocalImport.route) {
                com.wikidoc.presentation.import_export.LocalImportScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onImportComplete = { navController.popBackStack() }
                )
            }

            composable(Screen.Export.route) {
                com.wikidoc.presentation.import_export.ExportScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Favorites.route) {
                com.wikidoc.presentation.favorites.FavoritesScreen(
                    onDocumentClick = { documentId ->
                        navController.navigate(Screen.Editor.createRoute(documentId))
                    }
                )
            }
        }
    }
}

@Composable
fun XiaohongshuBottomNav(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEachIndexed { index, item ->
                if (item.isCenter) {
                    CenterFabButton(
                        isSelected = false,
                        onClick = { onNavigate(item.route) }
                    )
                } else {
                    val isSelected = currentRoute == item.route
                    BottomNavItem(
                        icon = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        label = item.title,
                        isSelected = isSelected,
                        onClick = { onNavigate(item.route) }
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) Primary else Color(0xFF999999),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "color"
    )

    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier
                .size(26.dp)
                .scale(scale),
            tint = iconColor
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = iconColor,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun CenterFabButton(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(Primary)
            .padding(0.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "发布",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
