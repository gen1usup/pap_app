# Структура данных

## 1. Технологии хранения

Приложение использует:

- `Room` для событий, истории, чек-листов, labor summary, трекеров и контактов
- `DataStore` для пользовательских настроек и app-level stage

## 2. Room database

Главная база: [AppDatabase.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/data/local/AppDatabase.kt)

Актуальная версия схемы: `6`

Ключевые миграции описаны в [DatabaseModule.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/di/DatabaseModule.kt).

## 3. Основные таблицы

### `contraction_sessions`

Хранит сессии схваток.

Ключевые поля:

- `id`
- `userId`
- `startedAt`
- `endedAt`

### `contractions`

Хранит отдельные схватки внутри сессии.

Ключевые поля:

- `id`
- `sessionId`
- `userId`
- `startedAt`
- `endedAt`

### `water_break_events`

Хранит события вод.

Ключевые поля:

- `id`
- `userId`
- `startedAt`
- `endedAt`
- `color`
- `note`

### `timeline_events`

Хранит журнал событий.

Ключевые поля:

- `id`
- `userId`
- `type`
- `title`
- `description`
- `occurredAt`

Типы включают как milestone-события, так и stage-specific notes.

### `checklists`

Хранит корневые списки.

Ключевые поля:

- `id`
- `userId`
- `title`
- `stage`
- `category`
- `isSystem`
- `sortOrder`
- `createdAt`

### `checklist_items`

Хранит пункты чек-листов.

Ключевые поля:

- `id`
- `checklistId`
- `userId`
- `text`
- `note`
- `quantity`
- `priority`
- `metadataJson`
- `isChecked`
- `createdAt`

Расширенные поля уже добавлены для будущего richer item model.

### `labor_summary`

Хранит агрегированное состояние родового / послеродового сценария.

Ключевые поля:

- `userId`
- `laborStartTime`
- `birthTime`
- `babyName`
- `birthWeightGrams`
- `birthHeightCm`

### `emergency_contacts`

Хранит пользовательские контакты.

Ключевые поля:

- `id`
- `type`
- `title`
- `phone`
- `address`
- `sortOrder`
- `isDefault`

Поддерживаются роли:

- `EMERGENCY`
- `WIFE`
- `DOCTOR`
- `HOSPITAL`
- `TAXI`
- `RELATIVE`
- `CUSTOM`

## 4. DataStore settings

Основная модель: [Settings.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/domain/model/Settings.kt)

Ключевые поля:

- `userId`
- `themeMode`
- `fatherName`
- `dueDate`
- `notificationsEnabled`
- `appStage`
- `maternityHospitalAddress`

Важно:

- поле `maternityHospitalAddress` оставлено для совместимости старых данных
- фактический маршрут и адрес роддома теперь берутся из `emergency_contacts`

## 5. Совместимость и миграции

Актуальные migration paths:

- `1 -> 3`
- `2 -> 3`
- `3 -> 4`
- `4 -> 5`
- `5 -> 6`
- `4 -> 6`

Что делают последние миграции:

- `4 -> 5` добавляет расширенные поля в `checklist_items`
- `5 -> 6` переводит `emergency_contacts` на динамическую модель с `id`, `address` и `isDefault`
- `4 -> 6` обеспечивает прямой безопасный upgrade-path для уже существующих установок
