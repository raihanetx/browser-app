package com.example.browser.domain.usecase

import com.example.browser.domain.repository.HistoryRepository
import javax.inject.Inject

class RecordVisitUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(url: String, title: String) {
        if (url.isBlank() || url == "about:blank") return
        historyRepository.recordVisit(url, title)
    }
}
