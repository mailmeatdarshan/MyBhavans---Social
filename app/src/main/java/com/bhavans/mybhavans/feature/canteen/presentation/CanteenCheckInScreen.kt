package com.bhavans.mybhavans.feature.canteen.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bhavans.mybhavans.core.ui.theme.BhavansPrimary
import com.bhavans.mybhavans.feature.canteen.domain.model.CrowdLevel

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

    LaunchedEffect(canteenId) {
        viewModel.onEvent(CanteenEvent.LoadCanteenDetail(canteenId))
    }

    LaunchedEffect(checkInState.isSuccess) {
        if (checkInState.isSuccess) {
            snackbarHostState.showSnackbar("Thanks for checking in!")
            viewModel.onEvent(CanteenEvent.ClearCheckInState)
            onNavigateBack()
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
                        text = "Check In",
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Canteen info card
            detailState.canteen?.let { canteen ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BhavansPrimary.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = canteen.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = canteen.location,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Current: ",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = getCrowdText(canteen.currentCrowdLevel),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = getCrowdColor(canteen.currentCrowdLevel)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Crowd level selection
            Text(
                text = "How crowded is it?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CrowdLevel.entries.forEach { level ->
                    CrowdLevelOption(
                        level = level,
                        isSelected = checkInState.selectedCrowdLevel == level,
                        onClick = { viewModel.onEvent(CanteenEvent.UpdateCrowdLevel(level)) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Wait time
            Text(
                text = "Estimated wait time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { 
                        viewModel.onEvent(CanteenEvent.UpdateWaitTime(checkInState.waitTime - 5))
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease"
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${checkInState.waitTime}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = BhavansPrimary
                    )
                    Text(
                        text = "minutes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                IconButton(
                    onClick = { 
                        viewModel.onEvent(CanteenEvent.UpdateWaitTime(checkInState.waitTime + 5))
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Optional comment
            Text(
                text = "Add a comment (optional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = checkInState.comment,
                onValueChange = { viewModel.onEvent(CanteenEvent.UpdateComment(it)) },
                placeholder = { Text("e.g., Long queue at main counter...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Submit button
            Button(
                onClick = { viewModel.onEvent(CanteenEvent.SubmitCheckIn(canteenId)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !checkInState.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BhavansPrimary
                )
            ) {
                if (checkInState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Submit Check-In",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
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
            .background(
                if (isSelected) color.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) color else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.People,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (isSelected) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = shortLabel,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}
