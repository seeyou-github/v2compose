package io.github.v2compose.ui.common

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.TargetBasedAnimation
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Precision
import coil3.size.Scale
import io.github.cooaer.htmltext.ExternalImageSizePolicy
import io.github.v2compose.LocalAppPlatformHandlers
import io.github.v2compose.usecase.ExternalImageUrlResolver
import io.github.v2compose.usecase.applyExternalImageRequestHeaders
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import v2compose.shared.generated.resources.Res
import v2compose.shared.generated.resources.save_image
import kotlin.math.ceil

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GalleryImage(
    imageUrl: String,
    onBackgroundClick: () -> Unit,
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val resolver = koinInject<ExternalImageUrlResolver>()
    val platformContext = LocalPlatformContext.current
    val resolvedImageUrl by produceState<String?>(initialValue = null, imageUrl, resolver) {
        value = resolver.resolve(imageUrl)
    }

    var viewWidth by remember { mutableFloatStateOf(0f) }
    var viewHeight by remember { mutableFloatStateOf(0f) }

    var offsetX by rememberSaveable { mutableFloatStateOf(0f) }
    var offsetY by rememberSaveable { mutableFloatStateOf(0f) }
    var scale by rememberSaveable { mutableFloatStateOf(1f) }

    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = minOf(maxOf(scale * zoomChange, 1f), 3f)

        val maxOffsetX = viewWidth * (scale - 1f) / 2
        val maxOffsetY = viewHeight * (scale - 1f) / 2
        offsetX = maxOf(minOf(offsetX + offsetChange.x, maxOffsetX), -maxOffsetX)
        offsetY = maxOf(minOf(offsetY + offsetChange.y, maxOffsetY), -maxOffsetY)
    }

    val visibleState = remember { MutableTransitionState(0.3f).apply { targetState = 1f } }
    val visibleTransition = rememberTransition(transitionState = visibleState, label = "visible")
    val currentAlpha by visibleTransition.animateFloat(
        transitionSpec = { tween(durationMillis = 400) },
        label = "alpha",
    ) { it }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = currentAlpha))
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    coroutineScope.launch {
                        visibleState.targetState = 0f
                        delay(400)
                        onBackgroundClick()
                    }
                },
                onDoubleClick = {
                    coroutineScope.launch {
                        val doubleClickAnim = TargetBasedAnimation(
                            animationSpec = tween(400),
                            typeConverter = Float.VectorConverter,
                            initialValue = 0f,
                            targetValue = 1f,
                        )
                        val startTime = withFrameNanos { it }
                        val startScale = scale
                        val startOffsetX = offsetX
                        val startOffsetY = offsetY

                        val endScale: Float
                        val endOffsetX: Float
                        val endOffsetY: Float
                        if (scale > 2f) {
                            endScale = 1f
                            endOffsetX = 0f
                            endOffsetY = 0f
                        } else {
                            endScale = scale + 1f
                            endOffsetX = offsetX
                            endOffsetY = offsetY
                        }

                        var playTime: Long
                        do {
                            playTime = withFrameNanos { it } - startTime
                            val progress = doubleClickAnim.getValueFromNanos(playTime)
                            scale = startScale + (endScale - startScale) * progress
                            offsetX = startOffsetX + (endOffsetX - startOffsetX) * progress
                            offsetY = startOffsetY + (endOffsetY - startOffsetY) * progress
                        } while (playTime < 400L * 1000L * 1000L)
                    }
                },
            )
            .transformable(transformableState),
    ) {
        val previewDecodeSize = remember(maxWidth, maxHeight, density) {
            ExternalImageSizePolicy.galleryPreviewDecodeSize(
                viewportWidthPx = with(density) { ceil(maxWidth.toPx()).toInt() },
                viewportHeightPx = with(density) { ceil(maxHeight.toPx()).toInt() },
            )
        }
        val imageRequest = remember(platformContext, resolvedImageUrl, previewDecodeSize) {
            resolvedImageUrl?.let { resolvedUrl ->
                applyExternalImageRequestHeaders(
                    ImageRequest.Builder(platformContext),
                    resolvedUrl,
                )
                    .data(resolvedUrl)
                    .size(width = previewDecodeSize.width, height = previewDecodeSize.height)
                    .precision(Precision.INEXACT)
                    .scale(Scale.FIT)
                    .build()
            }
        }

        LaunchedEffect(maxWidth, maxHeight) {
            viewWidth = with(density) { maxWidth.toPx() }
            viewHeight = with(density) { maxHeight.toPx() }
        }

        if (imageRequest == null) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.onSurface,
            )
        } else {
            AsyncImage(
                model = imageRequest,
                contentDescription = "current image",
                contentScale = ContentScale.Fit,
                alpha = currentAlpha,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY,
                        alpha = currentAlpha,
                    ),
            )
        }

        val platformHandlers = LocalAppPlatformHandlers.current
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .clickable(enabled = resolvedImageUrl != null) {
                    resolvedImageUrl?.let(platformHandlers::saveImage)
                }
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp),
                )
                .defaultMinSize(minWidth = 72.dp, minHeight = 32.dp)
                .padding(horizontal = 8.dp),
        ) {
            Text(
                stringResource(Res.string.save_image),
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
