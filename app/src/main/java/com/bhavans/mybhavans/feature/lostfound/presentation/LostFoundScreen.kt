package com.bhavans.mybhavans.feature.lostfound.presentation

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.bhavans.mybhavans.core.ui.theme.BhavansAccent
import com.bhavans.mybhavans.core.ui.theme.BhavansPrimary
import com.bhavans.mybhavans.core.ui.theme.SuccessColor
import com.bhavans.mybhavans.feature.lostfound.domain.model.LostFoundItem
import com.bhavans.mybhavans.feature.lostfound.domain.model.LostFoundType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LostFoundScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: LostFoundViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Lost & Found",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = BhavansPrimary,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Report Item"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Type filter chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.selectedType == null,
                        onClick = { viewModel.onEvent(LostFoundEvent.SelectType(null)) },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BhavansPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = BhavansPrimary
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = state.selectedType == LostFoundType.LOST,
                        onClick = { viewModel.onEvent(LostFoundEvent.SelectType(LostFoundType.LOST)) },
                        label = { Text("Lost Items") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BhavansAccent.copy(alpha = 0.2f),
                            selectedLabelColor = BhavansAccent
                        )
                    )
                }
                item {
                    FilterChip(
                        selected = state.selectedType == LostFoundType.FOUND,
                        onClick = { viewModel.onEvent(LostFoundEvent.SelectType(LostFoundType.FOUND)) },
                        label = { Text("Found Items") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SuccessColor.copy(alpha = 0.2f),
                            selectedLabelColor = SuccessColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BhavansPrimary)
                    }
                }
                state.items.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No items found",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Be the first to report an item",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.items) { item ->
                            LostFoundItemCard(
                                item = item,
                                onClick = { onNavigateToDetail(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LostFoundItemCard(
    item: LostFoundItem,
    onClick: () -> Unit
) {
    val typeColor = when (item.type) {
        LostFoundType.LOST -> BhavansAccent
        LostFoundType.FOUND -> SuccessColor
    }
    
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Image
            if (item.imageUrl != null) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(typeColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = typeColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Type badge
                    Box(
                        modifier = Modifier
                            .background(
                                color = typeColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.type.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = typeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    if (item.isResolved) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Resolved",
                            modifier = Modifier.size(16.dp),
                            tint = SuccessColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.location,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dateFormat.format(Date(item.createdAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
