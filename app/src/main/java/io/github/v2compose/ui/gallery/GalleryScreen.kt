package io.github.v2compose.ui.gallery

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.v2compose.ui.common.BackIcon
import io.github.v2compose.ui.common.GalleryImage
import org.koin.androidx.compose.koinViewModel

private const val TAG = "GalleryScreen"

@Composable
fun GalleryScreenRoute(
    onBackClick: () -> Unit,
    viewModel: GalleryViewModel = koinViewModel(),
) {
    val screenArgs = viewModel.screenArgs

    GalleryScreen(
        currentPic = screenArgs.current,
        pics = screenArgs.pics,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GalleryScreen(currentPic: String, pics: List<String>, onBackClick: () -> Unit) {
    val initialPicIndex = remember(currentPic, pics) {
        maxOf(pics.indexOf(currentPic), 0)
    }
    Log.d(TAG, "initialPicIndex = $initialPicIndex")

//    LightSystemBarIcons()

    Scaffold(
        contentWindowInsets = WindowInsets(top = 0, bottom = 0),
        containerColor = Color(0xFF2F312E),
        contentColor = Color(0xFFF0F1EC),
    ) { contentPaddings ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPaddings)
        ) {
            GalleryImage(imageUrl = currentPic, onBackgroundClick = onBackClick)
            val contentColor = LocalContentColor.current
            CenterAlignedTopAppBar(
                title = {},
                navigationIcon = { BackIcon(onBackClick = onBackClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                    actionIconContentColor = contentColor,
                    titleContentColor = contentColor,
                    navigationIconContentColor = contentColor,
                )
            )
        }
    }
}

