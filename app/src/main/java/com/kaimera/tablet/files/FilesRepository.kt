package com.kaimera.tablet.files

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore

data class MediaFile(
    val uri: Uri,
    val name: String,
    val dateAdded: Long,
    val isVideo: Boolean,
    val size: Long = 0,
    val width: Int = 0,
    val height: Int = 0,
    val duration: Long = 0
)

interface FilesRepository {
    suspend fun getMedia(): List<MediaFile>
    suspend fun deleteFile(uri: Uri): android.content.IntentSender?
    suspend fun renameFile(uri: Uri, newName: String): Boolean
}

class FilesRepositoryImpl(private val context: Context) : FilesRepository {
    override suspend fun getMedia(): List<MediaFile> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val mediaList = mutableListOf<MediaFile>()
        
        // Query Images
        queryMediaStore(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            false,
            mediaList
        )

        // Query Videos
        queryMediaStore(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            true,
            mediaList
        )

        // Sort by Date Descending
        mediaList.sortByDescending { it.dateAdded }
        
        mediaList
    }

    private fun queryMediaStore(
        collection: Uri,
        isVideo: Boolean,
        list: MutableList<MediaFile>
    ) {
        val projection = mutableListOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT
        ).apply {
            if (isVideo) {
                add(MediaStore.Video.VideoColumns.DURATION)
            }
        }.toTypedArray()

        // Add error handling safely
        try {
            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val widthCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH)
                val heightCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT)
                val durationCol = if (isVideo) cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION) else -1

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "Unknown"
                    val date = cursor.getLong(dateCol)
                    val size = cursor.getLong(sizeCol)
                    val width = cursor.getInt(widthCol)
                    val height = cursor.getInt(heightCol)
                    val duration = if (durationCol != -1) cursor.getLong(durationCol) else 0L
                    val uri = ContentUris.withAppendedId(collection, id)
                    
                    list.add(MediaFile(uri, name, date, isVideo, size, width, height, duration))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun deleteFile(uri: Uri): android.content.IntentSender? {
        return try {
            context.contentResolver.delete(uri, null, null)
            null // Deleted successfully without IntentSender
        } catch (e: android.app.RecoverableSecurityException) {
            e.userAction.actionIntent.intentSender
        } catch (e: Exception) {
            null // Handle other errors
        }
    }

    override suspend fun renameFile(uri: Uri, newName: String): Boolean {
        return try {
            val values = android.content.ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, newName)
            }
            context.contentResolver.update(uri, values, null, null) > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
