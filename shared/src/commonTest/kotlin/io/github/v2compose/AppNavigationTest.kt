package io.github.v2compose

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppNavigationTest {

    @Test
    fun resolveOpenUri_navigatesToTopicReply() {
        val action = resolveOpenUri("/t/123#reply4")

        assertEquals(AppNavigationAction.Navigate(AppRoutes.topic("123", 4)), action)
    }

    @Test
    fun resolveOpenUri_navigatesToNode() {
        val action = resolveOpenUri("https://www.v2ex.com/go/compose")

        assertEquals(AppNavigationAction.Navigate(AppRoutes.node("compose")), action)
    }

    @Test
    fun resolveOpenUri_navigatesToUser() {
        val action = resolveOpenUri("/member/alice")

        assertEquals(AppNavigationAction.Navigate(AppRoutes.user("alice")), action)
    }

    @Test
    fun resolveOpenUri_routesExternalDomainToBrowser() {
        val action = resolveOpenUri("https://example.com/path?q=1")

        assertEquals(
            AppNavigationAction.External("https://example.com/path?q=1"),
            action,
        )
    }

    @Test
    fun resolveOpenUri_routesSystemSchemesToExternalHandler() {
        val actions = listOf(
            resolveOpenUri("mailto:test@example.com"),
            resolveOpenUri("sms:10086"),
            resolveOpenUri("tel:10010"),
        )

        assertEquals(AppNavigationAction.External("mailto:test@example.com"), actions[0])
        assertEquals(AppNavigationAction.External("sms:10086"), actions[1])
        assertEquals(AppNavigationAction.External("tel:10010"), actions[2])
    }

    @Test
    fun resolveRedirectLocation_clearsBackStackForRoot() {
        val action = resolveRedirectLocation("/")

        assertEquals(
            AppNavigationAction.Navigate(route = "/", clearBackStackToRoot = true),
            action,
        )
    }

    @Test
    fun resolveRedirectLocation_keepsSigninFlowInApp() {
        val signin = resolveRedirectLocation("/signin")
        val twoStep = resolveRedirectLocation("/2fa")

        assertEquals(AppNavigationAction.Navigate("/signin"), signin)
        assertEquals(AppNavigationAction.Navigate("/2fa"), twoStep)
    }

    @Test
    fun resolveRedirectLocation_ignoresUnsupportedInternalRoutes() {
        val action = resolveRedirectLocation("/balance")

        assertTrue(action is AppNavigationAction.Ignore)
    }
}
