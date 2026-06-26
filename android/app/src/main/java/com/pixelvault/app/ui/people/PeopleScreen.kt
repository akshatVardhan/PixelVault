package com.pixelvault.app.ui.people

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.pixelvault.app.data.remote.ClusterDto
import com.pixelvault.app.ui.components.PeopleEmptyState
import com.pixelvault.app.ui.components.ShimmerGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(
    onClusterClick: (Int) -> Unit,
    viewModel: PeopleViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "People",
                        style = MaterialTheme.typography.headlineMedium
                    )
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
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
            state.clusters.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    PeopleEmptyState()
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    items(state.clusters, key = { it.id }) { cluster ->
                        PersonClusterCard(
                            cluster = cluster,
                            onClick = { onClusterClick(cluster.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonClusterCard(
    cluster: ClusterDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxWidth()
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = null,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = cluster.name?.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = cluster.name ?: "Person ${cluster.id}",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${cluster.faceCount} ${if (cluster.faceCount == 1) "photo" else "photos"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
