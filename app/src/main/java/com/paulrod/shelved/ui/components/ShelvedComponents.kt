package com.paulrod.shelved.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.GameStatus
import com.paulrod.shelved.ui.theme.Accent
import com.paulrod.shelved.ui.theme.AccentText
import com.paulrod.shelved.ui.theme.Background
import com.paulrod.shelved.ui.theme.Border
import com.paulrod.shelved.ui.theme.Surface
import com.paulrod.shelved.ui.theme.SurfaceElevated
import com.paulrod.shelved.ui.theme.TextMuted
import com.paulrod.shelved.ui.theme.TextPrimary

@Composable
fun ShelvedScreen(
    title: String,
    actions: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(Modifier.fillMaxSize().background(Background).statusBarsPadding().padding(horizontal = 16.dp)) {
        Row(
            Modifier.fillMaxWidth().height(64.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                title,
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-.3).sp,
            )
            actions()
        }
        content()
    }
}

@Composable
fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label,
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) Accent else Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 7.dp),
        color = if (selected) AccentText else TextMuted,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
fun StatusPicker(selected: GameStatus, onSelect: (GameStatus) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(GameStatus.entries) { status ->
            FilterPill(status.label, status == selected) { onSelect(status) }
        }
    }
}

@Composable
fun GameCard(
    game: Game,
    selected: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    Column(Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)) {
        Box(Modifier.fillMaxWidth().height(148.dp)) {
            GameCover(game.coverImageUrl, Modifier.fillMaxSize())
            if (selected) {
                Box(
                    Modifier.fillMaxSize().clip(RoundedCornerShape(11.dp))
                        .background(Accent.copy(alpha = .2f))
                        .border(2.dp, Accent, RoundedCornerShape(11.dp)),
                )
                Box(
                    Modifier.align(Alignment.TopEnd).padding(7.dp).size(24.dp)
                        .clip(RoundedCornerShape(8.dp)).background(Accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Check, "Selected", tint = AccentText, modifier = Modifier.size(17.dp))
                }
            }
        }
        Text(
            game.name,
            color = if (selected) Accent else TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().padding(top = 7.dp),
        )
    }
}

@Composable
fun GameCover(url: String?, modifier: Modifier = Modifier) {
    Box(
        modifier.clip(RoundedCornerShape(11.dp)).background(SurfaceElevated).height(148.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (url != null) {
            AsyncImage(url, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Icon(Icons.Outlined.Gamepad, null, tint = TextMuted, modifier = Modifier.size(30.dp))
        }
    }
}

@Composable
fun EmptyLibrary(all: Boolean, modifier: Modifier, onAdd: (() -> Unit)? = null) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Outlined.Gamepad, null, tint = TextMuted, modifier = Modifier.size(46.dp))
        Spacer(Modifier.height(14.dp))
        Text(
            if (all) "Your shelf is empty" else "Nothing in this section",
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            if (all) "Add a game to start your collection." else "Try another status.",
            color = TextMuted,
            fontSize = 13.sp,
        )
        if (all && onAdd != null) {
            TextButton(onClick = onAdd) { Text("Add your first game", color = Accent) }
        }
    }
}

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
            game.coverImageUrl?.let { AsyncImage(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(game.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 2)
            game.released?.take(4)?.let { Text(it, color = TextMuted, fontSize = 12.sp) }
        }
        Text("›", color = TextMuted, fontSize = 24.sp)
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
    ShelvedSheet("Game Details", onClose) {
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(Modifier.fillMaxWidth()) {
                GameCover(game.coverImageUrl, Modifier.weight(.36f))
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
                    TextButton(onClick = { descriptionExpanded = false }) {
                        Text("Show less", color = Accent)
                    }
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
                    TextButton(onClick = { descriptionExpanded = true }) {
                        Text("Read more", color = Accent)
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelvedSheet(
    title: String,
    onClose: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Surface,
        contentColor = TextPrimary,
        dragHandle = {
            Box(Modifier.padding(vertical = 12.dp).size(38.dp, 4.dp).clip(RoundedCornerShape(2.dp)).background(Border))
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Close", tint = TextMuted) }
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        color = TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = .7.sp,
        modifier = Modifier.padding(top = 22.dp, bottom = 9.dp),
    )
}

@Composable
fun PrimaryButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp).height(52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Accent,
            contentColor = AccentText,
            disabledContainerColor = Border,
        ),
    ) { Text(label, fontWeight = FontWeight.Bold) }
}

@Composable
fun ShelvedField(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = shelvedFieldColors(),
    )
}

@Composable
private fun shelvedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = SurfaceElevated,
    unfocusedContainerColor = SurfaceElevated,
    focusedBorderColor = Accent,
    unfocusedBorderColor = Color.Transparent,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedPlaceholderColor = TextMuted,
    unfocusedPlaceholderColor = TextMuted,
    focusedLeadingIconColor = Accent,
    unfocusedLeadingIconColor = TextMuted,
    focusedTrailingIconColor = TextMuted,
    unfocusedTrailingIconColor = TextMuted,
)

val GameStatus.label: String get() = name.lowercase().replaceFirstChar(Char::uppercase)
