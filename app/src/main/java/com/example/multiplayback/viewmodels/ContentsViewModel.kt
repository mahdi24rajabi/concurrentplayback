package com.example.multiplayback.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.multiplayback.uimodels.ContentUIModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ContentsViewModel @Inject constructor() : ViewModel() {
    val firstPaneContent = ContentUIModel(
        contentUrl = Uri.parse("https://dash.akamaized.net/akamai/bbb_30fps/bbb_30fps.mpd"),
    )
    val secondPaneContent = ContentUIModel(
        contentUrl = Uri.parse("https://dash.akamaized.net/akamai/bbb_30fps/bbb_30fps.mpd"),
    )
    val thirdPaneContent = ContentUIModel(
        contentUrl = Uri.parse("https://dash.akamaized.net/akamai/bbb_30fps/bbb_30fps.mpd"),
    )
}