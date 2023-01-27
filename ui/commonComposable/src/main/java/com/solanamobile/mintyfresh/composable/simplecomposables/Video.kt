package com.solanamobile.mintyfresh.composable.simplecomposables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.solanamobile.mintyfresh.composable.viewmodel.Media

@Composable
fun VideoView(media: Media) {
    val context = LocalContext.current

    val exoPlayer = ExoPlayer.Builder(context)
        .build()
        .also { exoPlayer ->
            val mediaItem = MediaItem.Builder()
                .setUri(media.path)
                .build()
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            exoPlayer.prepare()
            exoPlayer.volume = 0f
            exoPlayer.repeatMode = Player.REPEAT_MODE_OFF
            exoPlayer.play()
        }

    DisposableEffect(
        AndroidView(
            modifier = Modifier
                .width(76.dp)
                .aspectRatio(1.0f)
                .clip(RoundedCornerShape(8.dp))
                .background(color = MaterialTheme.colorScheme.surface),
            factory = {
                StyledPlayerView(context).apply {
                    player = exoPlayer
                }.also { player ->
                    player.useController = false
                }
            }
        )
    ) {
        onDispose { exoPlayer.release() }
    }
}
