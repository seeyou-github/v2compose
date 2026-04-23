package io.github.v2compose.ui.topic

import androidx.lifecycle.SavedStateHandle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TopicNavigationTest {

    @Test
    fun parseTopicRoute_decodesTopicIdAndReplyFloor() {
        val route = topicRoute("hello world", 45)

        assertEquals(
            TopicNavigationRequest(topicId = "hello world", replyFloor = 45),
            parseTopicRoute(route),
        )
    }

    @Test
    fun parseTopicRoute_defaultsReplyFloorToZeroWhenMissing() {
        assertEquals(
            TopicNavigationRequest(topicId = "123", replyFloor = 0),
            parseTopicRoute("/t/123"),
        )
    }

    @Test
    fun parseTopicRoute_returnsNullForNonTopicRoute() {
        assertEquals(null, parseTopicRoute("/member/alice"))
    }

    @Test
    fun shouldReuseCurrentTopicRoute_returnsTrueForSameTopic() {
        assertTrue(
            shouldReuseCurrentTopicRoute(
                currentDestinationRoute = topicNavigationRoute,
                currentTopicId = "123",
                targetRoute = topicRoute("123", 45),
            )
        )
        assertTrue(
            shouldReuseCurrentTopicRoute(
                currentDestinationRoute = topicNavigationRoute,
                currentTopicId = "123",
                targetRoute = topicRoute("123"),
            )
        )
    }

    @Test
    fun shouldReuseCurrentTopicRoute_returnsFalseForDifferentTopic() {
        assertFalse(
            shouldReuseCurrentTopicRoute(
                currentDestinationRoute = topicNavigationRoute,
                currentTopicId = "123",
                targetRoute = topicRoute("456", 45),
            )
        )
    }

    @Test
    fun shouldReuseCurrentTopicRoute_returnsFalseWhenCurrentDestinationIsNotTopic() {
        assertFalse(
            shouldReuseCurrentTopicRoute(
                currentDestinationRoute = "/member/{userName}",
                currentTopicId = "123",
                targetRoute = topicRoute("123", 45),
            )
        )
    }

    @Test
    fun requestTopicRefresh_incrementsRefreshToken() {
        val savedStateHandle = SavedStateHandle()

        requestTopicRefresh(savedStateHandle)
        requestTopicRefresh(savedStateHandle)

        assertEquals(2, savedStateHandle.get<Int>(topicRefreshRequestKey))
    }
}
