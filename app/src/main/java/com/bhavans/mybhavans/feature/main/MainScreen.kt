package com.bhavans.mybhavans.feature.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.bhavans.mybhavans.feature.activity.presentation.ActivityScreen
import com.bhavans.mybhavans.feature.explore.presentation.ExploreScreen
import com.bhavans.mybhavans.feature.feed.presentation.FeedScreen
import com.bhavans.mybhavans.feature.profile.presentation.ProfileScreen

data class BottomNavItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val contentDescription: String
)

@Composable
fun MainScreen(
    onNavigateToCreatePost: () -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
    onNavigateToLostFound: () -> Unit,
    onNavigateToCanteen: () -> Unit,
    onNavigateToSkillSwap: () -> Unit,
    onNavigateToSafeWalk: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onLogout: () -> Unit
) {
    val navItems = listOf(
        BottomNavItem(
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            contentDescription = "Home"
        ),
        BottomNavItem(
            selectedIcon = Icons.Filled.Search,
            unselectedIcon = Icons.Outlined.Search,
            contentDescription = "Explore"
        ),
        BottomNavItem(
            selectedIcon = Icons.Filled.AddBox,
            unselectedIcon = Icons.Outlined.AddBox,
            contentDescription = "Create"
        ),
        BottomNavItem(
            selectedIcon = Icons.Filled.Favorite,
            unselectedIcon = Icons.Outlined.FavoriteBorder,
            contentDescription = "Activity"
        ),
        BottomNavItem(
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person,
            contentDescription = "Profile"
        )
    )

    var selectedIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            Column {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navItems.forEachIndexed { index, item ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    if (index == 2) {
                                        onNavigateToCreatePost()
                                    } else {
                                        selectedIndex = index
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedIndex == index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.contentDescription,
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedVisibility(
                visible = selectedIndex == 0,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FeedScreen(
                    onNavigateToCreatePost = onNavigateToCreatePost,
                    onNavigateToPostDetail = onNavigateToPostDetail
                )
            }
            AnimatedVisibility(
                visible = selectedIndex == 1,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ExploreScreen(
                    onNavigateToLostFound = onNavigateToLostFound,
                    onNavigateToCanteen = onNavigateToCanteen,
                    onNavigateToSkillSwap = onNavigateToSkillSwap,
                    onNavigateToSafeWalk = onNavigateToSafeWalk
                )
            }
            // Index 2 = Create (navigates directly, no screen shown here)
            AnimatedVisibility(
                visible = selectedIndex == 3,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ActivityScreen()
            }
            AnimatedVisibility(
                visible = selectedIndex == 4,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ProfileScreen(
                    onLogout = onLogout,
                    onNavigateToEditProfile = onNavigateToEditProfile,
                    onNavigateToAdmin = onNavigateToAdmin
                )
            }
        }
    }
}
