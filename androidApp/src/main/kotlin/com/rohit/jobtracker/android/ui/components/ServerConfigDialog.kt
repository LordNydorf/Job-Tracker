package com.rohit.jobtracker.android.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rohit.jobtracker.android.network.ServerConfig
import com.rohit.jobtracker.android.ui.theme.ThemeMode

@Composable
fun ServerConfigDialog(
    currentUrl: String,
    currentApiKey: String = "",
    currentThemeMode: ThemeMode = ThemeMode.SYSTEM,
    onDismiss: () -> Unit,
    onSave: (url: String, apiKey: String) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit = {}
) {
    var urlInput by remember { mutableStateOf(currentUrl) }
    var apiKeyInput by remember { mutableStateOf(currentApiKey) }
    var selectedThemeMode by remember { mutableStateOf(currentThemeMode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings & Server") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Theme Selection Section
                Text(
                    text = "Theme Appearance",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ThemeMode.entries.forEach { mode ->
                        val isSelected = selectedThemeMode == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedThemeMode = mode
                                onThemeModeChange(mode)
                            },
                            label = {
                                Text(
                                    text = mode.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Server Configuration Section
                Text(
                    text = "Backend Server & API",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Configure the backend REST API endpoint and authentication key.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Server Base URL") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("API Key (X-API-Key)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Text("Quick Presets:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ServerConfig.PRESETS.forEach { preset ->
                        SuggestionChip(
                            onClick = { urlInput = preset.url },
                            label = { Text(preset.label) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (urlInput.isNotBlank()) {
                    onSave(urlInput, apiKeyInput)
                }
                onDismiss()
            }) {
                Text("Save & Connect")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
