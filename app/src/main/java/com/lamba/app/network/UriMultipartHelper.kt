package com.lamba.app.network

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.FileNotFoundException

object UriMultipartHelper {
    private const val MAX_IMAGE_BYTES = 5 * 1024 * 1024
    private val supportedMimeTypes = setOf("image/jpeg", "image/png", "image/webp")

    fun createImagePart(contentResolver: ContentResolver, uri: Uri): MultipartBody.Part {
        val mimeType = contentResolver.getType(uri)?.lowercase()
            ?: throw IllegalArgumentException("Unsupported image format")
        if (mimeType !in supportedMimeTypes) {
            throw IllegalArgumentException("Unsupported image format")
        }

        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw FileNotFoundException("Image file not found")
        if (bytes.size > MAX_IMAGE_BYTES) {
            throw IllegalArgumentException("Image is too large")
        }

        return MultipartBody.Part.createFormData(
            "file",
            queryDisplayName(contentResolver, uri) ?: defaultFilename(mimeType),
            RequestBody.create(MediaType.parse(mimeType), bytes),
        )
    }

    private fun queryDisplayName(contentResolver: ContentResolver, uri: Uri): String? {
        val cursor: Cursor = contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null,
        ) ?: return null
        cursor.use {
            if (!it.moveToFirst()) {
                return null
            }
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            return if (index >= 0) it.getString(index) else null
        }
    }

    private fun defaultFilename(mimeType: String): String {
        val extension = when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "img"
        }
        return "breakdown-photo.$extension"
    }
}
