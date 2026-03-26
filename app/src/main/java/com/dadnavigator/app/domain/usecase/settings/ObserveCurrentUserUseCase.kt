package com.dadnavigator.app.domain.usecase.settings

import com.dadnavigator.app.domain.model.AppUser
import com.dadnavigator.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Reads current local user profile.
 */
class ObserveCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<AppUser> = userRepository.observeCurrentUser()
}
