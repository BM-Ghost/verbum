package com.verbum.feature.missal.domain.contract

import com.verbum.feature.missal.domain.contract.model.MissalReading

interface MissalRepositoryContract {
    suspend fun getReading(date: String): MissalReading
}
