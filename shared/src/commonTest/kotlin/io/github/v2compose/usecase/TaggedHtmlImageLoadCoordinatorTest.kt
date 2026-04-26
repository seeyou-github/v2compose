package io.github.v2compose.usecase

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TaggedHtmlImageLoadCoordinatorTest {

    @Test
    fun ignoresDuplicateLaunchForSameTagAndRequestWhileRunning() = runTest {
        val coordinator = TaggedHtmlImageLoadCoordinator()
        val blocker = CompletableDeferred<Unit>()
        val cancellationState = CompletableDeferred<Boolean>()
        val htmlUpdates = mutableListOf<String>()
        var invocationCount = 0

        try {
            val loader = object : HtmlImageLoader {
                override suspend fun loadHtmlImages(html: String, src: String?) = flow {
                    invocationCount += 1
                    try {
                        emit("loading-$invocationCount")
                        blocker.await()
                        emit("success-$invocationCount")
                    } catch (e: Exception) {
                        if (!cancellationState.isCompleted) {
                            cancellationState.complete(true)
                        }
                        throw e
                    } finally {
                        if (!cancellationState.isCompleted) {
                            cancellationState.complete(false)
                        }
                    }
                }
            }

            coordinator.launch(
                tag = "content",
                html = "same-html",
                imageSrc = null,
                scope = this,
                loader = loader,
                onHtmlUpdated = { htmlUpdates.add(it) },
            )

            advanceUntilIdle()

            coordinator.launch(
                tag = "content",
                html = "same-html",
                imageSrc = null,
                scope = this,
                loader = loader,
                onHtmlUpdated = { htmlUpdates.add(it) },
            )

            blocker.complete(Unit)
            advanceUntilIdle()

            assertEquals(1, invocationCount)
            assertEquals(listOf("loading-1", "success-1"), htmlUpdates)
            assertEquals(false, cancellationState.await())
        } finally {
            coordinator.cancelAll()
            blocker.complete(Unit)
        }
    }

    @Test
    fun cancelsPreviousLoadForSameTag() = runTest {
        val coordinator = TaggedHtmlImageLoadCoordinator()
        val firstCancelled = CompletableDeferred<Boolean>()
        val firstBlocker = CompletableDeferred<Unit>()
        val htmlUpdates = mutableListOf<String>()
        try {
            coordinator.launch(
                tag = "content",
                html = "first",
                imageSrc = null,
                scope = this,
                loader = object : HtmlImageLoader {
                    override suspend fun loadHtmlImages(html: String, src: String?) = flow {
                        try {
                            emit("first-loading")
                            firstBlocker.await()
                        } finally {
                            if (!firstCancelled.isCompleted) {
                                firstCancelled.complete(true)
                            }
                        }
                    }
                },
                onHtmlUpdated = { htmlUpdates.add(it) },
            )

            advanceUntilIdle()

            coordinator.launch(
                tag = "content",
                html = "second",
                imageSrc = null,
                scope = this,
                loader = object : HtmlImageLoader {
                    override suspend fun loadHtmlImages(html: String, src: String?) = flow {
                        emit("second-success")
                    }
                },
                onHtmlUpdated = { htmlUpdates.add(it) },
            )

            advanceUntilIdle()

            assertTrue(firstCancelled.await())
            assertEquals(listOf("first-loading", "second-success"), htmlUpdates)
        } finally {
            coordinator.cancelAll()
            firstBlocker.complete(Unit)
        }
    }

    @Test
    fun allowsDifferentTagsToLoadIndependently() = runTest {
        val coordinator = TaggedHtmlImageLoadCoordinator()
        val htmlUpdates = mutableListOf<String>()
        try {
            listOf("content", "reply#1").forEach { tag ->
                coordinator.launch(
                    tag = tag,
                    html = tag,
                    imageSrc = null,
                    scope = this,
                    loader = object : HtmlImageLoader {
                        override suspend fun loadHtmlImages(html: String, src: String?) = flow {
                            emit("$html-success")
                        }
                    },
                    onHtmlUpdated = { htmlUpdates.add(it) },
                )
            }

            advanceUntilIdle()

            assertTrue(
                htmlUpdates.contains("content-success"),
                "Expected content-success in $htmlUpdates",
            )
            assertTrue(
                htmlUpdates.contains("reply#1-success"),
                "Expected reply#1-success in $htmlUpdates",
            )
            assertEquals(2, htmlUpdates.size, "Unexpected updates: $htmlUpdates")
        } finally {
            coordinator.cancelAll()
        }
    }
}
