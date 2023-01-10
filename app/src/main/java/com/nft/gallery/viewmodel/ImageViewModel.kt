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

    private var videosLiveData: MutableStateFlow<List<String>> = MutableStateFlow(listOf())

    fun getImageList(): StateFlow<List<String>> {
        return imagesLiveData.asStateFlow()
    }

    fun getVideoList(): StateFlow<List<String>> {
        return videosLiveData.asStateFlow()
    }

    /**
     * Getting All Images Path.
     *
     * Required Storage Permission
     *
     * @return ArrayList with images Path
     */
    private fun loadImagesFromSDCard(): ArrayList<String> {
        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor?
        val listOfAllImages = ArrayList<String>()
        var absolutePathOfImage: String?
        val context = getApplication<Application>().applicationContext

        val projection =
            arrayOf(MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.DATE_TAKEN)

        cursor = context.contentResolver.query(uri, projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC")

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

    fun loadAllVideos() {
        viewModelScope.launch(Dispatchers.IO) {
            videosLiveData.value = loadVideosFromSDCard()
        }
    }

    private fun loadVideosFromSDCard(): ArrayList<String> {
        val uri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val cursor: Cursor?
        val listOfAllVideos = ArrayList<String>()
        var absolutePathOfVideo: String?
        val context = getApplication<Application>().applicationContext

        val projection =
            arrayOf(MediaStore.Video.VideoColumns.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.DATE_TAKEN)

        cursor = context.contentResolver.query(uri, projection, null, null, MediaStore.Video.Media.DATE_TAKEN + " DESC")

        val columnIndexData = cursor!!.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA)
        while (cursor.moveToNext()) {
            absolutePathOfVideo = cursor.getString(columnIndexData)
            listOfAllVideos.add(absolutePathOfVideo)
        }
        cursor.close()
        return listOfAllVideos
    }
}
