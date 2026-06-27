package com.pixelvault.app.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pixelvault.app.ui.theme.LocalShadcnColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val themeOptions = listOf("LIGHT", "DARK", "SYSTEM")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings", style = MaterialTheme.typography.headlineMedium)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // Appearance card
            item {
                SettingsCard(title = "Appearance") {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        themeOptions.forEachIndexed { index, option ->
                            SegmentedButton(
                                selected = state.themeMode == option,
                                onClick = { viewModel.setThemeMode(option) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = themeOptions.size
                                )
                            ) {
                                Text(
                                    when (option) {
                                        "LIGHT" -> "Light"
                                        "DARK" -> "Dark"
                                        else -> "System"
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    SettingsSwitchRow(
                        label = "Use dynamic colors",
                        checked = state.useDynamicColors,
                        onCheckedChange = viewModel::setUseDynamicColors
                    )
                }
            }

            // Processing card
            item {
                SettingsCard(title = "Processing") {
                    SettingsSwitchRow(
                        label = "On-device ML",
                        checked = state.mlEnabled,
                        onCheckedChange = viewModel::setMlEnabled
                    )
                    if (state.unprocessedCount > 0) {
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.processNow() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Process ${state.unprocessedCount} unprocessed")
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Last processed: ${state.lastProcessed}",
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalShadcnColors.current.mutedForeground
                    )
                }
            }

            // Server card
            item {
                SettingsCard(title = "Server") {
                    TextButton(onClick = { viewModel.toggleAdvanced() }) {
                        Text("Advanced")
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    AnimatedVisibility(visible = state.advancedExpanded) {
                        Column {
                            var showPassword by remember { mutableStateOf(false) }
                            OutlinedTextField(
                                value = state.serverUrl,
                                onValueChange = { viewModel.setServerUrl(it) },
                                label = { Text("Server URL") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = state.authToken,
                                onValueChange = { viewModel.setAuthToken(it) },
                                label = { Text("Auth token") },
                                singleLine = true,
                                visualTransformation = if (showPassword) VisualTransformation.None
                                    else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            if (showPassword) Icons.Default.Visibility
                                            else Icons.Default.VisibilityOff,
                                            contentDescription = if (showPassword) "Hide" else "Show"
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.syncNow() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save & Sync")
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    SettingsSwitchRow(
                        label = "Enable remote sync",
                        checked = state.remoteSyncEnabled,
                        onCheckedChange = viewModel::setRemoteSyncEnabled
                    )
                }
            }

            // Storage card
            item {
                SettingsCard(title = "Storage") {
                    Text(
                        text = "${state.photoCount} photos",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { /* clear model cache */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear model cache")
                    }
                }
            }

            // About card
            item {
                SettingsCard(title = "About") {
                    Text(
                        text = "PixelVault v1.0",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Build: 2026.06.27",
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalShadcnColors.current.mutedForeground
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, LocalShadcnColors.current.border),
        color = LocalShadcnColors.current.card
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            HorizontalDivider(
                color = LocalShadcnColors.current.border,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
