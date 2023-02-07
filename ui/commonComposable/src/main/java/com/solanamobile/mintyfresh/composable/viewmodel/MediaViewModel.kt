package com.solanamobile.mintyfresh.composable.viewmodel

import android.app.Application
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun deliverSelfNotifications(): Boolean {
            return true
        }

        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            loadAllMediaFiles()
        }
    }

    private var _mediaStateFlow: MutableStateFlow<List<Media>> = MutableStateFlow(listOf())

    fun getMediaList(): StateFlow<List<Media>> {
        return _mediaStateFlow.asStateFlow()
    }

    /**
     * Load all Images from contentResolver.
     *
     * Required Storage Permission
     */
    private fun loadMediaFromSDCard(): ArrayList<Media> {
        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor?
        val mediaFiles = ArrayList<Media>()
        val context = getApplication<Application>().applicationContext

        val projection =
            arrayOf(
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.TITLE
            )

        cursor = context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            MediaStore.Images.Media.DATE_ADDED + " DESC"
        )

        val columnIndexData = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        val columnIndexDateAdded = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
        val columnIndexMimeType = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
        val columnIndexTitle = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE)

        while (cursor.moveToNext()) {
            val absolutePathOfImage = cursor.getString(columnIndexData)
            val dateAdded = cursor.getString(columnIndexDateAdded)
            val mimeType = cursor.getString(columnIndexMimeType)
            val title = cursor.getString(columnIndexTitle)

            mediaFiles.add(
                Media(
                    path = absolutePathOfImage,
                    dateAdded = dateAdded,
                    mediaType = MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE,
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
            _mediaStateFlow.update {
                loadMediaFromSDCard()
            }
        }
    }
}
