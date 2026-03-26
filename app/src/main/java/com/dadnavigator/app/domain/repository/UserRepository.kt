package com.dadnavigator.app.domain.repository

import com.dadnavigator.app.domain.model.AppUser
import kotlinx.coroutines.flow.Flow

/**
 * Contract for future user profile storage and synchronization.
 */
interface UserRepository {
    fun observeCurrentUser(): Flow<AppUser>
}
