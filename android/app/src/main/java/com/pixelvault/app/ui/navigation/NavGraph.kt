package com.pixelvault.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Gallery.route) {
        composable(Screen.Gallery.route) {
            placeholderScreen("Gallery")
        }
        composable(Screen.Search.route) {
            placeholderScreen("Search")
        }
        composable(Screen.People.route) {
            placeholderScreen("People")
        }
        composable(Screen.Settings.route) {
            placeholderScreen("Settings")
        }
        composable(Screen.PhotoDetail.route) {
            placeholderScreen("Photo Detail")
        }
        composable(Screen.PersonPhotos.route) {
            placeholderScreen("Person Photos")
        }
    }
}

@Composable
private fun placeholderScreen(name: String) {
    androidx.compose.material3.Text(
        text = "$name — coming in Prompt 8/9",
        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
    )
}
