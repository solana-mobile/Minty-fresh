package com.nft.gallery.theme

import com.nft.gallery.R

sealed class NavigationItem(var route: String, var icon: Int, var title: String) {
    object Camera : NavigationItem("camera", R.drawable.add_a_photo, "Camera")
    object Photos : NavigationItem("photos", R.drawable.image_icon, "Photos")
    object MintDetail : NavigationItem("mint", R.drawable.auto_awesome, "Mint")
    object MyMints : NavigationItem("mymints", R.drawable.auto_awesome, "My Mints")
    object MyMintsDetails : NavigationItem("mymintsdetails", R.drawable.auto_awesome, "My Mints Details")
}
