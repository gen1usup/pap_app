# Структура локального хранения

## Общая модель

Приложение использует два локальных источника:

- `Room` — основная оффлайн-база событий и доменных записей;
- `DataStore` — текущие пользовательские настройки и app-level state.

## Room

### База

- файл базы: `dad_navigator.db`
- текущая версия схемы: `4`
- база описана в [AppDatabase.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/data/local/AppDatabase.kt)

### Таблицы

#### `users`

Назначение:

- базовый локальный профиль пользователя.

Ключевые поля:

- `id`
- `displayName`
- `createdAt`

#### `settings`

Назначение:

- snapshot настроек для локальной согласованности и миграций.

Ключевые поля:

- `userId`
- `themeMode`
- `fatherName`
- `dueDateEpochDay`
- `maternityHospitalAddress`
- `notificationsEnabled`
- `appStage`
- `updatedAt`

#### `labor_summary`

Назначение:

- агрегированные данные о родах и ребенке.

Ключевые поля:

- `laborStartTime`
- `birthTime`
- `babyName`
- `birthWeightGrams`
- `birthHeightCm`

#### `contraction_sessions`

Назначение:

- одна пользовательская сессия отслеживания схваток.

#### `contractions`

Назначение:

- отдельные схватки внутри сессии.

#### `water_break_events`

Назначение:

- события отхождения вод и их состояние.

#### `timeline_events`

Назначение:

- единый журнал важных событий и ручных записей.

#### `checklists`

Назначение:

- заголовки и metadata чек-листов.

Ключевые поля включают:

- `stage`
- `category`
- `title`
- признак системного списка

#### `checklist_items`

Назначение:

- пункты чек-листа и их состояние.

#### `feeding_logs`, `diaper_logs`, `sleep_logs`, `notes`

Назначение:

- данные послеродовых трекеров.

#### `emergency_contacts`

Назначение:

- локальные контакты для звонков и экстренных сценариев.

## DataStore

DataStore хранит текущие значения настроек пользователя.

Ключевые значения:

- `userId`
- `themeMode`
- `fatherName`
- `dueDate`
- `maternityHospitalAddress`
- `notificationsEnabled`
- `appStage`

## Совместимость этапов

При чтении строкового значения этапа используется совместимость со старой моделью:

- `PREPARING -> PREPARING`
- `LABOR -> CONTRACTIONS`
- `AFTER_BIRTH -> AT_HOME`
- `AT_HOSPITAL -> AT_HOSPITAL`
- `AT_HOME -> AT_HOME`

## Почему используется и Room, и DataStore

`DataStore` нужен для простого и реактивного хранения настроек.

`Room` нужен для:

- событийной истории;
- сложных выборок;
- отношений сущностей;
- стабильного оффлайн-first сценария.

Такой split позволяет не смешивать длинную историю и lightweight настройки в одном механизме.
