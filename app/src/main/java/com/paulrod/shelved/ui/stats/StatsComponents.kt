package com.paulrod.shelved.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.paulrod.shelved.data.model.Game
import com.paulrod.shelved.data.model.GameStatus
import com.paulrod.shelved.ui.components.label
import com.paulrod.shelved.ui.theme.Accent
import com.paulrod.shelved.ui.theme.Border
import com.paulrod.shelved.ui.theme.Surface
import com.paulrod.shelved.ui.theme.SurfaceElevated
import com.paulrod.shelved.ui.theme.TextMuted
import com.paulrod.shelved.ui.theme.TextPrimary
import java.text.NumberFormat
import kotlin.math.roundToInt

private val PlayingColor = Color(0xFF55C2A4)
private val BacklogColor = Color(0xFF55555E)

@Composable
internal fun StatsSummaryCard(state: StatsUiState) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Surface).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryMetric(state.totalGames.toString(), "Games", Modifier.weight(1f))
            SummaryMetric(state.totalHours.toString(), "Hours", Modifier.weight(1f))
        }
        HorizontalDivider(color = Border)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryMetric(state.completedGames.toString(), "Completed", Modifier.weight(1f), compact = true)
            SummaryMetric("${state.completionRate}%", "Completion", Modifier.weight(1f), compact = true)
        }
    }
}

@Composable
private fun SummaryMetric(value: String, label: String, modifier: Modifier, compact: Boolean = false) {
    Column(modifier) {
        Text(
            value,
            color = Accent,
            fontSize = if (compact) 24.sp else 34.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-.6).sp,
        )
        Text(label, color = TextMuted, fontSize = 13.sp)
    }
}

@Composable
internal fun LibraryProgressCard(state: StatsUiState) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Surface).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        SegmentedProgressBar(state)
        GameStatus.entries.forEach { status ->
            ProgressLegendRow(
                label = status.label,
                count = state.statusCounts[status] ?: 0,
                percentage = state.percentageFor(status),
                color = status.progressColor,
            )
        }
    }
}

@Composable
private fun SegmentedProgressBar(state: StatsUiState) {
    Row(
        Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).background(Border),
    ) {
        GameStatus.entries.forEach { status ->
            val count = state.statusCounts[status] ?: 0
            if (count > 0) {
                Box(Modifier.weight(count.toFloat()).fillMaxHeight().background(status.progressColor))
            }
        }
    }
}

@Composable
private fun ProgressLegendRow(label: String, count: Int, percentage: Int, color: Color) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).clip(CircleShape).background(color))
        Text(label, color = TextPrimary, fontSize = 14.sp, modifier = Modifier.padding(start = 10.dp).weight(1f))
        Text("$count", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text("  $percentage%", color = TextMuted, fontSize = 13.sp)
    }
}

@Composable
internal fun PlaytimeInsights(state: StatsUiState) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        InsightCard(
            value = state.averageHoursPerTrackedGame.formattedHours(),
            label = "Avg. tracked game",
            modifier = Modifier.weight(1f),
        )
        InsightCard(
            value = state.topPlatform ?: "—",
            label = "Top platform",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun InsightCard(value: String, label: String, modifier: Modifier) {
    Column(
        modifier.height(104.dp).clip(RoundedCornerShape(18.dp)).background(Surface).padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            value,
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(label, color = TextMuted, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
internal fun MostPlayedGameRow(rank: Int, game: Game) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Surface).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "#$rank",
            color = TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp),
        )
        GameThumbnail(game)
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(
                game.name,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(game.status.label, color = TextMuted, fontSize = 12.sp)
        }
        Text(
            "${game.hoursPlayed ?: 0}h",
            color = Accent,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 6.dp),
        )
    }
}

@Composable
private fun GameThumbnail(game: Game) {
    Box(
        Modifier.size(width = 46.dp, height = 64.dp).clip(RoundedCornerShape(9.dp)).background(SurfaceElevated),
        contentAlignment = Alignment.Center,
    ) {
        if (game.coverImageUrl != null) {
            AsyncImage(game.coverImageUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Icon(Icons.Outlined.Gamepad, null, tint = TextMuted, modifier = Modifier.size(22.dp))
        }
    }
}

private val GameStatus.progressColor: Color
    get() = when (this) {
        GameStatus.BACKLOG -> BacklogColor
        GameStatus.PLAYING -> PlayingColor
        GameStatus.COMPLETED -> Accent
    }

private fun StatsUiState.percentageFor(status: GameStatus): Int {
    if (totalGames == 0) return 0
    return ((statusCounts[status] ?: 0) * 100f / totalGames).roundToInt()
}

private fun Float.formattedHours(): String {
    if (this == 0f) return "—"
    val formatter = NumberFormat.getNumberInstance().apply { maximumFractionDigits = 1 }
    return "${formatter.format(this)}h"
}
