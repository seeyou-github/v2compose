package io.github.v2compose.usecase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TaggedHtmlImageLoadCoordinator {
    private val jobs = mutableMapOf<String, TaggedLoadJob>()

    fun launch(
        tag: String,
        html: String,
        imageSrc: String?,
        scope: CoroutineScope,
        loader: HtmlImageLoader,
        onHtmlUpdated: (String) -> Unit,
    ) {
        val request = TaggedLoadRequest(
            html = html,
            imageSrc = imageSrc,
        )
        val existing = jobs[tag]
        if (existing != null && existing.job.isActive && existing.request == request) {
            return
        }

        existing?.job?.cancel()
        val job = scope.launch {
            try {
                loader.loadHtmlImages(html, imageSrc).collectLatest(onHtmlUpdated)
            } finally {
                if (jobs[tag]?.job === this.coroutineContext[Job]) {
                    jobs.remove(tag)
                }
            }
        }
        jobs[tag] = TaggedLoadJob(
            request = request,
            job = job,
        )
    }

    fun cancelAll() {
        jobs.values.forEach { it.job.cancel() }
        jobs.clear()
    }
}

private data class TaggedLoadRequest(
    val html: String,
    val imageSrc: String?,
)

private data class TaggedLoadJob(
    val request: TaggedLoadRequest,
    val job: Job,
)
