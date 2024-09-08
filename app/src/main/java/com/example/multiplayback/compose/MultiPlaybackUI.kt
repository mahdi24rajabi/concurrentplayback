package com.example.multiplayback.compose

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.multiplayback.tools.PlayerProvider
import com.example.multiplayback.viewmodels.ContentsViewModel

data class PaneSize(
    val firstPane: Float = 3f,
    val secondPane: Float = 3f,
    val thirdPane: Float = 3f,
)

@OptIn(UnstableApi::class)
@Composable
fun VideoContainer(
    playerRepository: PlayerProvider,
    contentsViewModel: ContentsViewModel = viewModel()
) {
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

    val firstPanePlayer = remember {
        playerRepository.createPlayerAndPlay(contentsViewModel.firstPaneContent)
    }
    val middlePanePlayer = remember {
        playerRepository.createPlayerAndPlay(contentsViewModel.secondPaneContent)
    }
    val lastPanePlayer = remember {
        playerRepository.createPlayerAndPlay(contentsViewModel.thirdPaneContent)
    }

    var paneSize by remember {
        mutableStateOf(PaneSize())
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            firstPanePlayer.release()
            middlePanePlayer.release()
            lastPanePlayer.release()
        }
    }

    Row(
        modifier = Modifier.fillMaxSize().pointerInput(Unit){
            detectVerticalDragGestures { change, _ ->
                val direction = change.position.x - change.previousPosition.y
                paneSize = if(direction < 0){
                    if(paneSize.firstPane > 1 ){
                        paneSize.copy(
                            firstPane = paneSize.firstPane.minus(1),
                            thirdPane = paneSize.thirdPane.plus(1)
                        )
                    } else {
                        paneSize
                    }
                } else {
                    if(paneSize.thirdPane > 1 ){
                        paneSize.copy(
                            firstPane = paneSize.firstPane.plus(1),
                            thirdPane = paneSize.secondPane.minus(1)
                        )
                    } else {
                        paneSize
                    }
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .weight(paneSize.firstPane)
                .fillMaxHeight()
        ) {
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        this.player = firstPanePlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                        layoutParams = FrameLayout
                            .LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(
            modifier = Modifier
                .weight(paneSize.secondPane)
                .fillMaxHeight()
        ) {
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        this.player = middlePanePlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                        layoutParams = FrameLayout
                            .LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        Box(
            modifier = Modifier
                .weight(paneSize.thirdPane)
                .fillMaxHeight()
        ) {
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        this.player = lastPanePlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                        layoutParams = FrameLayout
                            .LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT,
                            )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

}
