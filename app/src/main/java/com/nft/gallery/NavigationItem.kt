package com.nft.gallery

sealed class NavigationItem(var route: String, var icon: Int, var title: String) {
    object Camera: NavigationItem("camera", R.drawable.camera_icon, "Camera")
    object Photos : NavigationItem("photos", R.drawable.gallery_icon, "Photos")
    object Videos : NavigationItem("videos", R.drawable.video_folder_icon, "Videos")
}
