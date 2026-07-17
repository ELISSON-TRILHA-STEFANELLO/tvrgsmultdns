package com.tvrgs.domain.repository

import com.tvrgs.domain.model.ExternalRatings
import com.tvrgs.domain.model.ExternalRatingsLookup
import com.tvrgs.domain.model.Result

interface ExternalRatingsRepository {
    suspend fun getRatings(lookup: ExternalRatingsLookup): Result<ExternalRatings>
}