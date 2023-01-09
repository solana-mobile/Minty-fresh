package com.nft.gallery

import android.app.Application
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nft.gallery.repository.StorageUploadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImageViewModel @Inject constructor(
    application: Application,
    private val storageRepository: StorageUploadRepository
) : AndroidViewModel(application) {

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

    fun uploadImage(path: String) {
        viewModelScope.launch {
            storageRepository.uploadFile(path)
        }
    }
}
