package io.github.v2compose.usecase

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

class FixHtmlUseCaseHtmlRewriteTest {

    @Test
    fun rewritesImgurPageImageSourceToResolvedDirectUrl() = runTest {
        val html = """<div><img src="https://imgur.com/Ug1iMq4"></div>"""
        val resolver = object : ExternalImageUrlResolver {
            override suspend fun resolve(rawUrl: String): String {
                return "https://i.imgur.com/Ug1iMq4.png"
            }
        }

        val rewritten = rewriteHtmlImageSources(html, resolver)

        assertContains(rewritten, """src="https://i.imgur.com/Ug1iMq4.png"""")
        assertFalse(rewritten.contains("""src="https://imgur.com/Ug1iMq4""""))
    }

    @Test
    fun keepsNonImgurImageSourceUntouched() = runTest {
        val html = """<div><img src="https://example.com/image.png"></div>"""
        val resolver = object : ExternalImageUrlResolver {
            override suspend fun resolve(rawUrl: String): String = rawUrl
        }

        val rewritten = rewriteHtmlImageSources(html, resolver)

        assertContains(rewritten, """src="https://example.com/image.png"""")
    }
}
