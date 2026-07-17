package com.tvrgs.app.navigation

import com.tvrgs.domain.model.PlaybackHistory

internal fun PlaybackHistory.toPlayerNavigationRequest(): PlayerNavigationRequest =
    PlayerNavigationRequest(
        streamUrl = streamUrl,
        title = title,
        internalId = contentId,
        providerId = providerId,
        contentType = contentType.name,
        artworkUrl = posterUrl,
        seriesId = seriesId,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber
    )