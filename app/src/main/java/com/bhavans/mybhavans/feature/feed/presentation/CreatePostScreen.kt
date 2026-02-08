package com.bhavans.mybhavans.feature.feed.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.bhavans.mybhavans.core.ui.theme.BhavansPrimary
import com.bhavans.mybhavans.feature.feed.domain.model.PostCategory

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onEvent(FeedEvent.UpdatePostImage(uri))
    }

    LaunchedEffect(state.createPostError) {
        state.createPostError?.let { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    LaunchedEffect(state.createPostSuccess) {
        if (state.createPostSuccess) {
            viewModel.onEvent(FeedEvent.ClearCreatePostState)
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create Post",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.onEvent(FeedEvent.ClearCreatePostState)
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
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
            // Category selection
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PostCategory.entries.forEach { category ->
                    FilterChip(
                        selected = state.createPostCategory == category,
                        onClick = { viewModel.onEvent(FeedEvent.UpdatePostCategory(category)) },
                        label = { Text(category.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BhavansPrimary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Content input
            Text(
                text = "What's on your mind?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.createPostContent,
                onValueChange = { viewModel.onEvent(FeedEvent.UpdatePostContent(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = {
                    Text("Share something with your college community...")
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BhavansPrimary,
                    cursorColor = BhavansPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Image picker
            Text(
                text = "Add Image (Optional)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (state.createPostImageUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = state.createPostImageUri,
                        contentDescription = "Selected image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { viewModel.onEvent(FeedEvent.UpdatePostImage(null)) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(50)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove image"
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
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to add an image",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Submit button
            Button(
                onClick = { viewModel.onEvent(FeedEvent.CreatePost) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.isCreatingPost && state.createPostContent.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BhavansPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isCreatingPost) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Post",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
