package com.bhavans.mybhavans.core.util

object Constants {
    // College Email Domain
    const val COLLEGE_EMAIL_DOMAIN = "@bhavans.ac.in"
    
    // Firestore Collections
    const val USERS_COLLECTION = "users"
    const val POSTS_COLLECTION = "posts"
    const val COMMENTS_SUBCOLLECTION = "comments"
    const val LIKES_SUBCOLLECTION = "likes"
    const val LOST_FOUND_COLLECTION = "lostFoundItems"
    const val CANTEEN_STATUS_COLLECTION = "canteenStatus"
    const val CHECKINS_SUBCOLLECTION = "checkins"
    const val SKILL_LISTINGS_COLLECTION = "skillListings"
    const val SKILL_REQUESTS_SUBCOLLECTION = "requests"
    const val SAFE_WALK_COLLECTION = "safeWalkRequests"
    
    // Cloudinary Folders (replaces Firebase Storage paths)
    const val CLOUDINARY_CLOUD_NAME = "deuhovrsy" 
    const val CLOUDINARY_UPLOAD_PRESET = "mybhavans_unsigned" 
    const val CLOUDINARY_PROFILE_FOLDER = "mybhavans/profiles"
    const val CLOUDINARY_POST_IMAGES_FOLDER = "mybhavans/posts"
    const val CLOUDINARY_LOST_FOUND_FOLDER = "mybhavans/lostfound"
    
    // Pagination
    const val DEFAULT_PAGE_SIZE = 20
    
    // Canteen Settings
    const val CHECKIN_EXPIRY_MINUTES = 30
    const val MAX_CROWD_LEVEL = 5
    
    // User Roles
    const val ROLE_STUDENT = "student"
    const val ROLE_FACULTY = "faculty"
    const val ROLE_ADMIN = "admin"
    
    // Post Categories
    const val CATEGORY_GENERAL = "general"
    const val CATEGORY_ACADEMIC = "academic"
    const val CATEGORY_EVENTS = "events"
    const val CATEGORY_ANNOUNCEMENTS = "announcements"
    
    // Lost & Found Types
    const val TYPE_LOST = "lost"
    const val TYPE_FOUND = "found"
    
    // Item Categories
    const val ITEM_CATEGORY_ELECTRONICS = "electronics"
    const val ITEM_CATEGORY_DOCUMENTS = "documents"
    const val ITEM_CATEGORY_ACCESSORIES = "accessories"
    const val ITEM_CATEGORY_OTHER = "other"
    
    // Skill Categories
    const val SKILL_CATEGORY_PROGRAMMING = "programming"
    const val SKILL_CATEGORY_MUSIC = "music"
    const val SKILL_CATEGORY_LANGUAGES = "languages"
    const val SKILL_CATEGORY_ACADEMICS = "academics"
    const val SKILL_CATEGORY_OTHER = "other"
    
    // Safe Walk Status
    const val STATUS_OPEN = "open"
    const val STATUS_MATCHED = "matched"
    const val STATUS_COMPLETED = "completed"
    const val STATUS_CANCELLED = "cancelled"
}
