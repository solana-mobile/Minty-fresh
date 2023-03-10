package com.solanamobile.mintyfresh.gallery.viewmodel

import android.app.Application
import android.database.ContentObserver
import android.database.Cursor
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
    val mimeType: String,
    val title: String
)

@HiltViewModel
class MediaViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            loadAllMediaFiles()
        }
    }

    private var mediaStateFlow: MutableStateFlow<List<Media>> = MutableStateFlow(listOf())

    fun getMediaList(): StateFlow<List<Media>> {
        return mediaStateFlow.asStateFlow()
    }

    fun registerContentObserver() {
        getApplication<Application>().contentResolver.registerContentObserver(
            URI,
            true,
            contentObserver
        )
    }

    fun unregisterContentObserver() {
        getApplication<Application>().contentResolver.unregisterContentObserver(contentObserver)
    }

    /**
     * Load all Images from contentResolver.
     *
     * Required Storage Permission
     */
    private fun loadMediaFromSDCard(): ArrayList<Media> {
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
            URI,
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
            mediaStateFlow.update {
                loadMediaFromSDCard()
            }
        }
    }

    companion object {
        private val URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }
}
