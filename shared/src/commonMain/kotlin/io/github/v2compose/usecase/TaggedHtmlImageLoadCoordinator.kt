package io.github.v2compose.usecase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TaggedHtmlImageLoadCoordinator {
    private val jobs = mutableMapOf<String, Job>()

    fun launch(
        tag: String,
        html: String,
        imageSrc: String?,
        scope: CoroutineScope,
        loader: HtmlImageLoader,
        onHtmlUpdated: (String) -> Unit,
    ) {
        jobs.remove(tag)?.cancel()
        jobs[tag] = scope.launch {
            try {
                loader.loadHtmlImages(html, imageSrc).collectLatest(onHtmlUpdated)
            } finally {
                jobs.remove(tag)
            }
        }
    }

    fun cancelAll() {
        jobs.values.forEach(Job::cancel)
        jobs.clear()
    }
}
