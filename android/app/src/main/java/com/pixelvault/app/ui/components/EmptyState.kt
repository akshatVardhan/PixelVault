package com.pixelvault.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun GalleryEmptyState(modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Outlined.PhotoLibrary,
        title = "No photos yet",
        subtitle = "Tap Sync to start uploading your photos",
        modifier = modifier
    )
}

@Composable
fun PeopleEmptyState(modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Outlined.People,
        title = "No face clusters yet",
        subtitle = "Sync photos with faces to see them here",
        modifier = modifier
    )
}

@Composable
fun SearchEmptyState(modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Outlined.SearchOff,
        title = "No results found",
        subtitle = "Try a different search term or switch modes",
        modifier = modifier
    )
}

@Composable
fun SyncEmptyState(modifier: Modifier = Modifier) {
    EmptyState(
        icon = Icons.Outlined.Sync,
        title = "Ready to sync",
        subtitle = "Connect to your server and start syncing",
        modifier = modifier
    )
}
