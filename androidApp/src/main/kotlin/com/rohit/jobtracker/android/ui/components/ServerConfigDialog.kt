package com.rohit.jobtracker.android.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import com.rohit.jobtracker.android.network.ServerConfig

@Composable
fun ServerConfigDialog(
    currentUrl: String,
    currentApiKey: String = "",
    onDismiss: () -> Unit,
    onSave: (url: String, apiKey: String) -> Unit
) {
    var urlInput by remember { mutableStateOf(currentUrl) }
    var apiKeyInput by remember { mutableStateOf(currentApiKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Server & API Configuration") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                    singleLine = true
                )

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("API Key (X-API-Key)") },
                    modifier = Modifier.fillMaxWidth(),
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
