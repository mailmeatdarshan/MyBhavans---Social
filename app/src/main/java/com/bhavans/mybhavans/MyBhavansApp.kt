package com.bhavans.mybhavans

import android.app.Application
import com.bhavans.mybhavans.core.util.Constants
import com.cloudinary.android.MediaManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyBhavansApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Cloudinary — must happen once before any upload
        val config = mapOf("cloud_name" to Constants.CLOUDINARY_CLOUD_NAME)
        MediaManager.init(this, config)
    }
}

