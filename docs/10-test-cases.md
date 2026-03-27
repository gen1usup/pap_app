# Реестр тестов

Документ описывает актуальные автоматические тесты и последний подтвержденный прогон.

## 1. Последний подтвержденный прогон

Дата: 27.03.2026

Стенд:

- `emulator-5554`

Подтвержденные результаты:

- `:app:compileDebugKotlin` — успешно
- `:app:compileDebugAndroidTestKotlin` — успешно
- `:app:testDebugUnitTest` — успешно
- `:app:connectedDebugAndroidTest` — успешно

Instrumentation summary:

- `DeviceScenarioUiTest` — `OK (7 tests)`
- `NavigationUiTest` — `OK (1 test)`
- `SettingsValidationUiTest` — `OK (1 test)`
- `ContractionAnalyticsUiTest` — `OK (3 tests)`

Итого подтверждено на эмуляторе: `12 instrumented tests`.

## 2. Unit test suites

### [StageManagementTest.kt](c:/PROJECTS/pap_app/app/src/test/java/com/dadnavigator/app/domain/service/StageManagementTest.kt)

Покрывает:

- совместимость legacy stage-значений
- readiness-window с 37 недели
- отсутствие ПДР
- допустимые переходы между этапами
- блокировку ручного отката после рождения
- базовые правила `HomeContentBuilder`

### [LaborLifecycleUseCasesTest.kt](c:/PROJECTS/pap_app/app/src/test/java/com/dadnavigator/app/domain/usecase/labor/LaborLifecycleUseCasesTest.kt)

Покрывает:

- первое начало родов
- запрет повторного старта
- блокировку `Начались роды` после рождения
- первое сохранение рождения
- guard для `Приехали домой`

### [SettingsStageGuardUseCasesTest.kt](c:/PROJECTS/pap_app/app/src/test/java/com/dadnavigator/app/domain/usecase/settings/SettingsStageGuardUseCasesTest.kt)

Покрывает:

- защиту `UpdateAppStageUseCase`
- защиту `SaveSettingsUseCase`
- невозможность сохранить нелогичный stage rollback после рождения

### [ChecklistUseCasesTest.kt](c:/PROJECTS/pap_app/app/src/test/java/com/dadnavigator/app/domain/usecase/ChecklistUseCasesTest.kt)

Покрывает:

- создание пользовательского списка
- trim названия списка
- запрет пустого названия
- добавление пункта
- trim текста пункта
- delete item / delete checklist
- toggle item

### [CalculateContractionStatsUseCaseTest.kt](c:/PROJECTS/pap_app/app/src/test/java/com/dadnavigator/app/domain/usecase/CalculateContractionStatsUseCaseTest.kt)

Покрывает:

- пустую историю
- monitor / prepare / go patterns
- irregular сценарии
- recent-window расчеты

## 3. Instrumented test suites

### [DeviceScenarioUiTest.kt](c:/PROJECTS/pap_app/app/src/androidTest/java/com/dadnavigator/app/DeviceScenarioUiTest.kt)

Проверяет:

- `Главная -> Чек-листы -> Главная`
- открытие stage screen из drawer
- перестройку `Главной` в `AT_HOSPITAL`
- hospital-сценарий `Событий`
- home-сценарий `Событий`
- `Справку` и `SOS`
- открытие `Настроек` и наличие controls для ПДР

### [NavigationUiTest.kt](c:/PROJECTS/pap_app/app/src/androidTest/java/com/dadnavigator/app/NavigationUiTest.kt)

Проверяет:

- переход в экран схваток из `Событий`

### [SettingsValidationUiTest.kt](c:/PROJECTS/pap_app/app/src/androidTest/java/com/dadnavigator/app/SettingsValidationUiTest.kt)

Проверяет:

- inline-валидацию неверной ПДР

### [ContractionAnalyticsUiTest.kt](c:/PROJECTS/pap_app/app/src/androidTest/java/com/dadnavigator/app/ContractionAnalyticsUiTest.kt)

Проверяет через seeded данные:

- `Наблюдаем`
- `Готовимся к выезду`
- `Пора ехать`

## 4. Тестовые данные для длинных сценариев

Для сценариев, которые в реальности занимают десятки минут, используется [TestAppStateSeeder.kt](c:/PROJECTS/pap_app/app/src/androidTest/java/com/dadnavigator/app/testsupport/TestAppStateSeeder.kt).

Seeder умеет:

- очищать локальные данные
- seed-ить stage
- seed-ить готовые паттерны схваток с историческими timestamp

Это позволяет проверять критические и граничные сценарии без реального ожидания времени.

## 5. Что дополнительно подтверждено этим прогоном

- миграция Room `4 -> 6` теперь работает на уже существующей тестовой базе
- общий и прямой migration path для новых checklist/contact изменений не ломает открытие приложения
- обновленные `Справка`, `Контакты`, `SOS` и `Чек-листы` не ломают navigation smoke suite
