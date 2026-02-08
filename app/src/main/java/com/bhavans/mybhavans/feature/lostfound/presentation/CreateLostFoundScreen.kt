package com.bhavans.mybhavans.feature.lostfound.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.bhavans.mybhavans.core.ui.theme.BhavansAccent
import com.bhavans.mybhavans.core.ui.theme.BhavansPrimary
import com.bhavans.mybhavans.core.ui.theme.SuccessColor
import com.bhavans.mybhavans.feature.lostfound.domain.model.LostFoundCategory
import com.bhavans.mybhavans.feature.lostfound.domain.model.LostFoundType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLostFoundScreen(
    onNavigateBack: () -> Unit,
    viewModel: LostFoundViewModel = hiltViewModel()
) {
    val state by viewModel.createState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onEvent(LostFoundEvent.UpdateImage(uri))
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            viewModel.onEvent(LostFoundEvent.ClearCreateState)
            onNavigateBack()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(LostFoundEvent.ClearError)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Report Item",
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
            // Type Selection
            Text(
                text = "What happened?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = state.type == LostFoundType.LOST,
                    onClick = { viewModel.onEvent(LostFoundEvent.UpdateType(LostFoundType.LOST)) },
                    label = { Text("I Lost Something") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BhavansAccent.copy(alpha = 0.2f),
                        selectedLabelColor = BhavansAccent
                    )
                )
                FilterChip(
                    selected = state.type == LostFoundType.FOUND,
                    onClick = { viewModel.onEvent(LostFoundEvent.UpdateType(LostFoundType.FOUND)) },
                    label = { Text("I Found Something") },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SuccessColor.copy(alpha = 0.2f),
                        selectedLabelColor = SuccessColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title
            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.onEvent(LostFoundEvent.UpdateTitle(it)) },
                label = { Text("Title") },
                placeholder = { Text("e.g., Blue Wallet, iPhone 14, Keys") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.onEvent(LostFoundEvent.UpdateDescription(it)) },
                label = { Text("Description") },
                placeholder = { Text("Describe the item in detail...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LostFoundCategory.entries.forEach { category ->
                    item {
                        FilterChip(
                            selected = state.category == category,
                            onClick = { viewModel.onEvent(LostFoundEvent.UpdateCategory(category)) },
                            label = { Text(category.name.replace("_", " ")) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BhavansPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = BhavansPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Location
            OutlinedTextField(
                value = state.location,
                onValueChange = { viewModel.onEvent(LostFoundEvent.UpdateLocation(it)) },
                label = { Text("Location") },
                placeholder = { Text("Where was it lost/found?") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contact Number
            OutlinedTextField(
                value = state.contactNumber,
                onValueChange = { viewModel.onEvent(LostFoundEvent.UpdateContactNumber(it)) },
                label = { Text("Contact Number") },
                placeholder = { Text("Your phone number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Image
            Text(
                text = "Photo (Optional)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (state.imageUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = state.imageUri,
                        contentDescription = "Selected image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { viewModel.onEvent(LostFoundEvent.UpdateImage(null)) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(
                                Color.Black.copy(alpha = 0.5f),
                                RoundedCornerShape(50)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove image",
                            tint = Color.White
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add photo",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add a photo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = { viewModel.onEvent(LostFoundEvent.CreateItem) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BhavansPrimary
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Submit Report",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
