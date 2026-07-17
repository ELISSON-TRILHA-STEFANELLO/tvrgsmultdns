package com.tvrgs.data.remote.stalker

import com.tvrgs.domain.model.StalkerBootstrapRecipe
import com.tvrgs.domain.model.StalkerCookieMode
import com.tvrgs.domain.model.StalkerEndpointPreference
import com.tvrgs.domain.model.StalkerMagPreset
import com.tvrgs.domain.model.StalkerPlaybackBackendHint
import com.tvrgs.domain.model.StalkerPortalFingerprint
import java.io.IOException

class StalkerPlaybackResolutionException(
    message: String,
    cause: Throwable? = null,
    val streamKind: StalkerStreamKind = StalkerStreamKind.LIVE,
    val portalFingerprint: StalkerPortalFingerprint? = null,
    val magPreset: StalkerMagPreset? = null,
    val bootstrapRecipe: StalkerBootstrapRecipe? = null,
    val endpointPreference: StalkerEndpointPreference = StalkerEndpointPreference.AUTO,
    val cookieMode: StalkerCookieMode = StalkerCookieMode.NONE,
    val playbackBackendHint: StalkerPlaybackBackendHint = StalkerPlaybackBackendHint.AUTO,
    val fallbackRecipeUsed: Boolean = false,
    val rediscoveryAttempted: Boolean = false
) : IOException(message, cause)
