# База данных и хранение

## Общая схема
Приложение использует:
- `DataStore` для настроек и текущего этапа
- `Room` для журнала, чек-листов, схваток, вод, labor summary и контактов

Текущая версия схемы: `7`

## DataStore
Хранит:
- `userId`
- `themeMode`
- `fatherName`
- `dueDate`
- `maternityHospitalAddress`
- `notificationsEnabled`
- `appStage`

## Основные таблицы Room
### `timeline_events`
Хранит журнал событий.

Поля:
- `id`
- `userId`
- `timestamp`
- `type`
- `title`
- `description`
- `stageAtCreation`
- `entryType`

### `checklists`
Корневые чек-листы.

Поля:
- `id`
- `userId`
- `stage`
- `title`
- `category`
- `isSystem`
- `isDeleted`

### `checklist_items`
Пункты чек-листов.

Поля:
- `id`
- `checklistId`
- `text`
- `isChecked`
- `sortOrder`

### `contraction_sessions`
Сессии схваток.

### `contractions`
Отдельные записи схваток.
Удаленная схватка физически убирается и перестает участвовать в расчетах.

### `water_break_events`
История вод.
Удаленная запись исчезает из истории и из recommendation policy.

### `labor_summary`
Краткие данные о родовом milestone и ребенке:
- старт родов
- время рождения
- имя ребенка
- вес
- рост

### `emergency_contacts`
Контакты и роддом.

## Миграция `6 -> 7`
Добавлено:
- `stageAtCreation` в `timeline_events`
- `entryType` в `timeline_events`
- `isDeleted` в `checklists`

Эта миграция также backfill-ит старые записи журнала в совместимый вид.
