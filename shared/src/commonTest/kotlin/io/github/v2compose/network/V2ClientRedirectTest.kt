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
    fun returnsRootLocationForSigninSuccessRedirect() {
        assertEquals(
            "/",
            resolveAuthRedirectEventLocation(
                requestUrl = "https://www.v2ex.com/signin?next=%2Fmission%2Fdaily",
                redirectLocation = "/",
            ),
        )
    }

    @Test
    fun returnsNextLocationForSigninSuccessRedirect() {
        assertEquals(
            "/t/123?p=1",
            resolveAuthRedirectEventLocation(
                requestUrl = "https://www.v2ex.com/signin?next=%2Ft%2F123%3Fp%3D1",
                redirectLocation = "/t/123?p=1",
            ),
        )
    }

    @Test
    fun returnsSigninLocationForExpiredSessionRedirect() {
        assertEquals(
            "/signin?next=%2Fmission%2Fdaily",
            resolveAuthRedirectEventLocation(
                requestUrl = "https://www.v2ex.com/mission/daily/redeem?once=12027",
                redirectLocation = "/signin?next=%2Fmission%2Fdaily",
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
    fun ignoresMissionDailyBusinessRedirect() {
        assertNull(
            resolveAuthRedirectEventLocation(
                requestUrl = "https://www.v2ex.com/mission/daily/redeem?once=12027",
                redirectLocation = "/mission/daily",
            ),
        )
    }

    @Test
    fun ignoresUserActionRedirect() {
        assertNull(
            resolveAuthRedirectEventLocation(
                requestUrl = "https://www.v2ex.com/unblock/123",
                redirectLocation = "/member/alice",
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
