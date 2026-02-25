package com.bhavans.mybhavans.feature.library.presentation

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bhavans.mybhavans.core.ui.theme.AccentBlue
import com.bhavans.mybhavans.core.ui.theme.AccentOrange
import com.bhavans.mybhavans.core.ui.theme.AccentPurple
import com.bhavans.mybhavans.core.ui.theme.BhavansPrimary
import com.bhavans.mybhavans.core.ui.theme.BhavansSecondary
import com.bhavans.mybhavans.core.ui.theme.ErrorColor
import com.bhavans.mybhavans.core.ui.theme.SuccessColor
import com.bhavans.mybhavans.core.ui.theme.WarningColor
import com.bhavans.mybhavans.feature.library.domain.model.Library
import com.bhavans.mybhavans.feature.library.domain.model.LibraryBook
import com.bhavans.mybhavans.feature.library.domain.model.LibraryMedia
import com.bhavans.mybhavans.feature.library.domain.model.LibraryStatus
import com.bhavans.mybhavans.feature.library.domain.model.MediaType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onNavigateBack: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Bhavans Library", fontWeight = FontWeight.Bold)
                        state.library?.let {
                            Text(
                                text = "${it.openTime} – ${it.closeTime}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->

        if (state.isLoading && state.library == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = BhavansPrimary) }
            return@Scaffold
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {

            // ── Live Status Card ─────────────────────────────────────────
            state.library?.let { library ->
                item {
                    LibraryStatusCard(library = library)
                    Spacer(Modifier.height(8.dp))
                }
            }

            // ── Newly Added Books ─────────────────────────────────────────
            item {
                SectionHeader(
                    icon = Icons.AutoMirrored.Filled.LibraryBooks,
                    title = "Newly Added Books",
                    iconTint = AccentPurple
                )
                Spacer(Modifier.height(10.dp))
            }

            if (state.isBooksLoading) {
                item {
                    Box(Modifier.fillMaxWidth().height(160.dp), Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), color = BhavansPrimary, strokeWidth = 2.dp)
                    }
                }
            } else if (state.newBooks.isEmpty()) {
                item {
                    EmptySection("No new books found")
                }
            } else {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.newBooks, key = { it.id }) { book ->
                            BookCard(book = book)
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }

            // ── Today's Newspapers & Magazines ───────────────────────────
            item {
                SectionHeader(
                    icon = Icons.Default.Newspaper,
                    title = "Today's Newspapers & Magazines",
                    iconTint = AccentOrange
                )
                Spacer(Modifier.height(10.dp))
            }

            if (state.isMediaLoading) {
                item {
                    Box(Modifier.fillMaxWidth().height(80.dp), Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), color = BhavansPrimary, strokeWidth = 2.dp)
                    }
                }
            } else if (state.todaysMedia.isEmpty()) {
                item { EmptySection("No media available today") }
            } else {
                // Newspapers first, then magazines
                val newspapers = state.todaysMedia.filter { it.type == MediaType.NEWSPAPER }
                val magazines = state.todaysMedia.filter { it.type == MediaType.MAGAZINE }

                if (newspapers.isNotEmpty()) {
                    item {
                        Text(
                            text = "NEWSPAPERS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                    items(newspapers, key = { it.id }) { media ->
                        MediaRow(media = media)
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 60.dp, end = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                }

                if (magazines.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "MAGAZINES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                    items(magazines, key = { it.id }) { media ->
                        MediaRow(media = media)
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 60.dp, end = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}

// ── Sub-components ─────────────────────────────────────────────────────────

@Composable
private fun LibraryStatusCard(library: Library) {
    val statusColor = when (library.status) {
        LibraryStatus.OPEN -> SuccessColor
        LibraryStatus.BUSY -> WarningColor
        LibraryStatus.CLOSED -> ErrorColor
    }
    val statusText = when (library.status) {
        LibraryStatus.OPEN -> "OPEN"
        LibraryStatus.BUSY -> "BUSY"
        LibraryStatus.CLOSED -> "CLOSED"
    }
    val freeSeats = library.totalSeats - library.occupiedSeats
    val occupancyPct = if (library.totalSeats > 0)
        (library.occupiedSeats.toFloat() / library.totalSeats).coerceIn(0f, 1f) else 0f
    val quietFree = library.quietZoneSeats - library.quietZoneOccupied

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top row: title + status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Live Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.14f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Text(
                text = library.location,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(14.dp))

            // Occupancy bar
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EventSeat, null, Modifier.size(16.dp), tint = BhavansPrimary)
                Spacer(Modifier.width(6.dp))
                Text(
                    "$freeSeats seats free of ${library.totalSeats}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = BhavansPrimary
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${(occupancyPct * 100).toInt()}% full",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { occupancyPct },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = when {
                    occupancyPct < 0.5f -> SuccessColor
                    occupancyPct < 0.8f -> WarningColor
                    else -> ErrorColor
                },
                trackColor = BhavansPrimary.copy(alpha = 0.1f)
            )

            Spacer(Modifier.height(14.dp))

            // Amenities / Quiet zone row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                LibStatChip(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    label = "Quiet Zone: $quietFree free",
                    color = AccentBlue
                )
                if (library.wifiAvailable) {
                    LibStatChip(icon = Icons.Default.Wifi, label = "Wi-Fi", color = SuccessColor)
                }
                if (library.printingAvailable) {
                    LibStatChip(icon = Icons.Default.Print, label = "Printing", color = AccentPurple)
                }
            }
        }
    }
}

