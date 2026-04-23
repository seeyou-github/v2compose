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
    fun resolveOpenUri_routesProtocolRelativeExternalDomainToBrowser() {
        val action = resolveOpenUri("//example.com/path?q=1")

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
    fun resolveOpenUri_routesSigninPathToWebView() {
        val action = resolveOpenUri("/signin")

        assertEquals(
            AppNavigationAction.Navigate(
                AppRoutes.webView("https://www.v2ex.com/signin"),
            ),
            action,
        )
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

        assertEquals(AppNavigationAction.Navigate(authSigninRoute), signin)
        assertEquals(AppNavigationAction.Navigate(authTwoStepRoute), twoStep)
    }

    @Test
    fun resolveRedirectLocation_normalizesSigninChildPath() {
        val action = resolveRedirectLocation("/signin/cooldown?next=%2Fmission%2Fdaily")

        assertEquals(
            AppNavigationAction.Navigate("$authSigninRoute?next=%2Fmission%2Fdaily"),
            action,
        )
    }

    @Test
    fun resolveRedirectLocation_routesTopicReplyIntoTopicScreen() {
        val action = resolveRedirectLocation("/t/1207974?p=1#reply36")

        assertEquals(
            AppNavigationAction.Navigate(AppRoutes.topic("1207974", 36)),
            action,
        )
    }

    @Test
    fun resolveRedirectLocation_routesNodeIntoNodeScreen() {
        val action = resolveRedirectLocation("/go/compose")

        assertEquals(
            AppNavigationAction.Navigate(AppRoutes.node("compose")),
            action,
        )
    }

    @Test
    fun resolveRedirectLocation_routesUserIntoUserScreen() {
        val action = resolveRedirectLocation("/member/alice")

        assertEquals(
            AppNavigationAction.Navigate(AppRoutes.user("alice")),
            action,
        )
    }

    @Test
    fun resolveRedirectLocation_routesUnsupportedInternalRoutesToFallback() {
        val action = resolveRedirectLocation("/balance")

        assertEquals(
            AppNavigationAction.Navigate(AppRoutes.unsupported("/balance")),
            action,
        )
    }

    @Test
    fun authFlowRouteKey_matchesAuthRoutes() {
        assertEquals(authSigninRoute, authFlowRouteKey("/signin?next=%2Fmission%2Fdaily"))
        assertEquals(authSigninRoute, authFlowRouteKey("https://www.v2ex.com/signin"))
        assertEquals(authTwoStepRoute, authFlowRouteKey("/2fa"))
    }

    @Test
    fun isSameAuthFlow_matchesEquivalentSigninRoutes() {
        assertTrue(
            isSameAuthFlow(
                "https://www.v2ex.com/signin?next=%2Fmission%2Fdaily",
                "/signin",
            )
        )
    }

    @Test
    fun shouldIgnoreRepeatedAuthNavigation_onlyForSameAuthFlow() {
        assertTrue(shouldIgnoreRepeatedAuthNavigation("/signin?next={next}", "/signin"))
        assertTrue(shouldIgnoreRepeatedAuthNavigation("/2fa", "/2fa?once=1"))
        assertTrue(!shouldIgnoreRepeatedAuthNavigation("/", "/signin"))
        assertTrue(!shouldIgnoreRepeatedAuthNavigation("/signin?next={next}", "/2fa"))
    }
}
