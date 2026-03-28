package com.dadnavigator.app.domain.usecase.contraction

import com.dadnavigator.app.domain.repository.ContractionRepository
import javax.inject.Inject

class DeleteContractionUseCase @Inject constructor(
    private val contractionRepository: ContractionRepository
) {
    suspend operator fun invoke(contractionId: Long) {
        contractionRepository.deleteContraction(contractionId)
    }
}
