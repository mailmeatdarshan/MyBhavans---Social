package com.bhavans.mybhavans.feature.feed.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.bhavans.mybhavans.core.ui.theme.BhavansPrimary
import com.bhavans.mybhavans.feature.feed.domain.model.Comment
import com.bhavans.mybhavans.feature.feed.domain.model.Post
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onNavigateBack: () -> Unit,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(postId) {
        viewModel.onEvent(FeedEvent.LoadPostDetail(postId))
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.onEvent(FeedEvent.ClearPostDetail)
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onEvent(FeedEvent.ClearError)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            CommentInputBar(
                value = state.commentText,
                onValueChange = { viewModel.onEvent(FeedEvent.UpdateCommentText(it)) },
                onSendClick = { viewModel.onEvent(FeedEvent.AddComment) },
                isLoading = state.isAddingComment
            )
        }
    ) { paddingValues ->
        if (state.selectedPost == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BhavansPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
            ) {
                // Post content
                item {
                    PostDetailContent(
                        post = state.selectedPost!!,
                        currentUserId = currentUserId,
                        onLikeClick = { viewModel.onEvent(FeedEvent.LikePost(postId)) },
                        onUnlikeClick = { viewModel.onEvent(FeedEvent.UnlikePost(postId)) }
                    )
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Comments (${state.comments.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                if (state.isLoadingComments) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = BhavansPrimary)
                        }
                    }
                } else if (state.comments.isEmpty()) {
                    item {
                        Text(
                            text = "No comments yet. Be the first to comment!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
                        )
                    }
                } else {
                    items(
                        items = state.comments,
                        key = { it.id }
                    ) { comment ->
                        CommentItem(
                            comment = comment,
                            currentUserId = currentUserId,
                            onDeleteClick = { viewModel.onEvent(FeedEvent.DeleteComment(comment.id)) }
                        )
                    }
                }

                // Bottom spacing for keyboard
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun PostDetailContent(
    post: Post,
    currentUserId: String,
    onLikeClick: () -> Unit,
    onUnlikeClick: () -> Unit
) {
    val isLiked = post.isLikedBy(currentUserId)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Author header
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (post.authorPhotoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = post.authorPhotoUrl,
                        contentDescription = "Author photo",
                        modifier = Modifier.size(48.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = post.authorName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = post.authorName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatDate(post.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        Text(
            text = post.content,
            style = MaterialTheme.typography.bodyLarge
        )

        // Image
        if (post.imageUrl.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            AsyncImage(
                model = post.imageUrl,
                contentDescription = "Post image",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Like button
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (isLiked) onUnlikeClick() else onLikeClick() }
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isLiked) "Unlike" else "Like",
                    tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Text(
                text = "${post.likeCount} likes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun CommentItem(
    comment: Comment,
    currentUserId: String,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (comment.authorPhotoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = comment.authorPhotoUrl,
                        contentDescription = "Author photo",
                        modifier = Modifier.size(36.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = comment.authorName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = comment.authorName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatTimeAgo(comment.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (comment.authorId == currentUserId) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete comment",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Write a comment...") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BhavansPrimary,
                cursorColor = BhavansPrimary
            ),
            shape = RoundedCornerShape(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onSendClick,
            enabled = value.isNotBlank() && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = BhavansPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send comment",
                    tint = if (value.isNotBlank()) BhavansPrimary 
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m"
        diff < 86400_000 -> "${diff / 3600_000}h"
        diff < 604800_000 -> "${diff / 86400_000}d"
        else -> {
            val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
