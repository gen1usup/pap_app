package com.dadnavigator.app.di

import android.content.Context
import androidx.room.Room
import com.dadnavigator.app.data.local.AppDatabase
import com.dadnavigator.app.data.local.dao.ChecklistDao
import com.dadnavigator.app.data.local.dao.ContractionDao
import com.dadnavigator.app.data.local.dao.LaborDao
import com.dadnavigator.app.data.local.dao.SettingsDao
import com.dadnavigator.app.data.local.dao.TimelineDao
import com.dadnavigator.app.data.local.dao.TrackerDao
import com.dadnavigator.app.data.local.dao.UserDao
import com.dadnavigator.app.data.local.dao.WaterBreakDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides Room database and DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "dad_navigator.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideContractionDao(database: AppDatabase): ContractionDao = database.contractionDao()

    @Provides
    fun provideWaterBreakDao(database: AppDatabase): WaterBreakDao = database.waterBreakDao()

    @Provides
    fun provideTimelineDao(database: AppDatabase): TimelineDao = database.timelineDao()

    @Provides
    fun provideChecklistDao(database: AppDatabase): ChecklistDao = database.checklistDao()

    @Provides
    fun provideTrackerDao(database: AppDatabase): TrackerDao = database.trackerDao()

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    fun provideSettingsDao(database: AppDatabase): SettingsDao = database.settingsDao()

    @Provides
    fun provideLaborDao(database: AppDatabase): LaborDao = database.laborDao()
}
