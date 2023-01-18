package com.nft.gallery.viewmodel

import androidx.camera.core.impl.utils.ExifData
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExifDataViewModel @Inject constructor(): ViewModel() {

    private var _viewState: MutableStateFlow<ExifData?> = MutableStateFlow(null)

    val viewState: StateFlow<ExifData?> = _viewState.asStateFlow()

    fun loadExifData(filePath: String) {
        _viewState.value = null
        viewModelScope.launch {
            val exif = ExifInterface(filePath)
            print(exif)
        }
    }
}