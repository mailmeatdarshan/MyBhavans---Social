package com.bhavans.mybhavans.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.bhavans.mybhavans.core.ui.theme.BhavansPrimary
import com.bhavans.mybhavans.feature.feed.presentation.FeedScreen
import com.bhavans.mybhavans.feature.profile.presentation.ProfileScreen
import com.bhavans.mybhavans.feature.utilities.presentation.UtilitiesScreen

data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun MainScreen(
    onNavigateToCreatePost: () -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
    onNavigateToLostFound: () -> Unit,
    onNavigateToCanteen: () -> Unit,
    onNavigateToSkillSwap: () -> Unit,
    onNavigateToSafeWalk: () -> Unit,
    onLogout: () -> Unit
) {
    val navItems = listOf(
        BottomNavItem(
            title = "Feed",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        BottomNavItem(
            title = "Utilities",
            selectedIcon = Icons.Filled.Build,
            unselectedIcon = Icons.Outlined.Build
        ),
        BottomNavItem(
            title = "Profile",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person
        )
    )

    var selectedIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = androidx.compose.ui.unit.dp.times(3f)
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = {
                            Icon(
                                imageVector = if (selectedIndex == index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BhavansPrimary,
                            selectedTextColor = BhavansPrimary,
                            indicatorColor = BhavansPrimary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedIndex) {
                0 -> FeedScreen(
                    onNavigateToCreatePost = onNavigateToCreatePost,
                    onNavigateToPostDetail = onNavigateToPostDetail
                )
                1 -> UtilitiesScreen(
                    onNavigateToLostFound = onNavigateToLostFound,
                    onNavigateToCanteen = onNavigateToCanteen,
                    onNavigateToSkillSwap = onNavigateToSkillSwap,
                    onNavigateToSafeWalk = onNavigateToSafeWalk
                )
                2 -> ProfileScreen(onLogout = onLogout)
            }
        }
    }
}
