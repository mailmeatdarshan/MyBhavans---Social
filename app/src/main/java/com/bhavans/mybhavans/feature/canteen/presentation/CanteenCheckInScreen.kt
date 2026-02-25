package com.bhavans.mybhavans.feature.canteen.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bhavans.mybhavans.core.ui.theme.AccentOrange
import com.bhavans.mybhavans.core.ui.theme.BhavansPrimary
import com.bhavans.mybhavans.core.ui.theme.BhavansSecondary
import com.bhavans.mybhavans.core.ui.theme.SuccessColor
import com.bhavans.mybhavans.feature.canteen.domain.model.CrowdLevel
import com.bhavans.mybhavans.feature.canteen.domain.model.MenuItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanteenCheckInScreen(
    canteenId: String,
    onNavigateBack: () -> Unit,
    viewModel: CanteenViewModel = hiltViewModel()
) {
    val detailState by viewModel.detailState.collectAsState()
    val checkInState by viewModel.checkInState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showCheckInSheet by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(canteenId) {
        viewModel.onEvent(CanteenEvent.LoadCanteenDetail(canteenId))
    }

    LaunchedEffect(checkInState.isSuccess) {
        if (checkInState.isSuccess) {
            snackbarHostState.showSnackbar("Thanks for checking in! Crowd info updated.")
            viewModel.onEvent(CanteenEvent.ClearCheckInState)
            showCheckInSheet = false
        }
    }

    LaunchedEffect(checkInState.error) {
        checkInState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(CanteenEvent.ClearError)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = detailState.canteen?.name ?: "Canteen",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Check-In button fixed at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = { showCheckInSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BhavansPrimary)
                ) {
                    Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Report Crowd", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { paddingValues ->

        val canteen = detailState.canteen

        if (detailState.isLoading || canteen == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BhavansPrimary)
            }
            return@Scaffold
        }

        // Categories from menu
        val categories = canteen.menuItems
            .map { it.category }
            .distinct()
            .sorted()

        val filteredMenu = if (selectedCategory == null) canteen.menuItems
        else canteen.menuItems.filter { it.category == selectedCategory }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Live Status Card ─────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Live Status",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (canteen.isOpen) SuccessColor.copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.errorContainer,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (canteen.isOpen) "OPEN ● ${canteen.openTime}–${canteen.closeTime}" else "CLOSED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (canteen.isOpen) SuccessColor else MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Crowd bar
                        val crowdColor = getCrowdColor(canteen.currentCrowdLevel)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.People, null, Modifier.size(16.dp), tint = crowdColor)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = getCrowdText(canteen.currentCrowdLevel),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = crowdColor
                            )
                            Spacer(Modifier.weight(1f))
                            Text("${canteen.crowdPercentage}%", style = MaterialTheme.typography.labelMedium, color = crowdColor)
                        }
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { canteen.crowdPercentage / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = crowdColor,
                            trackColor = crowdColor.copy(alpha = 0.15f)
                        )

                        Spacer(Modifier.height(14.dp))

                        // Stats row
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            CanteenStatChip(
                                icon = { Icon(Icons.Default.AccessTime, null, Modifier.size(14.dp), tint = BhavansPrimary) },
                                label = "~${canteen.avgWaitTime} min wait"
                            )
                            CanteenStatChip(
                                icon = { Icon(Icons.Default.EventSeat, null, Modifier.size(14.dp), tint = BhavansSecondary) },
                                label = "${canteen.totalSeats - canteen.occupiedSeats} seats free"
                            )
                            CanteenStatChip(
                                icon = { Icon(Icons.Default.People, null, Modifier.size(14.dp), tint = AccentOrange) },
                                label = "${canteen.checkInsLast30Min} checked in"
                            )
                        }
                    }
                }
            }

            // ── Today's Specials ─────────────────────────────────────────
            if (canteen.specialItems.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocalFireDepartment, null, Modifier.size(20.dp), tint = AccentOrange)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Today's Specials",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }

                item {
                    // Look up specials in the menu for price info
                    val specialMenuItems = canteen.menuItems.filter { it.name in canteen.specialItems }
                    val specialNames = canteen.specialItems

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(specialNames) { name ->
                            val price = specialMenuItems.find { it.name == name }?.price
                            SpecialItemCard(name = name, price = price)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // ── Menu Header ──────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Restaurant, null, Modifier.size(20.dp), tint = BhavansPrimary)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Full Menu",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            // ── Category filter chips ─────────────────────────────────────
            if (categories.size > 1) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("All") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BhavansPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                                label = { Text(cat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BhavansPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // ── Menu items ───────────────────────────────────────────────
            if (filteredMenu.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No items in this category",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredMenu, key = { it.name + it.category }) { item ->
                    MenuItemRow(item = item, isSpecial = item.name in canteen.specialItems)
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }

    // ── Check-In Bottom Sheet ────────────────────────────────────────────────
    if (showCheckInSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCheckInSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Report Current Crowd",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Help fellow students know what to expect",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(20.dp))

                Text("How crowded is it right now?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    CrowdLevel.entries.forEach { level ->
                        CrowdLevelOption(
                            level = level,
                            isSelected = checkInState.selectedCrowdLevel == level,
                            onClick = { viewModel.onEvent(CanteenEvent.UpdateCrowdLevel(level)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text("Estimated wait time", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { viewModel.onEvent(CanteenEvent.UpdateWaitTime(maxOf(0, checkInState.waitTime - 5))) },
                        modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) { Icon(Icons.Default.Remove, null) }
                    Spacer(Modifier.width(20.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${checkInState.waitTime}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = BhavansPrimary)
                        Text("minutes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(20.dp))
                    IconButton(
                        onClick = { viewModel.onEvent(CanteenEvent.UpdateWaitTime(checkInState.waitTime + 5)) },
                        modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) { Icon(Icons.Default.Add, null) }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = checkInState.comment,
                    onValueChange = { viewModel.onEvent(CanteenEvent.UpdateComment(it)) },
                    placeholder = { Text("Optional note — e.g., long queue at counter") },
                    modifier = Modifier.fillMaxWidth().height(90.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.onEvent(CanteenEvent.SubmitCheckIn(canteenId)) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    enabled = !checkInState.isLoading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BhavansPrimary)
                ) {
                    if (checkInState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Submit Check-In", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ── Sub-components ───────────────────────────────────────────────────────────

@Composable
private fun CanteenStatChip(icon: @Composable () -> Unit, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        icon()
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SpecialItemCard(name: String, price: Double?) {
    Card(
        modifier = Modifier.width(130.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(AccentOrange.copy(alpha = 0.18f), BhavansSecondary.copy(alpha = 0.12f))
                    )
                )
                .padding(12.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AccentOrange.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Star, null, Modifier.size(18.dp), tint = AccentOrange)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                if (price != null) {
                    Text(
                        text = "₹${price.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = BhavansPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuItemRow(item: MenuItem, isSpecial: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Special star badge
        if (isSpecial) {
            Icon(Icons.Default.Star, null, Modifier.size(14.dp), tint = AccentOrange)
            Spacer(Modifier.width(6.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSpecial) FontWeight.SemiBold else FontWeight.Normal
                )
                if (!item.isAvailable) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Unavailable",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
            Text(
                text = item.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "₹${item.price.toInt()}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (item.isAvailable) BhavansPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun CrowdLevelOption(
    level: CrowdLevel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = getCrowdColor(level)
    val shortLabel = when (level) {
        CrowdLevel.EMPTY -> "Empty"
        CrowdLevel.LOW -> "Low"
        CrowdLevel.MODERATE -> "Med"
        CrowdLevel.BUSY -> "Busy"
        CrowdLevel.CROWDED -> "Full"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(if (isSelected) 2.dp else 0.dp, if (isSelected) color else Color.Transparent, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.People,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (isSelected) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = shortLabel,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}
