package com.bhavans.mybhavans.feature.splash

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.bhavans.mybhavans.R

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                VideoView(ctx).apply {
                    val videoUri = Uri.parse(
                        "android.resource://${ctx.packageName}/${R.raw.splash_video}"
                    )
                    setVideoURI(videoUri)

                    // When video finishes, navigate forward
                    setOnCompletionListener {
                        onSplashComplete()
                    }

                    // If video fails to load, still navigate forward
                    setOnErrorListener { _, _, _ ->
                        onSplashComplete()
                        true
                    }

                    start()
                }
            }
        )
    }
}
