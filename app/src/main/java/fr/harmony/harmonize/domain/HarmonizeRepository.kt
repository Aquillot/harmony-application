package fr.harmony.harmonize.domain

import fr.harmony.harmonize.data.HarmonizeResponse

interface HarmonizeRepository {
    suspend fun uploadImages(
        original: java.io.File,
        harmonized: java.io.File
    ): Result<HarmonizeResponse>
}
