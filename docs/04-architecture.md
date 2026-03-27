# Архитектура проекта

## Общая схема

Проект построен по слоям:

- `presentation` — Compose UI, экранные состояния, navigation shell;
- `domain` — модели, сервисы, use case, интерфейсы репозиториев;
- `data` — Room, DataStore, entity, mapper, repository implementation;
- `di` — Hilt modules и wiring зависимостей.

## Текущее состояние миграции

Проект эволюционно переводится из screen-centric presentation в более feature-first организацию.

Сейчас фактическая структура смешанная:

- основные экраны по-прежнему живут в `presentation/screen/*`;
- navigation shell уже разнесен на отдельные файлы в `presentation/navigation`;
- stage-oriented экран вынесен в `presentation/feature/stages`;
- reusable UI элементы лежат в `presentation/component`.

Это осознанный промежуточный шаг: поведение сохраняется, а giant-file точки уже разрезаются на независимые части.

## Navigation shell

### Основные файлы

- [AppNavHost.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/presentation/navigation/AppNavHost.kt)
- [AppNavGraph.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/presentation/navigation/AppNavGraph.kt)
- [AppDestination.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/presentation/navigation/AppDestination.kt)
- [BottomNavigationConfig.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/presentation/navigation/BottomNavigationConfig.kt)
- [DrawerNavigationConfig.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/presentation/navigation/DrawerNavigationConfig.kt)
- [AppDrawerContent.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/presentation/navigation/AppDrawerContent.kt)

### Текущий shell

Bottom navigation:

- `dashboard`
- `events`
- `checklist`

Secondary destination через top bar:

- `journal`

Drawer:

- группа `Этапы`;
- группа `Сервис`.

## App-level state

Главный app-level state собирается в [AppViewModel.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/presentation/navigation/AppViewModel.kt).

Ключевые поля:

- `userId`
- `themeMode`
- `dueDate`
- `appStage`

Источник правды:

- `DataStore` для живых настроек;
- Room snapshot таблица `settings` для локальной согласованности и миграций.

## Domain orchestration

Новая stage/context логика вынесена в сервисы:

- [StageManager.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/domain/service/StageManager.kt)
- [StageTransitionManager.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/domain/service/StageTransitionManager.kt)
- [HomeContentBuilder.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/domain/service/HomeContentBuilder.kt)
- [EventsProvider.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/domain/service/EventsProvider.kt)

Назначение:

- `StageManager` считает derived-info по ПДР и этапу;
- `StageTransitionManager` централизует допустимые смены этапов;
- `HomeContentBuilder` определяет, что именно нужно показать на главной;
- `EventsProvider` собирает секции и действия экрана `События` в зависимости от этапа.

Это позволяет держать бизнес-правила вне composable и уменьшает дублирование в ViewModel.

## Presentation

Ключевые ViewModel:

- `DashboardViewModel`
- `EventsViewModel`
- `ChecklistViewModel`
- `ContractionViewModel`
- `WaterBreakViewModel`
- `LaborViewModel`
- `TimelineViewModel`
- `SettingsViewModel`

Типовой паттерн:

1. `ViewModel` наблюдает `Flow` из use case.
2. Собирает `UiState` через `combine`.
3. Делегирует действия в use case / service.
4. Composable только отображает state и отправляет intent.

## Persistence

### DataStore

Используется для пользовательских настроек и текущего этапа.

### Room

Используется для:

- сессий и истории схваток;
- событий вод;
- timeline / journal;
- checklist данных;
- трекеров;
- labor summary;
- контактов;
- snapshot настроек.

## Совместимость данных

Старая 3-stage модель не ломается при чтении:

- `LABOR -> CONTRACTIONS`
- `AFTER_BIRTH -> AT_HOME`

Это реализовано в [AppStage.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/domain/model/AppStage.kt) и используется mapper/data-store слоем.

## Тестовый контур

Unit tests живут в `app/src/test/java`.

Instrumented/UI tests живут в `app/src/androidTest/java` и проверяют:

- shell navigation;
- stage screen;
- home behavior;
- stage-aware сценарии `Событий`;
- журнал;
- валидацию настроек;
- сценарии аналитики схваток.
