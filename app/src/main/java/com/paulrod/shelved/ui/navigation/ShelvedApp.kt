package com.paulrod.shelved.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.GameStatus
import com.paulrod.shelved.data.model.Platform
import com.paulrod.shelved.data.model.Profile
import com.paulrod.shelved.ui.SearchState
import com.paulrod.shelved.ui.ShelvedViewModel
import com.paulrod.shelved.ui.theme.Accent
import com.paulrod.shelved.ui.theme.AccentText
import com.paulrod.shelved.ui.theme.Background
import com.paulrod.shelved.ui.theme.Border
import com.paulrod.shelved.ui.theme.Surface
import com.paulrod.shelved.ui.theme.SurfaceElevated
import com.paulrod.shelved.ui.theme.TextMuted
import com.paulrod.shelved.ui.theme.TextPrimary

private enum class Destination(
    val label: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector,
) {
    BACKLOG("Backlog", Icons.Filled.Gamepad, Icons.Outlined.Gamepad),
    SEARCH("Search", Icons.Filled.Search, Icons.Outlined.Search),
    STATS("Stats", Icons.AutoMirrored.Filled.ShowChart, Icons.AutoMirrored.Outlined.ShowChart),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelvedApp(viewModel: ShelvedViewModel = viewModel()) {
    var destination by rememberSaveable { mutableStateOf(Destination.BACKLOG) }
    val games by viewModel.games.collectAsState()
    val profile by viewModel.profile.collectAsState()

    Scaffold(
        containerColor = Background,
        bottomBar = { SamsungFloatingNavigation(destination) { destination = it } },
    ) { contentPadding ->
        Box(Modifier.fillMaxSize().padding(bottom = contentPadding.calculateBottomPadding())) {
            when (destination) {
                Destination.BACKLOG -> BacklogScreen(games, viewModel)
                Destination.SEARCH -> SearchScreen(games, viewModel)
                Destination.STATS -> StatsScreen(games)
                Destination.PROFILE -> ProfileScreen(profile, games, viewModel)
            }
        }
    }
}

@Composable
private fun SamsungFloatingNavigation(selected: Destination, onSelect: (Destination) -> Unit) {
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = bottomInset + 12.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Surface.copy(alpha = .96f))
            .border(1.dp, Color.White.copy(alpha = .10f), RoundedCornerShape(32.dp))
            .padding(7.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Destination.entries.forEach { item ->
            val active = item == selected
            val tint by animateColorAsState(if (active) Accent else TextMuted, label = "tab tint")
            val scale by animateFloatAsState(if (active) 1f else .94f, label = "tab scale")
            Column(
                modifier = Modifier
                    .weight(1f)
                    .scale(scale)
                    .clip(RoundedCornerShape(25.dp))
                    .background(if (active) Accent.copy(alpha = .17f) else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onSelect(item) }
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(if (active) item.activeIcon else item.inactiveIcon, item.label, tint = tint, modifier = Modifier.size(21.dp))
                Text(item.label, color = tint, fontSize = 10.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1, modifier = Modifier.padding(top = 3.dp))
            }
        }
    }
}

