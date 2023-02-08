package com.solanamobile.mintyfresh.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Image
import androidx.compose.ui.graphics.vector.ImageVector
import com.solanamobile.mintyfresh.R

sealed class NavigationItem(var route: String, var icon: ImageVector, var title: Int) {
    object Camera : NavigationItem("camera", Icons.Outlined.AddAPhoto, R.string.camera)
    object Photos : NavigationItem("photos", Icons.Outlined.Image, R.string.photos)
    object MyMints : NavigationItem("mymints", Icons.Outlined.AutoAwesome, R.string.my_mints)
}
