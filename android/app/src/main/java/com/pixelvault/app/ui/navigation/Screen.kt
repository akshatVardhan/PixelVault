package com.pixelvault.app.ui.navigation

sealed class Screen(val route: String) {
    data object Gallery : Screen("gallery")
    data object Search : Screen("search")
    data object People : Screen("people")
    data object Settings : Screen("settings")

    data object PhotoDetail : Screen("photo/{photoId}") {
        fun create(photoId: Long) = "photo/$photoId"
    }

    data object PersonPhotos : Screen("person/{clusterId}") {
        fun create(clusterId: Int) = "person/$clusterId"
    }
}
