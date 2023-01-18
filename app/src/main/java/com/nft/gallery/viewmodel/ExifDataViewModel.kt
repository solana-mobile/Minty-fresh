package com.nft.gallery.viewmodel

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
class ExifDataViewModel @Inject constructor() : ViewModel() {

    private var _viewState: MutableStateFlow<String?> = MutableStateFlow(null)

    val viewState: StateFlow<String?> = _viewState.asStateFlow()

    fun loadExifData(filePath: String) {
        _viewState.value = null
        viewModelScope.launch {
            val exif = ExifInterface(filePath)
            val latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            val latRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            val longitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            val longitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
            if (latitude != null && longitude != null) {
                _viewState.value =
                    "${convertToDegree(latitude)}$latRef, ${convertToDegree(longitude)}$longitudeRef"
            }
        }
    }

    private fun convertToDegree(stringDMS: String): Double {
        val dms = stringDMS.split(",", limit = 3)

        val degreeString = dms[0].split("/", limit = 2)
        val degreeNumerator = degreeString[0].toDouble()
        val degreeDenominator = degreeString[1].toDouble()
        val degree = degreeNumerator / degreeDenominator

        val minuteString = dms[1].split("/", limit = 2)
        val minuteNumerator = minuteString[0].toDouble()
        val minuteDenominator = minuteString[1].toDouble()
        val minute = minuteNumerator / minuteDenominator

        val secondsString = dms[2].split("/", limit = 2)
        val secondsNumerator = secondsString[0].toDouble()
        val secondsDenominator = secondsString[1].toDouble()
        val seconds = secondsNumerator / secondsDenominator

        return String.format("%.3f", degree + minute / 60 + seconds / 3600).toDouble()
    }
}