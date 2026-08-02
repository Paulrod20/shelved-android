package com.paulrod.shelved.ui.backlog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
internal fun DeleteGamesDialog(gameCount: Int, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val noun = if (gameCount == 1) "game" else "games"
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Delete $gameCount $noun?") },
        text = { Text("This will remove the selected $noun from Shelved. This can’t be undone.") },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
    )
}
