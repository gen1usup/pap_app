# Реестр тестов

Документ описывает актуальные автоматические тесты проекта и последний подтвержденный прогон.

## 1. Как запускаются тесты

### Unit tests

Запускаются из `app/src/test/java` и проверяют:

- бизнес-логику;
- stage rules;
- home builder;
- доменные use case;
- аналитические расчеты.

### Instrumented tests

Запускаются из `app/src/androidTest/java` на эмуляторе или устройстве и проверяют:

- shell navigation;
- drawer и stage screen;
- настройки;
- журнал;
- экранные сценарии и seeded данные схваток.

## 2. Последний подтвержденный прогон

Дата: 27.03.2026

Стенд:

- `emulator-5554`

Подтвержденные результаты:

- `:app:compileDebugKotlin` — успешно
- `:app:compileDebugAndroidTestKotlin` — успешно
- `:app:assembleDebug` — успешно
- `:app:assembleDebugAndroidTest` — успешно
- `:app:testDebugUnitTest` для целевых suites — успешно
- `DeviceScenarioUiTest` — `OK (7 tests)`
- `SettingsValidationUiTest` — `OK (1 test)`
- `ContractionAnalyticsUiTest` — `OK (3 tests)`

## 3. Unit test suites

### 3.1 [StageManagementTest.kt](c:/PROJECTS/pap_app/app/src/test/java/com/dadnavigator/app/domain/service/StageManagementTest.kt)

Покрывает:

- совместимость старых stage-значений;
- расчет readiness-window с 37 недели;
- отсутствие ПДР;
- переходы `CONTRACTIONS / AT_HOSPITAL / AT_HOME`;
- reminder о ПДР на главной;
- появление contraction shortcut c 37 недели;
- скрытие contraction shortcut в `AT_HOSPITAL`.

Количество тестов:

- `7`

### 3.2 [EventsProviderTest.kt](c:/PROJECTS/pap_app/app/src/test/java/com/dadnavigator/app/domain/service/EventsProviderTest.kt)

Покрывает:

- подготовительный `Events`-сценарий до readiness-window;
- расширение `Events` в readiness-window c 37 недели;
- подмену `Начать схватку / Завершить схватку` по live-state;
- hospital-секцию с `Приехали домой`;
- домашние quick-record действия без labor-controls.

Количество тестов:

- `5`

### 3.3 [LaborLifecycleUseCasesTest.kt](c:/PROJECTS/pap_app/app/src/test/java/com/dadnavigator/app/domain/usecase/labor/LaborLifecycleUseCasesTest.kt)

Покрывает:

- первое начало родов;
- защиту от повторного перезаписывания `laborStartTime`;
- первое сохранение рождения;
- защиту от дублей рождения;
- игнорирование пустого необязательного имени ребенка.

Количество тестов:

- `5`

### 3.4 [CalculateContractionStatsUseCaseTest.kt](c:/PROJECTS/pap_app/app/src/test/java/com/dadnavigator/app/domain/usecase/CalculateContractionStatsUseCaseTest.kt)

Покрывает:

- пустую историю;
- устойчивый moderate-паттерн;
- устойчивый strong-паттерн;
- нерегулярный паттерн с длинными схватками;
- использование recent-window;
- расчет длины текущего паттерна.

Количество тестов:

- `6`

### 3.5 [ChecklistUseCasesTest.kt](c:/PROJECTS/pap_app/app/src/test/java/com/dadnavigator/app/domain/usecase/ChecklistUseCasesTest.kt)

Покрывает:

- запрет на пустой custom item;
- trim пользовательского текста;
- делегирование toggle в репозиторий.

Количество тестов:

- `3`

### 3.6 [EvaluateHospitalDecisionUseCaseTest.kt](c:/PROJECTS/pap_app/app/src/test/java/com/dadnavigator/app/domain/usecase/EvaluateHospitalDecisionUseCaseTest.kt)

Покрывает:

- emergency-решение при кровотечении;
- рекомендацию ехать при активном регулярном паттерне;
- возврат `MONITOR`, если тревожных признаков нет.

Количество тестов:

- `3`

## 4. Instrumented test suites

### 4.1 [NavigationUiTest.kt](c:/PROJECTS/pap_app/app/src/androidTest/java/com/dadnavigator/app/NavigationUiTest.kt)

Проверяет:

- открытие экрана счетчика схваток из вкладки `События`.

Количество тестов:

- `1`

### 4.2 [DeviceScenarioUiTest.kt](c:/PROJECTS/pap_app/app/src/androidTest/java/com/dadnavigator/app/DeviceScenarioUiTest.kt)

Проверяет:

- `Главная -> Чек-листы -> Главная` через bottom navigation;
- открытие stage screen из drawer;
- перестройку главной в `AT_HOSPITAL` и скрытие shortcut счетчика схваток;
- hospital-сценарий `Событий` с `Приехали домой` и скрытием labor-tools;
- домашний `Events`-сценарий с быстрыми записями;
- открытие настроек и наличие controls ПДР;
- валидацию пустого labor-события в журнале;
- отображение ключевых блоков `Справка` и `SOS`.

Количество тестов:

- `7`

### 4.3 [SettingsValidationUiTest.kt](c:/PROJECTS/pap_app/app/src/androidTest/java/com/dadnavigator/app/SettingsValidationUiTest.kt)

Проверяет:

- inline-валидацию неверной ПДР на Compose-экране.

Количество тестов:

- `1`

### 4.4 [ContractionAnalyticsUiTest.kt](c:/PROJECTS/pap_app/app/src/androidTest/java/com/dadnavigator/app/ContractionAnalyticsUiTest.kt)

Проверяет через seeded данные:

- `Наблюдаем` для нерегулярных схваток;
- `Готовимся к выезду` для moderate regular pattern;
- `Пора ехать` для strong regular pattern.

Количество тестов:

- `3`

## 5. Тестовые данные для долгих сценариев

Для сценариев, которые в реальности занимают 30-90 минут, используется [TestAppStateSeeder.kt](c:/PROJECTS/pap_app/app/src/androidTest/java/com/dadnavigator/app/testsupport/TestAppStateSeeder.kt).

Он умеет:

- очищать локальные данные;
- seed-ить готовые сценарии схваток;
- seed-ить конкретный этап приложения.

Это позволяет проверять критические и граничные сценарии без реального ожидания времени.

## 6. Что пока не покрыто полностью

Автоматически пока не покрыты как отдельные полноценные e2e-сценарии:

- полный путь `схватки -> воды -> рождение -> AT_HOME` одним тестом;
- расширенный CRUD контактов и адресов.
