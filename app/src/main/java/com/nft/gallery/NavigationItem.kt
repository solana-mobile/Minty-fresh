package com.nft.gallery

sealed class NavigationItem(var route: String, var icon: Int, var title: String) {
    object Photos : NavigationItem("photos", android.R.drawable.ic_menu_gallery, "Photos")
    object Videos : NavigationItem("videos", android.R.drawable.presence_video_online, "Videos")
}
