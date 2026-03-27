# Локальная база данных и хранение

## Общая схема

Приложение использует два локальных механизма хранения:

- `Room` — событийные и справочные данные;
- `DataStore` — app-level настройки и сценарный контекст.

Текущая версия `Room` базы: `4`.

## Room: список таблиц

### 1. `contraction_sessions`

Назначение:

- хранит сессии наблюдения за схватками.

Основные поля:

- `id`
- `userId`
- `startedAt`
- `endedAt`

Связи:

- одна сессия -> много записей в `contractions`.

### 2. `contractions`

Назначение:

- хранит отдельные схватки внутри сессии.

Основные поля:

- `id`
- `sessionId`
- `userId`
- `startedAt`
- `endedAt`

Связи:

- `sessionId` -> `contraction_sessions.id`

### 3. `water_break_events`

Назначение:

- хранит факты отхождения вод и историю закрытия события.

Основные поля:

- `id`
- `userId`
- `happenedAt`
- `color`
- `notes`
- `closedAt`

### 4. `timeline_events`

Назначение:

- единая хронология для родов и послеродовых записей.

Основные поля:

- `id`
- `userId`
- `type`
- `timestamp`
- `title`
- `description`

Типы событий соответствуют `TimelineType`:

- `CONTRACTION`
- `WATER_BREAK`
- `LABOR`
- `BIRTH`
- `FEEDING`
- `DIAPER`
- `SLEEP`
- `NOTE`

### 5. `checklists`

Назначение:

- хранит контейнеры чек-листов.

Основные поля:

- `id`
- `userId`
- `title`
- `stage`
- `category`
- `isSystem`
- `sortOrder`
- `createdAt`

### 6. `checklist_items`

Назначение:

- хранит пункты чек-листов.

Основные поля:

- `id`
- `checklistId`
- `userId`
- `text`
- `isChecked`
- `createdAt`

Связи:

- `checklistId` -> `checklists.id`

### 7. `feeding_logs`

Назначение:

- записи по кормлению.

Основные поля:

- `id`
- `userId`
- `timestamp`
- `durationMinutes`
- `type`

### 8. `diaper_logs`

Назначение:

- записи по подгузникам.

Основные поля:

- `id`
- `userId`
- `timestamp`
- `type`
- `notes`

### 9. `sleep_logs`

Назначение:

- записи по сну ребенка.

Основные поля:

- `id`
- `userId`
- `startTime`
- `endTime`
- `notes`

### 10. `notes`

Назначение:

- произвольные заметки.

Основные поля:

- `id`
- `userId`
- `timestamp`
- `text`
- `category`

### 11. `settings`

Назначение:

- snapshot настроек для Room-слоя и возможного будущего sync.

Основные поля:

- `userId`
- `themeMode`
- `fatherName`
- `dueDateEpochDay`
- `maternityHospitalAddress`
- `notificationsEnabled`
- `appStage`
- `updatedAt`

### 12. `users`

Назначение:

- локальный профиль пользователя.

Основные поля:

- `id`
- `displayName`
- `createdAt`

### 13. `labor_summary`

Назначение:

- ключевые агрегированные данные о родах.

Основные поля:

- `userId`
- `laborStartTime`
- `birthTime`
- `babyName`
- `birthWeightGrams`
- `birthHeightCm`

### 14. `emergency_contacts`

Назначение:

- быстрые экстренные контакты.

Основные поля:

- `type`
- `title`
- `phone`
- `sortOrder`

## Индексы и производительность

Индексы используются на:

- `userId` для большинства пользовательских таблиц;
- `timestamp` / `startTime` для журналов и трекеров;
- `sessionId` для схваток;
- `checklistId` для пунктов чек-листа.

Это позволяет быстро строить:

- историю;
- последние события;
- активные сессии;
- stage-aware списки чек-листов.

## DataStore: структура настроек

DataStore хранит ключи:

- `user_id`
- `theme_mode`
- `father_name`
- `due_date_epoch_day`
- `maternity_hospital_address`
- `notifications_enabled`
- `app_stage`

## Источник правды

Для настроек источником правды считается `DataStore`.

`Room.settings` используется как локальный snapshot и подготовка к будущим сценариям синхронизации, но экранные настройки читаются через `SettingsDataStore`.

## Миграции

Текущие миграции определены в `DatabaseModule`.

Важные моменты:

- миграции пересоздают таблицу `settings`, чтобы сохранять консистентную схему;
- миграции учитывают предыдущие версии `checklists`;
- схема `labor_summary` расширялась полем `babyName`;
- база эволюционирует без намеренной потери пользовательских данных там, где это возможно.
