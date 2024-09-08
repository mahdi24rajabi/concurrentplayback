package com.example.multiplayback.tools

import android.app.Application
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import com.example.multiplayback.uimodels.ContentUIModel
import dagger.Provides
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerProvider @OptIn(UnstableApi::class)
@Inject constructor(
    private val context: Application,
) {

    val playbackErrorState: StateFlow<String?> = MutableStateFlow<String?>(null)

    @OptIn(UnstableApi::class)
    fun createPlayerAndPlay(
        contentUIModel: ContentUIModel
    ) = ExoPlayer.Builder(context, provideRendererFactory(context))
        .setMediaSourceFactory(provideMediaSourceFactory(context))
        .setLoadControl(
            DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    5_000,
                    10_000,
                    1_500,
                    3_000
                )
                .build()
        ).build().apply {
            addListener(
                object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        super.onPlaybackStateChanged(playbackState)
                        (playbackErrorState as MutableStateFlow).value = null
                        when(playbackState){
                            Player.STATE_ENDED -> {
                                this@apply.seekTo(0)
                                this@apply.playWhenReady = true
                            }
                            else -> {}
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        super.onPlayerError(error)
                        (playbackErrorState as MutableStateFlow).value = "Error!"
                    }


                }
            )
            startContent(contentUIModel)
        }

    @OptIn(UnstableApi::class)
    private fun provideRendererFactory(
        context: Application,
    ): RenderersFactory = DefaultRenderersFactory(context).apply {
        setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
        setEnableDecoderFallback(true)
    }


    private fun provideMediaSourceFactory(
        context: Application
    ): MediaSource.Factory = DefaultMediaSourceFactory(context)

    private fun ExoPlayer.startContent(contentUIModel: ContentUIModel) {
        setMediaItem(
            MediaItem.Builder().setUri(contentUIModel.contentUrl).build(), 0
        )
        playWhenReady = true
        prepare()
    }

    fun release(player: Player){
        player.release()
    }
}