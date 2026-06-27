package com.pixelvault.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pixelvault.app.data.local.SettingsDataStore
import com.pixelvault.app.ui.navigation.NavGraph
import com.pixelvault.app.ui.navigation.Screen
import com.pixelvault.app.ui.theme.LocalShadcnColors
import com.pixelvault.app.ui.theme.PixelVaultTheme
import com.pixelvault.app.ui.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("Gallery", Icons.Outlined.PhotoLibrary, Screen.Gallery.route),
    BottomNavItem("Search", Icons.Outlined.Search, Screen.Search.route),
    BottomNavItem("People", Icons.Outlined.People, Screen.People.route),
    BottomNavItem("Settings", Icons.Outlined.Settings, Screen.Settings.route)
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val settings: SettingsDataStore by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by settings.themeMode.collectAsState(initial = "SYSTEM")
            PixelVaultTheme(
                themeMode = try { ThemeMode.valueOf(themeMode) } catch (_: Exception) { ThemeMode.SYSTEM }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PixelVaultMainScreen()
                }
            }
        }
    }
}

@Composable
private fun PixelVaultMainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val density = LocalDensity.current

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier.drawBehind {
                        val borderWidth = with(density) { 1.dp.toPx() }
                        drawRect(
                            color = LocalShadcnColors.current.border,
                            top = 0f,
                            bottom = borderWidth
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = LocalShadcnColors.current.accent,
                                selectedTextColor = LocalShadcnColors.current.accent,
                                unselectedIconColor = LocalShadcnColors.current.mutedForeground,
                                unselectedTextColor = LocalShadcnColors.current.mutedForeground,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavGraph(navController = navController)
        }
    }
}
