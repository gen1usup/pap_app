package com.dadnavigator.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dadnavigator.app.data.local.dao.ChecklistDao
import com.dadnavigator.app.data.local.dao.ContractionDao
import com.dadnavigator.app.data.local.dao.EmergencyContactDao
import com.dadnavigator.app.data.local.dao.LaborDao
import com.dadnavigator.app.data.local.dao.SettingsDao
import com.dadnavigator.app.data.local.dao.TimelineDao
import com.dadnavigator.app.data.local.dao.TrackerDao
import com.dadnavigator.app.data.local.dao.UserDao
import com.dadnavigator.app.data.local.dao.WaterBreakDao
import com.dadnavigator.app.data.local.entity.ChecklistEntity
import com.dadnavigator.app.data.local.entity.ChecklistItemEntity
import com.dadnavigator.app.data.local.entity.ContractionEntity
import com.dadnavigator.app.data.local.entity.ContractionSessionEntity
import com.dadnavigator.app.data.local.entity.DiaperLogEntity
import com.dadnavigator.app.data.local.entity.EmergencyContactEntity
import com.dadnavigator.app.data.local.entity.FeedingLogEntity
import com.dadnavigator.app.data.local.entity.LaborSummaryEntity
import com.dadnavigator.app.data.local.entity.NoteEntity
import com.dadnavigator.app.data.local.entity.RoomConverters
import com.dadnavigator.app.data.local.entity.SettingsEntity
import com.dadnavigator.app.data.local.entity.SleepLogEntity
import com.dadnavigator.app.data.local.entity.TimelineEventEntity
import com.dadnavigator.app.data.local.entity.UserEntity
import com.dadnavigator.app.data.local.entity.WaterBreakEventEntity

/**
 * Room database for all offline application data.
 */
@Database(
    entities = [
        ContractionSessionEntity::class,
        ContractionEntity::class,
        WaterBreakEventEntity::class,
        TimelineEventEntity::class,
        ChecklistEntity::class,
        ChecklistItemEntity::class,
        FeedingLogEntity::class,
        DiaperLogEntity::class,
        SleepLogEntity::class,
        NoteEntity::class,
        SettingsEntity::class,
        UserEntity::class,
        LaborSummaryEntity::class,
        EmergencyContactEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contractionDao(): ContractionDao
    abstract fun waterBreakDao(): WaterBreakDao
    abstract fun timelineDao(): TimelineDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun trackerDao(): TrackerDao
    abstract fun userDao(): UserDao
    abstract fun settingsDao(): SettingsDao
    abstract fun laborDao(): LaborDao
    abstract fun emergencyContactDao(): EmergencyContactDao
}
