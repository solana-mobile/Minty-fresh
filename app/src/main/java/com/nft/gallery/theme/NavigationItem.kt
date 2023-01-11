package com.nft.gallery.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.VideoCameraBack
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(var route: String, var icon: ImageVector, var title: String) {
    object Camera : NavigationItem("camera", Icons.Filled.Camera, "Camera")
    object Photos : NavigationItem("photos", Icons.Filled.PhotoAlbum, "Photos")
    object Videos : NavigationItem("videos", Icons.Filled.VideoCameraBack, "Videos")
    object MintDetail : NavigationItem("mint", Icons.Filled.Scanner, "Mint")
    object MyMints : NavigationItem("mymints", Icons.Filled.Scanner, "My Mints")
}
