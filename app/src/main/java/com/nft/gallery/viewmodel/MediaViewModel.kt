package com.nft.gallery.viewmodel

import android.app.Application
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


data class Media(
    val path: String,
    val dateAdded: String,
    val mediaType: Int,
    val mimeType: String,
    val title: String
)

@HiltViewModel
class MediaViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    private var mediaLiveData: MutableStateFlow<List<Media>> = MutableStateFlow(listOf())

    fun getMediaList(): StateFlow<List<Media>> {
        return mediaLiveData.asStateFlow()
    }

    /**
     * Load all media (Images and Videos) from contentResolver.
     *
     * Required Storage Permission
     */
    private fun loadMediaFromSDCard(): ArrayList<Media> {
        val uri: Uri = MediaStore.Files.getContentUri("external")
        val cursor: Cursor?
        val mediaFiles = ArrayList<Media>()
        val context = getApplication<Application>().applicationContext

        val projection =
            arrayOf(
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.MIME_TYPE,
                MediaStore.Files.FileColumns.TITLE
            )

        // Return only video and image metadata.
        val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)

        cursor = context.contentResolver.query(
            uri,
            projection,
            selection,
            null,
            MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        )

        val columnIndexData = cursor!!.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
        val columnIndexDateAdded = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
        val columnIndexMediaType = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
        val columnIndexMimeType = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
        val columnIndexTitle = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.TITLE)

        while (cursor.moveToNext()) {
            val absolutePathOfImage = cursor.getString(columnIndexData)
            val dateAdded = cursor.getString(columnIndexDateAdded)
            val mediaType = cursor.getInt(columnIndexMediaType)
            val mimeType = cursor.getString(columnIndexMimeType)
            val title = cursor.getString(columnIndexTitle)

            mediaFiles.add(
                Media(
                    path = absolutePathOfImage,
                    dateAdded = dateAdded,
                    mediaType = mediaType,
                    mimeType = mimeType,
                    title = title
                )
            )
        }
        cursor.close()
        return mediaFiles
    }

    fun loadAllMediaFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            mediaLiveData.value = loadMediaFromSDCard().filter { it.mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE }
        }
    }
}
