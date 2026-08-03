package com.paulrod.shelved.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.paulrod.shelved.R
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.GameStatus
import com.paulrod.shelved.ui.theme.Accent
import com.paulrod.shelved.ui.theme.SurfaceElevated
import com.paulrod.shelved.ui.theme.TextMuted
import com.paulrod.shelved.ui.theme.TextPrimary
import com.paulrod.shelved.ui.search.SearchFailure

@Composable
fun SearchField(
    value: String,
    placeholder: String = "Search for a game…",
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Outlined.Search, null) },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onChange("") }) { Icon(Icons.Default.Close, "Clear") }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = shelvedFieldColors(),
    )
}

@Composable
fun SearchResultRow(game: Game, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(54.dp).clip(RoundedCornerShape(9.dp)).background(SurfaceElevated)) {
            game.coverModel()?.let { AsyncImage(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(game.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 2)
            game.released?.take(4)?.let { Text(it, color = TextMuted, fontSize = 12.sp) }
        }
        Text("›", color = TextMuted, fontSize = 24.sp)
    }
}

@Composable
fun SearchFailurePanel(
    failure: SearchFailure,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            stringResource(failure.messageResource),
            color = TextMuted,
            fontSize = 13.sp,
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.search_retry), color = Accent, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogGameSheet(
    game: Game,
    alreadyAdded: Boolean = false,
    isDetailsLoading: Boolean = false,
    detailsError: String? = null,
    onClose: () -> Unit,
    onAdd: (Game) -> Unit,
) {
    var status by remember { mutableStateOf(GameStatus.BACKLOG) }
    var descriptionExpanded by remember(game.id) { mutableStateOf(false) }
    ShelvedSheet("Game Details", onClose, scrollable = false) {
        Column(
            Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState()),
        ) {
            Row(Modifier.fillMaxWidth()) {
                GameCover(game, Modifier.weight(.36f))
                Column(Modifier.weight(.64f).padding(start = 16.dp)) {
                    Text(game.name, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    game.released?.take(4)?.let { Text(it, color = TextMuted, modifier = Modifier.padding(top = 4.dp)) }
                    game.playtime?.let { Text("About $it hours", color = TextMuted, fontSize = 13.sp) }
                }
            }
            if (isDetailsLoading) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(color = Accent, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    Text("Loading game details…", color = TextMuted, fontSize = 13.sp)
                }
            }
            game.description?.let { description ->
                SectionLabel("About")
                if (descriptionExpanded) {
                    TextButton(onClick = { descriptionExpanded = false }) { Text("Show less", color = Accent) }
                }
                Text(
                    description,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    maxLines = if (descriptionExpanded) Int.MAX_VALUE else 4,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!descriptionExpanded && description.length > 240) {
                    TextButton(onClick = { descriptionExpanded = true }) { Text("Read more", color = Accent) }
                }
            }
            detailsError?.let { message ->
                Text(message, color = TextMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 16.dp))
            }
            if (game.platforms.isNotEmpty()) {
                SectionLabel("Platforms")
                Text(game.platforms.take(4).joinToString("  •  "), color = TextMuted, fontSize = 13.sp)
            }
            SectionLabel("Add to")
            StatusPicker(status) { status = it }
            PrimaryButton(
                if (alreadyAdded) "Already in Shelved" else "Add to Shelved",
                enabled = !alreadyAdded,
            ) { onAdd(game.copy(status = status)) }
        }
    }
}
