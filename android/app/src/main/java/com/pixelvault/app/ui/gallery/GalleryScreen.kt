package com.pixelvault.app.ui.gallery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pixelvault.app.ui.theme.LocalShadcnColors
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelvault.app.ui.components.GalleryEmptyState
import com.pixelvault.app.ui.components.ShimmerGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onPhotoClick: (Long) -> Unit,
    viewModel: GalleryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES
                else Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        if (granted) viewModel.triggerSync()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "PixelVault",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    if (state.unprocessedCount > 0) {
                        Surface(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            shape = RoundedCornerShape(8.dp),
                            color = LocalShadcnColors.current.muted
                        ) {
                            Text(
                                text = "${state.unprocessedCount}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = LocalShadcnColors.current.mutedForeground
                            )
                        }
                    }
                    IconButton(onClick = {
                        if (permissionGranted) {
                            viewModel.triggerSync()
                        } else {
                            permissionLauncher.launch(
                                if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES
                                else Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                        }
                    }) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = "Process",
                            tint = if (state.isSyncing)
                                MaterialTheme.colorScheme.primary
                            else
                                LocalShadcnColors.current.mutedForeground
                        )
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    GalleryEmptyState()
                }
            }
            else -> {
                PullToRefreshBox(
                    isRefreshing = state.isSyncing,
                    onRefresh = { viewModel.triggerSync() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 140.dp),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.photos, key = { it.id }) { photo ->
                            GalleryPhotoItem(
                                photo = photo,
                                onClick = { onPhotoClick(photo.id) },
                                badge = photo.sceneLabel
                            )
                        }
                    }
                }
            }
        }
    }
}
