package io.github.v2compose.ui.main.notifications

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import io.github.v2compose.repository.AccountRepository
import io.github.v2compose.usecase.HtmlImageLoader
import io.github.v2compose.usecase.TaggedHtmlImageLoadCoordinator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class NotificationViewModel(
    private val accountRepository: AccountRepository,
    private val htmlImageLoader: HtmlImageLoader,
) : ViewModel() {

    val isLoggedIn = accountRepository.isLoggedIn
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            false
        )

    val unreadNotifications = accountRepository.unreadNotifications.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        0
    )

    val notifications = accountRepository.getNotifications().cachedIn(viewModelScope)

    val sizedHtmls = mutableStateMapOf<String, String>()
    private val htmlImageLoadCoordinator = TaggedHtmlImageLoadCoordinator()

    fun loadHtmlImage(tag: String, html: String, imageSrc: String?) {
        htmlImageLoadCoordinator.launch(
            tag = tag,
            html = html,
            imageSrc = imageSrc,
            scope = viewModelScope,
            loader = htmlImageLoader,
        ) { sizedHtmls[tag] = it }
    }
}
