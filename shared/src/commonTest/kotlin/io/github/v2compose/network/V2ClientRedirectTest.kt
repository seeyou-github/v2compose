package io.github.v2compose.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class V2ClientRedirectTest {

    @Test
    fun returns2faLocationForSigninRedirect() {
        assertEquals(
            "/2fa",
            resolveAuthRedirectEventLocation(
                requestUrl = "https://www.v2ex.com/signin",
                redirectLocation = "/2fa",
            ),
        )
    }

    @Test
    fun ignoresRedirectToSameAuthFlow() {
        assertNull(
            resolveAuthRedirectEventLocation(
                requestUrl = "https://www.v2ex.com/signin",
                redirectLocation = "/signin?next=%2Fmission%2Fdaily",
            ),
        )
    }

    @Test
    fun ignoresRedirectsFromNonV2exRequests() {
        assertNull(
            resolveAuthRedirectEventLocation(
                requestUrl = "https://example.com/signin",
                redirectLocation = "/2fa",
            ),
        )
    }
}
