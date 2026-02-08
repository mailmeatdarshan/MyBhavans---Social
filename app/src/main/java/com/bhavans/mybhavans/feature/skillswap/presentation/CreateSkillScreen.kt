package com.bhavans.mybhavans.feature.skillswap.presentation

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bhavans.mybhavans.core.ui.theme.BhavansSecondary
import com.bhavans.mybhavans.core.ui.theme.SuccessColor
import com.bhavans.mybhavans.feature.skillswap.domain.model.SkillCategory
import com.bhavans.mybhavans.feature.skillswap.domain.model.SkillLevel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateSkillScreen(
    onNavigateBack: () -> Unit,
    viewModel: SkillSwapViewModel = hiltViewModel()
) {
    val createState by viewModel.createState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var categoryExpanded by remember { mutableStateOf(false) }
    var levelExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(createState.isSuccess) {
        if (createState.isSuccess) {
            snackbarHostState.showSnackbar("Skill created successfully!")
            viewModel.onEvent(SkillSwapEvent.ClearCreateState)
            onNavigateBack()
        }
    }

    LaunchedEffect(createState.error) {
        createState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(SkillSwapEvent.ClearError)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Share Your Skill",
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
            // Teaching or Learning toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (createState.isTeaching) "I want to teach" else "I want to learn",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (createState.isTeaching) SuccessColor else BhavansSecondary
                )
                Switch(
                    checked = createState.isTeaching,
                    onCheckedChange = { viewModel.onEvent(SkillSwapEvent.UpdateIsTeaching(it)) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SuccessColor,
                        checkedTrackColor = SuccessColor.copy(alpha = 0.3f),
                        uncheckedThumbColor = BhavansSecondary,
                        uncheckedTrackColor = BhavansSecondary.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            OutlinedTextField(
                value = createState.title,
                onValueChange = { viewModel.onEvent(SkillSwapEvent.UpdateTitle(it)) },
                label = { Text("Skill Title") },
                placeholder = { Text("e.g., Python Programming") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            OutlinedTextField(
                value = createState.description,
                onValueChange = { viewModel.onEvent(SkillSwapEvent.UpdateDescription(it)) },
                label = { Text("Description") },
                placeholder = { Text("Describe what you can teach or want to learn...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = createState.category.name.lowercase().replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    SkillCategory.entries.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                viewModel.onEvent(SkillSwapEvent.UpdateCategory(category))
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Level dropdown
            ExposedDropdownMenuBox(
                expanded = levelExpanded,
                onExpandedChange = { levelExpanded = !levelExpanded }
            ) {
                OutlinedTextField(
                    value = createState.level.name.lowercase().replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Your Level") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = levelExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = levelExpanded,
                    onDismissRequest = { levelExpanded = false }
                ) {
                    SkillLevel.entries.forEach { level ->
                        DropdownMenuItem(
                            text = { Text(level.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                viewModel.onEvent(SkillSwapEvent.UpdateLevel(level))
                                levelExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Looking for (what skills to exchange)
            Text(
                text = "What would you like in exchange?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Select categories you're interested in learning",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkillCategory.entries.forEach { category ->
                    FilterChip(
                        selected = createState.lookingFor.contains(category),
                        onClick = { viewModel.onEvent(SkillSwapEvent.ToggleLookingFor(category)) },
                        label = { Text(category.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = getCategoryColor(category).copy(alpha = 0.2f),
                            selectedLabelColor = getCategoryColor(category)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Availability
            OutlinedTextField(
                value = createState.availability,
                onValueChange = { viewModel.onEvent(SkillSwapEvent.UpdateAvailability(it)) },
                label = { Text("Availability") },
                placeholder = { Text("e.g., Weekends, Evenings after 6PM") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Contact preference
            Text(
                text = "Contact Preference",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("email", "phone", "both").forEach { pref ->
                    FilterChip(
                        selected = createState.contactPreference == pref,
                        onClick = { viewModel.onEvent(SkillSwapEvent.UpdateContactPreference(pref)) },
                        label = { Text(pref.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            if (createState.contactPreference == "phone" || createState.contactPreference == "both") {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = createState.phoneNumber,
                    onValueChange = { viewModel.onEvent(SkillSwapEvent.UpdatePhoneNumber(it)) },
                    label = { Text("Phone Number") },
                    placeholder = { Text("Enter your phone number") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit button
            Button(
                onClick = { viewModel.onEvent(SkillSwapEvent.CreateSkill) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !createState.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BhavansSecondary
                )
            ) {
                if (createState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Share Skill",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
