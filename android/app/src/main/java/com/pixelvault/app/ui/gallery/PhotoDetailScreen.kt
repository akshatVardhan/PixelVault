package com.pixelvault.app.ui.gallery

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.pixelvault.app.ui.theme.LocalShadcnColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PhotoDetailScreen(
    photoId: Long,
    onBack: () -> Unit,
    onPersonClick: (Int) -> Unit = {},
    viewModel: PhotoDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    viewModel.loadPhoto(photoId)

    val photo = state.photo
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(photo?.filename ?: "Photo") },
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
        if (photo == null) return@Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            AsyncImage(
                model = Uri.parse(photo.path),
                contentDescription = photo.filename,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth()
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, LocalShadcnColors.current.border),
                color = LocalShadcnColors.current.card
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = photo.filename,
                        style = MaterialTheme.typography.titleLarge
                    )

                    if (photo.createdAt.isNotBlank()) {
                        Text(
                            text = photo.createdAt,
                            style = MaterialTheme.typography.bodySmall,
                            color = LocalShadcnColors.current.mutedForeground,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = LocalShadcnColors.current.border
                    )

                    val sceneTags = state.tags.filter { it.type == "scene" }
                    val labelTags = state.tags.filter { it.type == "tag" || it.type == "object" }

                    if (labelTags.isNotEmpty()) {
                        Text(
                            text = "TAGS",
                            style = MaterialTheme.typography.labelMedium,
                            color = LocalShadcnColors.current.mutedForeground,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            labelTags.forEach { tag ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(tag.label) }
                                )
                            }
                        }
                        if (sceneTags.isNotEmpty() || state.faces.isNotEmpty()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = LocalShadcnColors.current.border
                            )
                        }
                    }

                    if (sceneTags.isNotEmpty()) {
                        Text(
                            text = "SCENES",
                            style = MaterialTheme.typography.labelMedium,
                            color = LocalShadcnColors.current.mutedForeground,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            sceneTags.forEach { tag ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(tag.label) }
                                )
                            }
                        }
                        if (state.faces.isNotEmpty()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = LocalShadcnColors.current.border
                            )
                        }
                    }

                    if (state.faces.isNotEmpty()) {
                        Text(
                            text = "PEOPLE",
                            style = MaterialTheme.typography.labelMedium,
                            color = LocalShadcnColors.current.mutedForeground,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            state.faces.forEach { face ->
                                SuggestionChip(
                                    onClick = {
                                        face.clusterId?.let { onPersonClick(it) }
                                    },
                                    label = { Text("Person #${face.clusterId ?: "?"}") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
