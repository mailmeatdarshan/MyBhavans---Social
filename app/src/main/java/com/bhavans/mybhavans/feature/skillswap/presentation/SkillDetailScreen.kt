package com.bhavans.mybhavans.feature.skillswap.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
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
import com.bhavans.mybhavans.core.ui.theme.BhavansSecondary
import com.bhavans.mybhavans.core.ui.theme.SuccessColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SkillDetailScreen(
    skillId: String,
    onNavigateBack: () -> Unit,
    viewModel: SkillSwapViewModel = hiltViewModel()
) {
    val detailState by viewModel.detailState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showRequestSheet by remember { mutableStateOf(false) }
    var requestMessage by remember { mutableStateOf("") }

    LaunchedEffect(skillId) {
        viewModel.onEvent(SkillSwapEvent.LoadSkillDetail(skillId))
    }

    LaunchedEffect(detailState.matchRequestSent) {
        if (detailState.matchRequestSent) {
            snackbarHostState.showSnackbar("Request sent successfully!")
            showRequestSheet = false
        }
    }

    LaunchedEffect(detailState.error) {
        detailState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(SkillSwapEvent.ClearError)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Skill Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            detailState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BhavansSecondary)
                }
            }
            detailState.skill != null -> {
                val skill = detailState.skill!!
                val typeColor = if (skill.isTeaching) SuccessColor else BhavansAccent

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // User card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (skill.userProfileUrl != null) {
                                AsyncImage(
                                    model = skill.userProfileUrl,
                                    contentDescription = skill.userName,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(BhavansSecondary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = BhavansSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = skill.userName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${skill.level.name.lowercase().replaceFirstChar { it.uppercase() }} Level",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(
                                        color = typeColor.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (skill.isTeaching) "Teaching" else "Learning",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = typeColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Skill title and category
                    Text(
                        text = skill.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .background(
                                color = getCategoryColor(skill.category).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = skill.category.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium,
                            color = getCategoryColor(skill.category)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    Text(
                        text = "About this skill",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = skill.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Looking for
                    if (skill.lookingFor.isNotEmpty()) {
                        Text(
                            text = "Looking to exchange for",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            skill.lookingFor.forEach { category ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            getCategoryColor(category).copy(alpha = 0.1f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = category.name.lowercase().replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = getCategoryColor(category)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Availability and contact
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            if (skill.availability.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = BhavansSecondary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Availability",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = skill.availability,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = BhavansSecondary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Email",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = skill.userEmail,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            if (skill.phoneNumber != null && skill.contactPreference != "email") {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = BhavansSecondary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Phone",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        Text(
                                            text = skill.phoneNumber,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Connect button
                    Button(
                        onClick = { showRequestSheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BhavansSecondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Connect & Exchange",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                // Request bottom sheet
                if (showRequestSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showRequestSheet = false },
                        sheetState = rememberModalBottomSheetState()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Send Connection Request",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Write a message to ${skill.userName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = requestMessage,
                                onValueChange = { requestMessage = it },
                                placeholder = { Text("Hi! I'd love to learn from you...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    viewModel.onEvent(
                                        SkillSwapEvent.SendMatchRequest(skillId, requestMessage)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = !detailState.isLoading,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BhavansSecondary
                                )
                            ) {
                                if (detailState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White
                                    )
                                } else {
                                    Text(
                                        text = "Send Request",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}
