package com.bhavans.mybhavans.feature.canteen.presentation

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.bhavans.mybhavans.core.ui.theme.BhavansAccent
import com.bhavans.mybhavans.core.ui.theme.BhavansPrimary
import com.bhavans.mybhavans.core.ui.theme.ErrorColor
import com.bhavans.mybhavans.core.ui.theme.SuccessColor
import com.bhavans.mybhavans.core.ui.theme.WarningColor
import com.bhavans.mybhavans.feature.canteen.domain.model.Canteen
import com.bhavans.mybhavans.feature.canteen.domain.model.CrowdLevel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanteenScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: CanteenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Canteen Tracker",
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
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BhavansPrimary)
                }
            }
            state.canteens.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No canteens available",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(paddingValues)
                ) {
                    items(state.canteens) { canteen ->
                        CanteenCard(
                            canteen = canteen,
                            onClick = { onNavigateToDetail(canteen.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CanteenCard(
    canteen: Canteen,
    onClick: () -> Unit
) {
    val crowdColor = getCrowdColor(canteen.currentCrowdLevel)
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Canteen image or icon
                if (canteen.imageUrl != null) {
                    AsyncImage(
                        model = canteen.imageUrl,
                        contentDescription = canteen.name,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BhavansAccent.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(30.dp),
                            tint = BhavansAccent
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
                        Text(
                            text = canteen.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Open/Closed badge
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (canteen.isOpen) SuccessColor.copy(alpha = 0.1f) else ErrorColor.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (canteen.isOpen) "OPEN" else "CLOSED",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (canteen.isOpen) SuccessColor else ErrorColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = canteen.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Crowd level indicator
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = crowdColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = getCrowdText(canteen.currentCrowdLevel),
                    style = MaterialTheme.typography.labelMedium,
                    color = crowdColor,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${canteen.crowdPercentage}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = crowdColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { canteen.crowdPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = crowdColor,
                trackColor = crowdColor.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Wait time and check-ins
            Row {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "~${canteen.avgWaitTime} min wait",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "${canteen.checkInsLast30Min} check-ins in 30min",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

fun getCrowdColor(level: CrowdLevel): Color {
    return when (level) {
        CrowdLevel.EMPTY -> SuccessColor
        CrowdLevel.LOW -> SuccessColor
        CrowdLevel.MODERATE -> WarningColor
        CrowdLevel.BUSY -> BhavansAccent
        CrowdLevel.CROWDED -> ErrorColor
    }
}

fun getCrowdText(level: CrowdLevel): String {
    return when (level) {
        CrowdLevel.EMPTY -> "Almost Empty"
        CrowdLevel.LOW -> "Not Crowded"
        CrowdLevel.MODERATE -> "Moderate"
        CrowdLevel.BUSY -> "Busy"
        CrowdLevel.CROWDED -> "Very Crowded"
    }
}
