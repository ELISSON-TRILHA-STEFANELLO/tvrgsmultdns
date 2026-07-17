package com.tvrgs.data.repository

import com.tvrgs.domain.model.ExternalRatings
import com.tvrgs.domain.model.ExternalRatingsLookup
import com.tvrgs.domain.model.Result
import com.tvrgs.domain.repository.ExternalRatingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExternalRatingsRepositoryImpl @Inject constructor() : ExternalRatingsRepository {

    override suspend fun getRatings(lookup: ExternalRatingsLookup): Result<ExternalRatings> {
        return Result.success(ExternalRatings.unavailable())
    }
}