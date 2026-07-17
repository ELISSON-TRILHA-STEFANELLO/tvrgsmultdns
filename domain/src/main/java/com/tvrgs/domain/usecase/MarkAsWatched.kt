package com.tvrgs.domain.usecase

import com.tvrgs.domain.model.ContentType
import com.tvrgs.domain.model.PlaybackHistory
import com.tvrgs.domain.model.Result
import com.tvrgs.domain.repository.PlaybackHistoryRepository
import com.tvrgs.domain.util.isPlaybackComplete
import javax.inject.Inject

class MarkAsWatched @Inject constructor(
    private val playbackHistoryRepository: PlaybackHistoryRepository
) {
    suspend operator fun invoke(history: PlaybackHistory): Result<Unit> {
        val normalizedHistory = if (
            history.contentType != ContentType.LIVE &&
            isPlaybackComplete(history.resumePositionMs, history.totalDurationMs)
        ) {
            history.copy(
                resumePositionMs = history.totalDurationMs.coerceAtLeast(history.resumePositionMs),
                lastWatchedAt = System.currentTimeMillis()
            )
        } else {
            history
        }
        return playbackHistoryRepository.markAsWatched(normalizedHistory)
    }
}