package com.bhavans.mybhavans.core.navigation

sealed class Routes(val route: String) {
    // Auth Routes
    data object Login : Routes("login")
    data object SignUp : Routes("signup")
    data object VerifyEmail : Routes("verify_email")
    
    // Main Routes
    data object Main : Routes("main")
    data object Feed : Routes("feed")
    data object Utilities : Routes("utilities")
    data object Profile : Routes("profile")
    
    // Feed Routes
    data object CreatePost : Routes("create_post")
    data object PostDetail : Routes("post_detail/{postId}") {
        fun createRoute(postId: String) = "post_detail/$postId"
    }
    
    // Lost & Found Routes
    data object LostFound : Routes("lost_found")
    data object ReportItem : Routes("report_item")
    data object ItemDetail : Routes("item_detail/{itemId}") {
        fun createRoute(itemId: String) = "item_detail/$itemId"
    }
    
    // Canteen Routes
    data object CanteenStatus : Routes("canteen_status")
    data object CanteenCheckIn : Routes("canteen_checkin/{canteenId}") {
        fun createRoute(canteenId: String) = "canteen_checkin/$canteenId"
    }
    
    // Skill Swap Routes
    data object SkillSwap : Routes("skill_swap")
    data object CreateSkillListing : Routes("create_skill_listing")
    data object SkillListingDetail : Routes("skill_listing_detail/{listingId}") {
        fun createRoute(listingId: String) = "skill_listing_detail/$listingId"
    }
    
    // Safe Walk Routes
    data object SafeWalk : Routes("safe_walk")
    data object CreateSafeWalkRequest : Routes("create_safe_walk_request")
    
    // Profile Routes
    data object EditProfile : Routes("edit_profile")
    data object UserProfile : Routes("user_profile/{userId}") {
        fun createRoute(userId: String) = "user_profile/$userId"
    }

    // Admin Routes
    data object AdminDashboard : Routes("admin_dashboard")
    data object AdminUsers : Routes("admin_users")
    data object AdminContent : Routes("admin_content")

    // Explore & Activity (tab content, used internally)
    data object Explore : Routes("explore")
    data object Activity : Routes("activity")
}
