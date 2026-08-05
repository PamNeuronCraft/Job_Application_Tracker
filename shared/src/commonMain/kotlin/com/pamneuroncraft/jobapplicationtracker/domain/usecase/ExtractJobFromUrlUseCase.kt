package com.pamneuroncraft.jobapplicationtracker.domain.usecase

import com.pamneuroncraft.jobapplicationtracker.domain.repository.JobExtractor
import com.pamneuroncraft.jobapplicationtracker.domain.repository.ExtractedJob

class ExtractJobFromUrlUseCase(
    private val extractor: JobExtractor
) {
    suspend operator fun invoke(url: String): ExtractedJob {
        return extractor.extractFromUrl(url)
    }
}
