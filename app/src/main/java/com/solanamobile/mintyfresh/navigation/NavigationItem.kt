package com.solanamobile.mintyfresh.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Image
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(var route: String, var icon: ImageVector, var title: String) {
    object Camera : NavigationItem("camera", Icons.Outlined.AddAPhoto, "Camera")
    object Photos : NavigationItem("photos", Icons.Outlined.Image, "Photos")
    object MintDetail : NavigationItem("mint", Icons.Outlined.AutoAwesome, "Mint")
    object MyMints : NavigationItem("mymints", Icons.Outlined.AutoAwesome, "My Mints")
    object MyMintsDetails : NavigationItem("mymintsdetails", Icons.Outlined.AutoAwesome, "My Mints Details")
}
