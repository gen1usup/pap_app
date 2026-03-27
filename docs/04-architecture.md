# Архитектура проекта

## Общая схема

Проект построен по слоям:

- `presentation` — Compose UI, screen/view state, navigation shell
- `domain` — модели, сервисы оркестрации, use case, контракты репозиториев
- `data` — Room, DataStore, entity, mapper, repository implementation
- `di` — Hilt modules

## Presentation

Текущая структура эволюционно движется от screen-centric к feature-first, но без разрушения рабочего приложения.

Ключевые зоны:

- `presentation/navigation` — shell, drawer, bottom navigation, nav graph
- `presentation/screen/*` — основные экраны
- `presentation/feature/stages` — отдельный stage screen
- `presentation/component` — reusable UI-компоненты

### Navigation shell

Основные файлы:

- [AppNavHost.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/presentation/navigation/AppNavHost.kt)
- [AppNavGraph.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/presentation/navigation/AppNavGraph.kt)
- [AppDestination.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/presentation/navigation/AppDestination.kt)
- [BottomNavigationConfig.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/presentation/navigation/BottomNavigationConfig.kt)
- [DrawerNavigationConfig.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/presentation/navigation/DrawerNavigationConfig.kt)
- [AppDrawerContent.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/presentation/navigation/AppDrawerContent.kt)

Bottom navigation:

- `dashboard`
- `events`
- `checklist`

Secondary destination через top bar:

- `journal`

## App-level state

Главный app-level state собирается в [AppViewModel.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/presentation/navigation/AppViewModel.kt).

Ключевые поля:

- `userId`
- `themeMode`
- `dueDate`
- `appStage`
- `birthRecorded`

Источники правды:

- `DataStore` для живых пользовательских настроек
- Room snapshot для устойчивых миграций и связанного оффлайн-поведения

## Domain orchestration

Ключевая логика вынесена из composable в сервисы:

- [StageManager.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/domain/service/StageManager.kt)
- [StageTransitionManager.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/domain/service/StageTransitionManager.kt)
- [HomeContentBuilder.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/domain/service/HomeContentBuilder.kt)
- [EventsProvider.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/domain/service/EventsProvider.kt)

Роли:

- `StageManager` вычисляет derived-info по ПДР и текущему этапу
- `StageTransitionManager` содержит правила допустимых переходов и доменные guard’ы
- `HomeContentBuilder` определяет, какие секции `Главной` показывать в текущем состоянии
- `EventsProvider` собирает секции и действия экрана `События`

## Stage model

Актуальная модель:

- `PREPARING`
- `CONTRACTIONS`
- `AT_HOSPITAL`
- `AT_HOME`

Совместимость со старыми данными:

- `LABOR -> CONTRACTIONS`
- `AFTER_BIRTH -> AT_HOME`

Дополнительно действуют guard’ы:

- после сохранения рождения нельзя вручную вернуться в `PREPARING` или `CONTRACTIONS`
- `Начались роды` блокируется после сохранения рождения
- `Приехали домой` разрешено только после рождения и только из `AT_HOSPITAL`

## Persistence

### DataStore

Используется для:

- темы
- ПДР
- app stage
- других пользовательских настроек

### Room

Используется для:

- сессий и истории схваток
- событий по водам
- timeline / journal
- чек-листов и их пунктов
- labor summary
- контактов
- трекеров

Актуальные миграции:

- legacy `1 -> 3`
- legacy `2 -> 3`
- `3 -> 4`
- `4 -> 5`
- `5 -> 6`
- прямой safe-path `4 -> 6`

Последняя миграция переводит контакты на динамическую user-managed модель и сохраняет совместимость старых данных.

## Contacts architecture

Контакты больше не завязаны на фиксированные слоты экрана.

Актуальная модель:

- `EmergencyContactType`
- `EmergencyContact`
- `EmergencyContactRepository`

Поддерживаются роли:

- `EMERGENCY`
- `WIFE`
- `DOCTOR`
- `HOSPITAL`
- `TAXI`
- `RELATIVE`
- `CUSTOM`

`Home`, `SOS` и экран контактов используют одну и ту же контактную модель.

## Checklist architecture

Чек-листы состоят из:

- корневого списка `Checklist`
- пунктов `ChecklistItem`
- агрегата `ChecklistWithItems`

Поддерживаются:

- системные списки
- пользовательские списки

В item-модели уже предусмотрены расширяемые поля:

- `note`
- `quantity`
- `priority`
- `metadataJson`

## Testing

Unit tests:

- stage rules
- home builder
- labor lifecycle guards
- checklist use cases
- contractions analytics

Instrumented tests:

- shell navigation
- stage-aware home/events flows
- help / SOS
- settings validation
- contraction analytics on seeded data