@Composable
private fun LibStatChip(icon: ImageVector, label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(icon, null, Modifier.size(14.dp), tint = color)
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String, iconTint: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Icon(icon, null, Modifier.size(20.dp), tint = iconTint)
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BookCard(book: LibraryBook) {
    val genreGradient = when (book.genre.lowercase()) {
        "technology" -> listOf(AccentBlue.copy(alpha = 0.2f), BhavansPrimary.copy(alpha = 0.1f))
        "fiction" -> listOf(AccentPurple.copy(alpha = 0.2f), AccentBlue.copy(alpha = 0.1f))
        "self-help" -> listOf(SuccessColor.copy(alpha = 0.2f), AccentBlue.copy(alpha = 0.1f))
        "biography" -> listOf(AccentOrange.copy(alpha = 0.2f), WarningColor.copy(alpha = 0.1f))
        "finance" -> listOf(SuccessColor.copy(alpha = 0.25f), AccentBlue.copy(alpha = 0.1f))
        else -> listOf(BhavansSecondary.copy(alpha = 0.15f), AccentPurple.copy(alpha = 0.1f))
    }

    Card(
        modifier = Modifier.width(150.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(genreGradient))
                .padding(14.dp)
        ) {
            Column {
                // Book icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BhavansPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoStories, null, Modifier.size(22.dp), tint = BhavansPrimary)
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = book.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = book.author,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(6.dp))

                // Genre chip
                Box(
                    modifier = Modifier
                        .background(BhavansPrimary.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(book.genre, style = MaterialTheme.typography.labelSmall, color = BhavansPrimary)
                }

                Spacer(Modifier.height(8.dp))

                // Availability
                if (book.isAvailable) {
                    Text(
                        text = "${book.availableCopies}/${book.totalCopies} available",
                        style = MaterialTheme.typography.labelSmall,
                        color = SuccessColor,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "All copies issued",
                        style = MaterialTheme.typography.labelSmall,
                        color = ErrorColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaRow(media: LibraryMedia) {
    val typeColor = if (media.type == MediaType.NEWSPAPER) AccentBlue else AccentOrange
    val typeIcon = if (media.type == MediaType.NEWSPAPER) Icons.Default.Newspaper else Icons.AutoMirrored.Filled.MenuBook

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(typeColor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(typeIcon, null, Modifier.size(20.dp), tint = typeColor)
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = media.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${media.publisher} · ${media.language}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .background(
                    if (media.isAvailable) SuccessColor.copy(alpha = 0.12f) else ErrorColor.copy(alpha = 0.12f),
                    RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (media.isAvailable) "Available" else "Issued",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (media.isAvailable) SuccessColor else ErrorColor
            )
        }
    }
}

@Composable
private fun EmptySection(text: String) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}
