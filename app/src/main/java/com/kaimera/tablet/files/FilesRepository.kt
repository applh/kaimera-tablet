package com.kaimera.tablet.files

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore

interface FilesRepository {
    suspend fun getImages(): List<Uri>
}

class FilesRepositoryImpl(private val context: Context) : FilesRepository {
    override suspend fun getImages(): List<Uri> {
        val images = mutableListOf<Uri>()
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val uri = ContentUris.withAppendedId(collection, id)
                    images.add(uri)
                }
            }
        } catch (e: Exception) {
            // Handle error or return empty list
            e.printStackTrace()
        }
        return images
    }
}
