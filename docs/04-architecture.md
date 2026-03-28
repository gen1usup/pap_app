# Архитектура

## Слои
### Presentation
Compose-экраны, view model и navigation shell.

Ключевые экраны:
- `DashboardScreen`
- `EventsScreen`
- `ContractionScreen` как экран `Активные роды`
- `ChecklistScreen`
- `EmergencyContactsScreen`
- `TimelineScreen`
- `BabyScreen`

### Domain
Здесь живут:
- модели этапов и событий
- use case
- policy и builder-слой

Ключевые сервисы:
- `StageManager`
- `StageTransitionManager`
- `HomeContentBuilder`
- `EventsProvider`
- `ActiveLaborRecommendationPolicy`
- `LiveContractionSnapshotBuilder`

### Data
- `Room`
- `DataStore`
- repository-реализации
- mapper-ы

## Stage architecture
Текущая модель этапов:
- `PREPARING`
- `LABOR`
- `BABY_BORN`

Legacy mapping для старых данных:
- `CONTRACTIONS` и `LABOR` -> `LABOR`
- `AT_HOSPITAL`, `AT_HOME`, `AFTER_BIRTH` -> `BABY_BORN`

`StageTransitionManager` централизует правила:
- схватка стартовала -> `LABOR`
- воды записаны -> `LABOR`
- рождение записано -> `BABY_BORN`
- ручной переход через preview screen остается доступным

## Active labor architecture
Единый экран `Активные роды` не дублирует расчет.

Источник правды:
- `ToggleContractionUseCase` — старт или стоп схватки
- `CalculateContractionStatsUseCase` — расчет статистики
- `LiveContractionSnapshotBuilder` — подготовка live-состояния
- `ActiveLaborRecommendationPolicy` — объединение схваток, вод и рождения

Главная и экран `Активные роды` используют одну и ту же recommendation policy.

## Journal architecture
Модель журнала теперь хранит:
- `timestamp`
- `stageAtCreation`
- `entryType`
- `type`
- `title`
- `description`

Фильтрация вынесена в `TimelineFilterPolicy`.
Это позволяет:
- фильтровать по этапу создания
- отдельно показывать только пользовательские заметки

## Checklist architecture
Системные и пользовательские чек-листы живут в одной структуре.

Ключевые правила:
- у чек-листа есть stage-привязка
- удаление системного чек-листа — soft delete
- seed дефолтных чек-листов больше не восстанавливает удаленные записи автоматически

## Contacts architecture
Контакты — top-level feature.

Содержимое:
- обязательные базовые записи
- пользовательские контакты
- роддом с адресом и телефоном

`Home` открывает `Contacts`, а сами действия звонка и маршрута остаются внутри feature-кода контактов.
