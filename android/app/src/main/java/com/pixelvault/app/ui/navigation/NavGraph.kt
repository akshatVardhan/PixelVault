package com.pixelvault.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pixelvault.app.ui.gallery.GalleryScreen
import com.pixelvault.app.ui.gallery.PhotoDetailScreen
import com.pixelvault.app.ui.people.PeopleScreen
import com.pixelvault.app.ui.people.PersonPhotosScreen
import com.pixelvault.app.ui.search.SearchScreen
import com.pixelvault.app.ui.settings.SettingsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Gallery.route) {
        composable(Screen.Gallery.route) {
            GalleryScreen(
                onPhotoClick = { photoId ->
                    navController.navigate(Screen.PhotoDetail.create(photoId))
                }
            )
        }
        composable(
            route = Screen.PhotoDetail.route,
            arguments = listOf(navArgument("photoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val photoId = backStackEntry.arguments?.getLong("photoId") ?: return@composable
            PhotoDetailScreen(
                photoId = photoId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(onPhotoClick = { photoId ->
                navController.navigate(Screen.PhotoDetail.create(photoId))
            })
        }
        composable(Screen.People.route) {
            PeopleScreen(
                onClusterClick = { clusterId ->
                    navController.navigate(Screen.PersonPhotos.create(clusterId))
                }
            )
        }
        composable(
            route = Screen.PersonPhotos.route,
            arguments = listOf(navArgument("clusterId") { type = NavType.LongType })
        ) { backStackEntry ->
            val clusterId = backStackEntry.arguments?.getLong("clusterId") ?: return@composable
            PersonPhotosScreen(
                clusterId = clusterId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