@Composable
private fun Screen(title: String, actions: @Composable () -> Unit = {}, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(Background).statusBarsPadding().padding(horizontal = 16.dp)) {
        Row(
            Modifier.fillMaxWidth().height(64.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(title, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-.3).sp)
            actions()
        }
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BacklogScreen(games: List<Game>, viewModel: ShelvedViewModel) {
    var filter by rememberSaveable { mutableStateOf<GameStatus?>(null) }
    var editing by remember { mutableStateOf<Game?>(null) }
    var addVisible by rememberSaveable { mutableStateOf(false) }
    val filtered = remember(games, filter) { if (filter == null) games else games.filter { it.status == filter } }

    Screen("SHELVED", actions = {
        FloatingActionButton(
            onClick = { addVisible = true },
            modifier = Modifier.size(40.dp),
            containerColor = Accent,
            contentColor = AccentText,
            shape = CircleShape,
        ) { Icon(Icons.Default.Add, "Add game") }
    }) {
        StatusFilters(filter) { filter = it }
        if (filtered.isEmpty()) {
            EmptyLibrary(filter == null, Modifier.fillMaxSize()) { addVisible = true }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(filtered, key = { it.id }) { game -> GameCard(game) { editing = game } }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
    if (addVisible) AddGameSheet(viewModel) { addVisible = false }
    editing?.let { game -> EditGameSheet(game, { editing = null }) { viewModel.updateGame(it); editing = null } }
}

@Composable
private fun StatusFilters(selected: GameStatus?, onSelect: (GameStatus?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
        item { FilterPill("All", selected == null) { onSelect(null) } }
        items(GameStatus.entries) { status -> FilterPill(status.label, selected == status) { onSelect(status) } }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        label,
        modifier = Modifier.clip(RoundedCornerShape(18.dp)).background(if (selected) Accent else Surface)
            .clickable(onClick = onClick).padding(horizontal = 13.dp, vertical = 7.dp),
        color = if (selected) AccentText else TextMuted,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun GameCard(game: Game, onClick: () -> Unit) {
    Column(Modifier.clickable(onClick = onClick)) {
        Cover(game.coverImageUrl, Modifier.fillMaxWidth())
        Text(game.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
            maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 7.dp))
    }
}

@Composable
private fun Cover(url: String?, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(11.dp)).background(SurfaceElevated).height(148.dp), contentAlignment = Alignment.Center) {
        if (url != null) AsyncImage(url, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Icon(Icons.Outlined.Gamepad, null, tint = TextMuted, modifier = Modifier.size(30.dp))
    }
}

@Composable
private fun EmptyLibrary(all: Boolean, modifier: Modifier, onAdd: () -> Unit) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Outlined.Gamepad, null, tint = TextMuted, modifier = Modifier.size(46.dp))
        Spacer(Modifier.height(14.dp))
        Text(if (all) "Your shelf is empty" else "Nothing in this section", color = TextPrimary, fontWeight = FontWeight.SemiBold)
        Text(if (all) "Add a game to start your collection." else "Try another status.", color = TextMuted, fontSize = 13.sp)
        if (all) TextButton(onClick = onAdd) { Text("Add your first game", color = Accent) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGameSheet(viewModel: ShelvedViewModel, onClose: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var selected by remember { mutableStateOf<Game?>(null) }
    val results by viewModel.searchResults.collectAsState()
    val state by viewModel.searchState.collectAsState()
    LaunchedEffect(query) { viewModel.search(query) }
    Sheet("Add Game", onClose) {
        SearchField(query) { query = it }
        when (val current = state) {
            SearchState.Loading -> CircularProgressIndicator(color = Accent, modifier = Modifier.align(Alignment.CenterHorizontally).padding(24.dp))
            is SearchState.Error -> Text(current.message, color = TextMuted, modifier = Modifier.padding(vertical = 18.dp))
            else -> Unit
        }
        LazyColumn(Modifier.fillMaxWidth().fillMaxHeight(.72f)) {
            items(results, key = { it.id }) { game -> SearchRow(game) { selected = game } }
        }
    }
    selected?.let { game -> AddSelectedGameSheet(game, { selected = null }) { viewModel.addGame(it); selected = null; onClose() } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreen(library: List<Game>, viewModel: ShelvedViewModel) {
    var query by rememberSaveable { mutableStateOf("") }
    var selected by remember { mutableStateOf<Game?>(null) }
    val results by viewModel.searchResults.collectAsState()
    val state by viewModel.searchState.collectAsState()
    LaunchedEffect(query) { viewModel.search(query) }
    Screen("Search") {
        SearchField(query) { query = it }
        when (val current = state) {
            SearchState.Loading -> CircularProgressIndicator(color = Accent, modifier = Modifier.align(Alignment.CenterHorizontally).padding(24.dp))
            is SearchState.Error -> Text(current.message, color = TextMuted, modifier = Modifier.padding(top = 16.dp))
            SearchState.Ready -> if (results.isEmpty()) Text("No games found.", color = TextMuted, modifier = Modifier.padding(top = 16.dp))
            else -> if (query.isBlank()) SearchPrompt()
        }
        LazyColumn(Modifier.fillMaxSize()) {
            items(results, key = { it.id }) { game -> SearchRow(game) { selected = game } }
        }
    }
    selected?.let { game ->
        AddSelectedGameSheet(game, { selected = null }, alreadyAdded = library.any { it.id == game.id }) {
            viewModel.addGame(it); selected = null
        }
    }
}

@Composable
private fun SearchField(value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value, onChange, Modifier.fillMaxWidth().padding(bottom = 10.dp),
        placeholder = { Text("Search for a game…") },
        leadingIcon = { Icon(Icons.Outlined.Search, null) },
        trailingIcon = { if (value.isNotEmpty()) IconButton({ onChange("") }) { Icon(Icons.Default.Close, "Clear") } },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = fieldColors(),
    )
}

@Composable
private fun SearchPrompt() {
    Column(Modifier.fillMaxWidth().padding(top = 70.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Outlined.Search, null, tint = TextMuted, modifier = Modifier.size(42.dp))
        Text("Find your next game", color = TextPrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp))
        Text("Search the RAWG game database.", color = TextMuted, fontSize = 13.sp)
    }
}

@Composable
private fun SearchRow(game: Game, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
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
private fun AddSelectedGameSheet(game: Game, onClose: () -> Unit, alreadyAdded: Boolean = false, onAdd: (Game) -> Unit) {
    var status by remember { mutableStateOf(GameStatus.BACKLOG) }
    Sheet("Game Details", onClose) {
        Row(Modifier.fillMaxWidth()) {
            Cover(game.coverImageUrl, Modifier.width(110.dp))
            Column(Modifier.padding(start = 16.dp)) {
                Text(game.name, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                game.released?.take(4)?.let { Text(it, color = TextMuted, modifier = Modifier.padding(top = 4.dp)) }
                if (game.playtime != null) Text("About ${game.playtime} hours", color = TextMuted, fontSize = 13.sp)
            }
        }
        if (game.platforms.isNotEmpty()) {
            SectionLabel("Platforms")
            Text(game.platforms.take(4).joinToString("  •  "), color = TextMuted, fontSize = 13.sp)
        }
        SectionLabel("Add to")
        StatusPicker(status) { status = it }
        PrimaryButton(if (alreadyAdded) "Already in Shelved" else "Add to Shelved", enabled = !alreadyAdded) { onAdd(game.copy(status = status)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditGameSheet(game: Game, onClose: () -> Unit, onSave: (Game) -> Unit) {
    var status by remember { mutableStateOf(game.status) }
    var hours by remember { mutableStateOf(game.hoursPlayed?.toString().orEmpty()) }
    var notes by remember { mutableStateOf(game.notes.orEmpty()) }
    Sheet("Game Details", onClose) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(72.dp).clip(RoundedCornerShape(10.dp)).background(SurfaceElevated)) {
                game.coverImageUrl?.let { AsyncImage(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
            }
            Text(game.name, color = TextPrimary, fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 14.dp))
        }
        SectionLabel("Status")
        StatusPicker(status) { status = it }
        SectionLabel("Hours played")
        ShelvedField(hours, { hours = it.filter(Char::isDigit) }, "0", KeyboardType.Number)
        SectionLabel("Notes")
        ShelvedField(notes, { notes = it }, "What do you think so far?", minLines = 3)
        PrimaryButton("Save") { onSave(game.copy(status = status, hoursPlayed = hours.toIntOrNull(), notes = notes.trim().ifBlank { null })) }
    }
}

@Composable
private fun StatusPicker(selected: GameStatus, onSelect: (GameStatus) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(GameStatus.entries) { status -> FilterPill(status.label, status == selected) { onSelect(status) } }
    }
}

@Composable
private fun StatsScreen(games: List<Game>) {
    val totalHours = games.sumOf { it.hoursPlayed ?: 0 }
    Screen("Stats") {
        if (games.isEmpty()) EmptyLibrary(true, Modifier.fillMaxSize()) {}
        else LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Games", games.size.toString(), Modifier.weight(1f))
                    StatCard("Hours", totalHours.toString(), Modifier.weight(1f))
                }
            }
            item { SectionLabel("Library breakdown") }
            items(GameStatus.entries) { status ->
                val count = games.count { it.status == status }
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Surface).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(status.label, color = TextPrimary, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                    Text(count.toString(), color = Accent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(18.dp)).background(Surface).padding(18.dp)) {
        Text(value, color = Accent, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextMuted, fontSize = 13.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(profile: Profile, games: List<Game>, viewModel: ShelvedViewModel) {
    var editing by rememberSaveable { mutableStateOf(false) }
    var menu by rememberSaveable { mutableStateOf(false) }
    val favorites = profile.favoriteGameIds.mapNotNull { id -> games.find { it.id == id } }
    Screen("Profile", actions = {
        Row {
            IconButton({ editing = true }, Modifier.size(40.dp).clip(CircleShape).background(Surface)) { Icon(Icons.Default.Edit, "Edit", tint = TextPrimary, modifier = Modifier.size(18.dp)) }
            Spacer(Modifier.width(8.dp))
            IconButton({ menu = true }, Modifier.size(40.dp).clip(CircleShape).background(Surface)) { Icon(Icons.Default.MoreHoriz, "Menu", tint = TextPrimary) }
        }
    }) {
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(90.dp).clip(CircleShape).background(Surface).border(2.dp, Accent, CircleShape), contentAlignment = Alignment.Center) {
                        Text(profile.displayName.firstOrNull()?.uppercase() ?: "🎮", color = TextPrimary, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(profile.displayName.ifBlank { "Add your name" }, color = TextPrimary, fontSize = 21.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
                    Text("${games.size} ${if (games.size == 1) "game" else "games"} shelved", color = TextMuted, fontSize = 13.sp)
                }
            }
            item { SectionLabel("Bio"); ProfileText(profile.bio, "Tap Edit to add a bio.") }
            item { SectionLabel("Favorite platform"); ProfileText(profile.favoritePlatform?.label.orEmpty(), "No platform selected yet.") }
            item { SectionLabel("Favorite games") }
            if (favorites.isEmpty()) item { ProfileText("", "Pick up to three favorites in Edit.") }
            else item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    favorites.forEach { game -> Column(Modifier.weight(1f)) { Cover(game.coverImageUrl, Modifier.fillMaxWidth()); Text(game.name, color = TextPrimary, fontSize = 11.sp, maxLines = 2) } }
                    repeat(3 - favorites.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
    if (editing) EditProfileSheet(profile, games, { editing = false }) { viewModel.updateProfile(it); editing = false }
    if (menu) Sheet("Menu", { menu = false }) {
        listOf("Settings", "About Shelved").forEach { Text(it, color = TextPrimary, fontSize = 16.sp, modifier = Modifier.fillMaxWidth().clickable { menu = false }.padding(vertical = 16.dp)) }
    }
}

@Composable
private fun ProfileText(value: String, placeholder: String) {
    Text(value.ifBlank { placeholder }, color = if (value.isBlank()) TextMuted else TextPrimary,
        fontStyle = if (value.isBlank()) FontStyle.Italic else FontStyle.Normal, fontSize = 14.sp, modifier = Modifier.padding(bottom = 22.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileSheet(profile: Profile, games: List<Game>, onClose: () -> Unit, onSave: (Profile) -> Unit) {
    var name by remember { mutableStateOf(profile.displayName) }
    var bio by remember { mutableStateOf(profile.bio) }
    var platform by remember { mutableStateOf(profile.favoritePlatform) }
    var favorites by remember { mutableStateOf(profile.favoriteGameIds) }
    Sheet("Edit Profile", onClose) {
        SectionLabel("Display name"); ShelvedField(name, { name = it }, "Your name")
        SectionLabel("Bio"); ShelvedField(bio, { bio = it }, "A little about your gaming taste…", minLines = 3)
        SectionLabel("Favorite platform")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(Platform.entries) { item -> FilterPill(item.label, item == platform) { platform = item } }
        }
        SectionLabel("Favorite games · ${favorites.size}/3")
        if (games.isEmpty()) Text("Add games to your shelf first.", color = TextMuted)
        else LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(games, key = { it.id }) { game ->
                val selected = game.id in favorites
                Box(Modifier.width(88.dp).clickable {
                    favorites = if (selected) favorites - game.id else if (favorites.size < 3) favorites + game.id else favorites
                }) {
                    Cover(game.coverImageUrl, Modifier.fillMaxWidth())
                    if (selected) Box(Modifier.align(Alignment.TopEnd).padding(6.dp).size(24.dp).clip(CircleShape).background(Accent), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Check, null, tint = AccentText, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        PrimaryButton("Save") { onSave(Profile(name.trim(), bio.trim(), platform, favorites)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Sheet(title: String, onClose: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Surface,
        contentColor = TextPrimary,
        dragHandle = { Box(Modifier.padding(vertical = 12.dp).size(38.dp, 4.dp).clip(CircleShape).background(Border)) },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(Modifier.fillMaxWidth().imePadding().padding(start = 20.dp, end = 20.dp, bottom = 28.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                IconButton(onClose) { Icon(Icons.Default.Close, "Close", tint = TextMuted) }
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text.uppercase(), color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = .7.sp,
        modifier = Modifier.padding(top = 22.dp, bottom = 9.dp))
}

@Composable
private fun PrimaryButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick, Modifier.fillMaxWidth().padding(top = 24.dp).height(52.dp), enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = AccentText, disabledContainerColor = Border),
    ) { Text(label, fontWeight = FontWeight.Bold) }
}

@Composable
private fun ShelvedField(value: String, onChange: (String) -> Unit, placeholder: String, keyboardType: KeyboardType = KeyboardType.Text, minLines: Int = 1) {
    OutlinedTextField(
        value, onChange, Modifier.fillMaxWidth(), placeholder = { Text(placeholder) }, minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType), shape = RoundedCornerShape(12.dp), colors = fieldColors(),
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = SurfaceElevated, unfocusedContainerColor = SurfaceElevated,
    focusedBorderColor = Accent, unfocusedBorderColor = Color.Transparent,
    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
    focusedPlaceholderColor = TextMuted, unfocusedPlaceholderColor = TextMuted,
    focusedLeadingIconColor = Accent, unfocusedLeadingIconColor = TextMuted,
    focusedTrailingIconColor = TextMuted, unfocusedTrailingIconColor = TextMuted,
)

private val GameStatus.label: String get() = name.lowercase().replaceFirstChar(Char::uppercase)
