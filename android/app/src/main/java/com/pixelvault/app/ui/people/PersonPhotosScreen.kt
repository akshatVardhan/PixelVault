package com.pixelvault.app.ui.people

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelvault.app.data.local.PhotoEntity
import com.pixelvault.app.ui.components.EmptyState
import com.pixelvault.app.ui.components.ShimmerGrid
import com.pixelvault.app.ui.gallery.GalleryPhotoItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonPhotosScreen(
    clusterId: Long,
    onBack: () -> Unit,
    viewModel: PersonPhotosViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    viewModel.loadCluster(clusterId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.clusterName ?: "Person $clusterId") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                ShimmerGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
            state.photos.isEmpty() -> {
                EmptyState(
                    icon = Icons.Outlined.People,
                    title = "No photos in this cluster",
                    subtitle = "Photos with this person will appear here",
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    contentPadding = PaddingValues(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    items(state.photos, key = { it.id }) { photo ->
                        GalleryPhotoItem(
                            photo = photo.toPhotoEntity(),
                            onClick = {}
                        )
                    }
                }
            }
        }
    }
}

private fun com.pixelvault.app.data.remote.PhotoDto.toPhotoEntity() = PhotoEntity(
    id = id.toLong(),
    filename = filename,
    hash = "",
    size = 0L,
    createdAt = createdAt,
    syncedAt = null,
    path = path
)
