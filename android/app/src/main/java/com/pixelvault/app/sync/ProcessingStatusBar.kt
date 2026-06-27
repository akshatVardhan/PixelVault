package com.pixelvault.app.sync

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ProcessingStatusBar(
    modifier: Modifier = Modifier,
    viewModel: ProcessingStatusViewModel = hiltViewModel()
) {
    val status by viewModel.status.collectAsState()

    AnimatedVisibility(visible = status.isProcessing || status.lastProcessedTime != null || status.unprocessedCount > 0) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (status.isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Processing...",
                        style = MaterialTheme.typography.labelSmall
                    )
                } else if (status.unprocessedCount > 0) {
                    Text(
                        text = "${status.unprocessedCount} photos to process",
                        style = MaterialTheme.typography.labelSmall
                    )
                } else {
                    val last = status.lastProcessedTime?.take(16)?.replace("T", " ") ?: "Never"
                    Text(
                        text = "Last processed: $last",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
