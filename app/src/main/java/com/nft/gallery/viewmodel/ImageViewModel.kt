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

@HiltViewModel
class ImageViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    private var imagesLiveData: MutableStateFlow<List<String>> = MutableStateFlow(listOf())

    fun getImageList(): StateFlow<List<String>> {
        return imagesLiveData.asStateFlow()
    }

    /**
     * Getting All Images Path.
     *
     * Required Storage Permission
     *
     * @return ArrayList with images Path
     */
    private fun loadImagesFromSDCard(): ArrayList<String> {
        val uri: Uri = MediaStore.Files.getContentUri("external")
        val cursor: Cursor?
        val listOfAllImages = ArrayList<String>()
        var absolutePathOfImage: String?
        val context = getApplication<Application>().applicationContext

        val projection =
            arrayOf(
                MediaStore.Files.FileColumns._ID,
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

        val columnIndexData = cursor!!.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(columnIndexData)
            listOfAllImages.add(absolutePathOfImage)
        }
        cursor.close()
        return listOfAllImages
    }

    fun loadAllImages() {
        viewModelScope.launch(Dispatchers.IO) {
            imagesLiveData.value = loadImagesFromSDCard()
        }
    }
}
