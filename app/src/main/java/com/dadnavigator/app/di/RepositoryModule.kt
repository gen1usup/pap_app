package com.dadnavigator.app.di

import com.dadnavigator.app.data.repository.ChecklistRepositoryImpl
import com.dadnavigator.app.data.repository.ContractionRepositoryImpl
import com.dadnavigator.app.data.repository.LaborRepositoryImpl
import com.dadnavigator.app.data.repository.SettingsRepositoryImpl
import com.dadnavigator.app.data.repository.TimelineRepositoryImpl
import com.dadnavigator.app.data.repository.TrackerRepositoryImpl
import com.dadnavigator.app.data.repository.UserRepositoryImpl
import com.dadnavigator.app.data.repository.WaterBreakRepositoryImpl
import com.dadnavigator.app.domain.repository.ChecklistRepository
import com.dadnavigator.app.domain.repository.ContractionRepository
import com.dadnavigator.app.domain.repository.LaborRepository
import com.dadnavigator.app.domain.repository.SettingsRepository
import com.dadnavigator.app.domain.repository.TimelineRepository
import com.dadnavigator.app.domain.repository.TrackerRepository
import com.dadnavigator.app.domain.repository.UserRepository
import com.dadnavigator.app.domain.repository.WaterBreakRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds repository interfaces to concrete implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindContractionRepository(impl: ContractionRepositoryImpl): ContractionRepository

    @Binds
    @Singleton
    abstract fun bindWaterBreakRepository(impl: WaterBreakRepositoryImpl): WaterBreakRepository

    @Binds
    @Singleton
    abstract fun bindChecklistRepository(impl: ChecklistRepositoryImpl): ChecklistRepository

    @Binds
    @Singleton
    abstract fun bindTimelineRepository(impl: TimelineRepositoryImpl): TimelineRepository

    @Binds
    @Singleton
    abstract fun bindTrackerRepository(impl: TrackerRepositoryImpl): TrackerRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindLaborRepository(impl: LaborRepositoryImpl): LaborRepository
}
