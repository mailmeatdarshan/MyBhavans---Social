package com.bhavans.mybhavans.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bhavans.mybhavans.feature.admin.presentation.AdminDashboardScreen
import com.bhavans.mybhavans.feature.admin.presentation.ContentModerationScreen
import com.bhavans.mybhavans.feature.admin.presentation.UserManagementScreen
import com.bhavans.mybhavans.feature.auth.presentation.AuthViewModel
import com.bhavans.mybhavans.feature.auth.presentation.LoginScreen
import com.bhavans.mybhavans.feature.auth.presentation.SignUpScreen
import com.bhavans.mybhavans.feature.canteen.presentation.CanteenCheckInScreen
import com.bhavans.mybhavans.feature.canteen.presentation.CanteenScreen
import com.bhavans.mybhavans.feature.feed.presentation.CreatePostScreen
import com.bhavans.mybhavans.feature.feed.presentation.PostDetailScreen
import com.bhavans.mybhavans.feature.lostfound.presentation.CreateLostFoundScreen
import com.bhavans.mybhavans.feature.lostfound.presentation.LostFoundDetailScreen
import com.bhavans.mybhavans.feature.lostfound.presentation.LostFoundScreen
import com.bhavans.mybhavans.feature.library.presentation.LibraryScreen
import com.bhavans.mybhavans.feature.main.MainScreen
import com.bhavans.mybhavans.feature.profile.presentation.EditProfileScreen
import com.bhavans.mybhavans.feature.profile.presentation.UserProfileScreen
import com.bhavans.mybhavans.feature.safewalk.presentation.CreateWalkRequestScreen
import com.bhavans.mybhavans.feature.safewalk.presentation.SafeWalkScreen
import com.bhavans.mybhavans.feature.settings.presentation.SettingsScreen
import com.bhavans.mybhavans.feature.skillswap.presentation.CreateSkillScreen
import com.bhavans.mybhavans.feature.skillswap.presentation.SkillDetailScreen
import com.bhavans.mybhavans.feature.skillswap.presentation.SkillSwapScreen
import com.bhavans.mybhavans.feature.splash.SplashScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.Splash.route
    ) {
        // Splash Screen
        composable(
            route = Routes.Splash.route,
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            SplashScreen(
                onSplashComplete = {
                    val destination = if (authState.isLoggedIn) {
                        Routes.Main.route
                    } else {
                        Routes.Login.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Auth Flow
        composable(
            route = Routes.Login.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) }
        ) {
            LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate(Routes.SignUp.route)
                },
                onLoginSuccess = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.SignUp.route) {
            SignUpScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onSignUpSuccess = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Main App Flow
        composable(Routes.Main.route) {
            MainScreen(
                onNavigateToCreatePost = {
                    navController.navigate(Routes.CreatePost.route)
                },
                onNavigateToPostDetail = { postId ->
                    navController.navigate(Routes.PostDetail.createRoute(postId))
                },
                onNavigateToLostFound = {
                    navController.navigate(Routes.LostFound.route)
                },
                onNavigateToCanteen = {
                    navController.navigate(Routes.CanteenStatus.route)
                },
                onNavigateToSkillSwap = {
                    navController.navigate(Routes.SkillSwap.route)
                },
                onNavigateToSafeWalk = {
                    navController.navigate(Routes.SafeWalk.route)
                },
                onNavigateToLibrary = {
                    navController.navigate(Routes.Library.route)
                },
                onNavigateToEditProfile = {
                    navController.navigate(Routes.EditProfile.route)
                },
                onNavigateToAdmin = {
                    navController.navigate(Routes.AdminDashboard.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.Settings.route)
                },
                onNavigateToUserProfile = { userId ->
                    navController.navigate(Routes.UserProfile.createRoute(userId))
                },
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Main.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Edit Profile
        composable(Routes.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // User Profile (public)
        composable(
            route = Routes.UserProfile.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            UserProfileScreen(
                userId = userId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Settings
        composable(Routes.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Admin Panel
        composable(Routes.AdminDashboard.route) {
            AdminDashboardScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToUsers = {
                    navController.navigate(Routes.AdminUsers.route)
                },
                onNavigateToContent = {
                    navController.navigate(Routes.AdminContent.route)
                }
            )
        }

        composable(Routes.AdminUsers.route) {
            UserManagementScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.AdminContent.route) {
            ContentModerationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Create Post
        composable(Routes.CreatePost.route) {
            CreatePostScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Post Detail
        composable(
            route = Routes.PostDetail.route,
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            PostDetailScreen(
                postId = postId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Lost & Found
        composable(Routes.LostFound.route) {
            LostFoundScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCreate = {
                    navController.navigate(Routes.ReportItem.route)
                },
                onNavigateToDetail = { itemId ->
                    navController.navigate(Routes.ItemDetail.createRoute(itemId))
                }
            )
        }
        
        // Create Lost & Found Item
        composable(Routes.ReportItem.route) {
            CreateLostFoundScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Lost & Found Detail
        composable(
            route = Routes.ItemDetail.route,
            arguments = listOf(
                navArgument("itemId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            LostFoundDetailScreen(
                itemId = itemId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Canteen Status
        composable(Routes.CanteenStatus.route) {
            CanteenScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDetail = { canteenId ->
                    navController.navigate(Routes.CanteenCheckIn.createRoute(canteenId))
                }
            )
        }
        
        // Canteen Check-In
        composable(
            route = Routes.CanteenCheckIn.route,
            arguments = listOf(
                navArgument("canteenId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val canteenId = backStackEntry.arguments?.getString("canteenId") ?: ""
            CanteenCheckInScreen(
                canteenId = canteenId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Skill Swap
        composable(Routes.SkillSwap.route) {
            SkillSwapScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCreate = {
                    navController.navigate(Routes.CreateSkillListing.route)
                },
                onNavigateToDetail = { skillId ->
                    navController.navigate(Routes.SkillListingDetail.createRoute(skillId))
                }
            )
        }
        
        // Create Skill Listing
        composable(Routes.CreateSkillListing.route) {
            CreateSkillScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Skill Listing Detail
        composable(
            route = Routes.SkillListingDetail.route,
            arguments = listOf(
                navArgument("listingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val skillId = backStackEntry.arguments?.getString("listingId") ?: ""
            SkillDetailScreen(
                skillId = skillId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Library
        composable(Routes.Library.route) {
            LibraryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Safe Walk
        composable(Routes.SafeWalk.route) {
            SafeWalkScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCreate = {
                    navController.navigate(Routes.CreateSafeWalkRequest.route)
                },
                onNavigateToDetail = { requestId ->
                    // Safe walk detail not needed — accept/cancel done inline
                }
            )
        }
        
        // Create Safe Walk Request
        composable(Routes.CreateSafeWalkRequest.route) {
            CreateWalkRequestScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
