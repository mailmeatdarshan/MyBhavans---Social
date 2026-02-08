package com.bhavans.mybhavans.feature.skillswap.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
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
import com.bhavans.mybhavans.core.ui.theme.BhavansSecondary
import com.bhavans.mybhavans.core.ui.theme.SuccessColor
import com.bhavans.mybhavans.feature.skillswap.domain.model.Skill
import com.bhavans.mybhavans.feature.skillswap.domain.model.SkillCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillSwapScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: SkillSwapViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Skill Swap",
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
                containerColor = BhavansSecondary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Skill",
                    tint = Color.White
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.showTeaching == null,
                    onClick = { viewModel.onEvent(SkillSwapEvent.FilterByType(null)) },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BhavansSecondary.copy(alpha = 0.2f),
                        selectedLabelColor = BhavansSecondary
                    )
                )
                FilterChip(
                    selected = state.showTeaching == true,
                    onClick = { viewModel.onEvent(SkillSwapEvent.FilterByType(true)) },
                    label = { Text("Teaching") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SuccessColor.copy(alpha = 0.2f),
                        selectedLabelColor = SuccessColor
                    )
                )
                FilterChip(
                    selected = state.showTeaching == false,
                    onClick = { viewModel.onEvent(SkillSwapEvent.FilterByType(false)) },
                    label = { Text("Learning") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BhavansAccent.copy(alpha = 0.2f),
                        selectedLabelColor = BhavansAccent
                    )
                )
            }

            // Category filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedCategory == null,
                    onClick = { viewModel.onEvent(SkillSwapEvent.FilterByCategory(null)) },
                    label = { Text("All Categories") }
                )
                SkillCategory.entries.forEach { category ->
                    FilterChip(
                        selected = state.selectedCategory == category,
                        onClick = { viewModel.onEvent(SkillSwapEvent.FilterByCategory(category)) },
                        label = { Text(category.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BhavansSecondary)
                    }
                }
                state.skills.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No skills available",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Be the first to share your skills!",
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
                        items(state.skills) { skill ->
                            SkillCard(
                                skill = skill,
                                onClick = { onNavigateToDetail(skill.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SkillCard(
    skill: Skill,
    onClick: () -> Unit
) {
    val typeColor = if (skill.isTeaching) SuccessColor else BhavansAccent
    val categoryColor = getCategoryColor(skill.category)

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
                // User avatar
                if (skill.userProfileUrl != null) {
                    AsyncImage(
                        model = skill.userProfileUrl,
                        contentDescription = skill.userName,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(BhavansSecondary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = BhavansSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = skill.userName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = skill.level.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Teaching/Learning badge
                Box(
                    modifier = Modifier
                        .background(
                            color = typeColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (skill.isTeaching) "Teaching" else "Learning",
                        style = MaterialTheme.typography.labelSmall,
                        color = typeColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = skill.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = skill.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category chip
                Box(
                    modifier = Modifier
                        .background(
                            color = categoryColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = skill.category.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = categoryColor
                    )
                }

                if (skill.availability.isNotBlank()) {
                    Text(
                        text = "• ${skill.availability}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

fun getCategoryColor(category: SkillCategory): Color {
    return when (category) {
        SkillCategory.PROGRAMMING -> BhavansPrimary
        SkillCategory.DESIGN -> BhavansAccent
        SkillCategory.MUSIC -> Color(0xFF9C27B0)
        SkillCategory.LANGUAGES -> Color(0xFF00BCD4)
        SkillCategory.ACADEMICS -> BhavansSecondary
        SkillCategory.SPORTS -> SuccessColor
        SkillCategory.ART -> Color(0xFFFF5722)
        SkillCategory.PHOTOGRAPHY -> Color(0xFF607D8B)
        SkillCategory.COOKING -> Color(0xFFFF9800)
        SkillCategory.OTHER -> Color(0xFF795548)
    }
}
