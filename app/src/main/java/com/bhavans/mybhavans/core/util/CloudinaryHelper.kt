package com.bhavans.mybhavans.core.util

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume

/**
 * Suspending helper that uploads a local [Uri] to Cloudinary and returns the
 * secure HTTPS URL, or throws an exception on failure.
 *
 * Call this from any repository that needs to store images.
 *
 * @param uri       The local content URI of the image to upload.
 * @param folder    The Cloudinary folder to place the image in (e.g. "profile_images").
 * @param uploadPreset The unsigned upload preset configured in your Cloudinary dashboard.
 */
suspend fun uploadToCloudinary(
    uri: Uri,
    folder: String,
    uploadPreset: String = Constants.CLOUDINARY_UPLOAD_PRESET
): String = suspendCancellableCoroutine { continuation ->
    val publicId = "$folder/${UUID.randomUUID()}"

    val requestId = MediaManager.get()
        .upload(uri)
        .unsigned(uploadPreset)
        .option("folder", folder)
        .option("public_id", publicId)
        .callback(object : UploadCallback {
            override fun onStart(requestId: String) {}

            override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

            override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                val secureUrl = resultData["secure_url"] as? String
                if (secureUrl != null) {
                    continuation.resume(secureUrl)
                } else {
                    continuation.cancel(Exception("Cloudinary upload: secure_url missing in response"))
                }
            }

            override fun onError(requestId: String, error: ErrorInfo) {
                continuation.cancel(Exception("Cloudinary upload failed: ${error.description}"))
            }

            override fun onReschedule(requestId: String, error: ErrorInfo) {
                continuation.cancel(Exception("Cloudinary upload rescheduled: ${error.description}"))
            }
        })
        .dispatch()

    continuation.invokeOnCancellation {
        try { MediaManager.get().cancelRequest(requestId) } catch (_: Exception) {}
    }
}
