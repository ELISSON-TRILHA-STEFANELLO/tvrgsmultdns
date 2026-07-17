package com.tvrgs.app.util

import com.tvrgs.domain.util.isPlaybackComplete as domainIsPlaybackComplete

fun isPlaybackComplete(
    progressMs: Long,
    totalDurationMs: Long,
    threshold: Float = com.tvrgs.domain.util.DEFAULT_PLAYBACK_COMPLETION_THRESHOLD
): Boolean = domainIsPlaybackComplete(progressMs, totalDurationMs, threshold)
