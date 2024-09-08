package com.example.multiplayback.compose

import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

    val errorMessage by
        playerRepository.playbackErrorState.collectAsStateWithLifecycle(null)

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

    Column(modifier = Modifier.fillMaxSize()) {
        if(errorMessage != null) {
            Text(
                errorMessage!!,
                color = Color.White,
                fontSize = TextUnit(12f, TextUnitType.Sp),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .background(Color.Red)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, _ ->
                        val direction = change.position.x - change.previousPosition.y
                        paneSize = if (direction < 0) {
                            if (paneSize.firstPane > 1) {
                                paneSize.copy(
                                    firstPane = paneSize.firstPane.minus(1),
                                    thirdPane = paneSize.thirdPane.plus(1)
                                )
                            } else {
                                paneSize
                            }
                        } else {
                            if (paneSize.thirdPane > 1) {
                                paneSize.copy(
                                    firstPane = paneSize.firstPane.plus(1),
                                    thirdPane = paneSize.thirdPane.minus(1)
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

}
