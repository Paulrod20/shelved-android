package com.paulrod.shelved.ui.backlog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paulrod.shelved.data.model.GameNote
import com.paulrod.shelved.ui.components.SectionLabel
import com.paulrod.shelved.ui.components.ShelvedField
import com.paulrod.shelved.ui.theme.Accent
import com.paulrod.shelved.ui.theme.SurfaceElevated
import com.paulrod.shelved.ui.theme.TextMuted
import com.paulrod.shelved.ui.theme.TextPrimary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun GameNotesSection(
    notes: List<GameNote>,
    onNotesChange: (List<GameNote>) -> Unit,
) {
    var draft by remember { mutableStateOf("") }
    var isAddingNote by remember { mutableStateOf(notes.isEmpty()) }

    SectionLabel("Notes")
    notes.forEach { note -> NoteEntry(note) }

    if (isAddingNote) {
        ShelvedField(draft, { draft = it }, "What do you think so far?", minLines = 3)
        TextButton(
            onClick = {
                onNotesChange(notes + GameNote(draft.trim(), System.currentTimeMillis()))
                draft = ""
                isAddingNote = false
            },
            enabled = draft.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Add note",
                color = if (draft.isNotBlank()) Accent else TextMuted,
                fontWeight = FontWeight.SemiBold,
            )
        }
    } else {
        TextButton(
            onClick = { isAddingNote = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Add another note", color = Accent, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun NoteEntry(note: GameNote) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceElevated)
            .padding(12.dp),
    ) {
        Text(note.text, color = TextPrimary, fontSize = 14.sp)
        Text(
            text = formatNoteTime(note.createdAtEpochMillis),
            color = TextMuted,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

private fun formatNoteTime(epochMillis: Long): String {
    if (epochMillis <= 0L) return "Previously added"
    return NOTE_TIME_FORMATTER.format(Instant.ofEpochMilli(epochMillis))
}

private val NOTE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter
    .ofPattern("MMM d, yyyy · h:mm a")
    .withZone(ZoneId.systemDefault())
