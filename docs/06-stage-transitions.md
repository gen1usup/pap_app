# Правила переходов между этапами

Документ фиксирует фактические правила stage transition в текущей реализации.

## Список этапов

- `PREPARING`
- `CONTRACTIONS`
- `AT_HOSPITAL`
- `AT_HOME`

## Общий принцип

Этапы не меняются автоматически по ПДР, времени, статистике схваток или неявным эвристикам.

Переход выполняется только:

- явным действием пользователя
- или use case, который оформляет milestone-событие

## Источники смены этапа

### 1. Ручная активация из drawer

Поток:

1. Пользователь открывает drawer.
2. Выбирает нужный этап.
3. Открывается stage screen с описанием сценария.
4. Пользователь подтверждает активацию.
5. `UpdateAppStageUseCase` сохраняет этап в настройках.

### 2. Ручная активация из настроек

Поток:

1. Пользователь выбирает этап в `Настройках`.
2. Нажимает сохранение.
3. `SaveSettingsUseCase` сохраняет настройки и stage.

### 3. Сценарное действие `Начались роды`

Источник:

- `DashboardScreen`
- `EventsScreen`
- `MarkLaborStartedUseCase`

Результат:

- сохраняется `laborStartTime`, если его еще не было
- создается timeline event о начале родов, если это первый запуск
- этап становится `CONTRACTIONS`

### 4. Сценарное действие `Ребенок родился`

Источник:

- `EventsScreen`
- `LaborScreen`
- `MarkBirthUseCase`

Результат:

- сохраняется `birthTime`
- при наличии сохраняются имя, вес и рост
- этап становится `AT_HOSPITAL`
- создается timeline event о рождении

### 5. Сценарное действие `Приехали домой`

Источник:

- `EventsScreen`
- `MarkArrivedHomeUseCase`

Результат:

- создается запись о возвращении домой
- этап становится `AT_HOME`

## Domain guards

### Запрет отката после рождения

Если `birthTime != null`, ручное переключение в:

- `PREPARING`
- `CONTRACTIONS`

запрещено.

Guard применяется не только в UI, но и в домене:

- [StageTransitionManager.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/domain/service/StageTransitionManager.kt)
- [UpdateAppStageUseCase.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/domain/usecase/settings/UpdateAppStageUseCase.kt)
- [SaveSettingsUseCase.kt](c:/PROJECTS/pap_app/app/src/main/java/com/dadnavigator/app/domain/usecase/settings/SaveSettingsUseCase.kt)

### Блокировка `Начались роды` после рождения

Если `birthTime != null`, `MarkLaborStartedUseCase` не должен:

- сохранять `laborStartTime`
- создавать labor event
- переводить UI в `CONTRACTIONS`

### Guard для `Приехали домой`

`MarkArrivedHomeUseCase` разрешен только если:

- рождение уже зафиксировано
- текущий этап равен `AT_HOSPITAL`

Если этап уже `AT_HOME`, повторное действие не создает дубликат.

## Чего система не делает

Система не делает автоматически:

- `PREPARING -> CONTRACTIONS` по ПДР
- `PREPARING -> CONTRACTIONS` по аналитике схваток
- `AT_HOSPITAL -> AT_HOME` по таймеру
- возврат на ранние этапы после рождения

## Совместимость старых данных

При чтении legacy-значений используется mapping:

- `LABOR -> CONTRACTIONS`
- `AFTER_BIRTH -> AT_HOME`
