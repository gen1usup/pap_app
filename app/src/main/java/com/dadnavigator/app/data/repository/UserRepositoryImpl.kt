package com.dadnavigator.app.data.repository

import com.dadnavigator.app.data.local.dao.UserDao
import com.dadnavigator.app.data.local.entity.UserEntity
import com.dadnavigator.app.data.mapper.toDomain
import com.dadnavigator.app.domain.model.AppUser
import com.dadnavigator.app.domain.model.DEFAULT_USER_ID
import com.dadnavigator.app.domain.repository.UserRepository
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed user repository with local fallback profile.
 */
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override fun observeCurrentUser(): Flow<AppUser> {
        return userDao.observeUser(DEFAULT_USER_ID).map { entity ->
            entity?.toDomain() ?: UserEntity(
                id = DEFAULT_USER_ID,
                displayName = "",
                createdAt = Instant.EPOCH
            ).toDomain()
        }
    }
}
