# Архитектура проекта

## Общая схема

Проект построен по схеме:

- `presentation` — Compose UI, navigation, screen state;
- `domain` — модели, use cases, repository interfaces;
- `data` — Room, DataStore, repository implementations, mappers;
- `di` — Hilt-модули.

## Структура слоев

### Presentation

Основные каталоги:

- `presentation/navigation`
- `presentation/screen/*`
- `presentation/component`

Ответственность:

- composable-экраны;
- сборка экранных состояний;
- обработка пользовательских действий;
- навигация между разделами;
- показ snackbar, bottom sheet, диалогов.

### Domain

Основные каталоги:

- `domain/model`
- `domain/repository`
- `domain/usecase/*`

Ответственность:

- бизнес-модели;
- интерфейсы репозиториев;
- отдельные сценарии изменения и чтения данных.

### Data

Основные каталоги:

- `data/local`
- `data/repository`
- `data/mapper`

Ответственность:

- локальное хранение;
- миграции Room;
- преобразование entity <-> domain;
- реализация repository interface.

## Навигация

### Top-level навигация

`AppNavHost` управляет shell приложения.

Top-level destinations:

- `dashboard`
- `events`
- `checklist`
- `journal`

Для них используется единая логика перехода с `restoreState`, `saveState` и `launchSingleTop`, чтобы bottom navigation и карточки на главной работали консистентно.

### Secondary destinations

- `contraction`
- `water_break`
- `decision`
- `sos`
- `emergency_contacts`
- `mom_support`
- `labor`
- `postpartum`
- `trackers`
- `help`
- `about`
- `settings`

## Состояние приложения

Ключевой app-level state:

- `Settings`
- `AppStage`
- `ThemeMode`

Источники:

- `DataStore` — источник правды для настроек;
- `Room` — локальная база для событий и зеркала части настроек.

## ViewModel-подход

Каждый экран или крупный сценарий использует свой `ViewModel`.

Примеры:

- `DashboardViewModel`
- `EventsViewModel`
- `ChecklistViewModel`
- `ContractionViewModel`
- `WaterBreakViewModel`
- `LaborViewModel`
- `SettingsViewModel`
- `SosViewModel`
- `HelpViewModel`

Типовой паттерн:

1. `ViewModel` подписывается на `Flow`.
2. Собирает `UiState` через `combine`.
3. Вызывает `use case` для действий.
4. UI только отображает состояние и отправляет intent.

## Dependency Injection

Используется `Hilt`.

Основные модули:

- `RepositoryModule`
- `DatabaseModule`
- другие Hilt bindings по мере необходимости

## Подход к данным

### Room

Хранит:

- сессии и схватки;
- события отхождения вод;
- журнал;
- чек-листы;
- трекеры;
- контакты;
- labor summary;
- snapshot-настройки;
- профиль пользователя.

### DataStore

Хранит:

- `userId`
- `themeMode`
- `fatherName`
- `dueDate`
- `maternityHospitalAddress`
- `notificationsEnabled`
- `appStage`

## Принципы проекта

- Не класть бизнес-логику в composable.
- Не дублировать доменную логику между экранами.
- Не завязывать UX на сеть.
- Сохранять эволюционную доработку поверх текущей кодовой базы, а не переписывать все с нуля.
