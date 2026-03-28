# Тестовые сценарии

## Unit tests
### Stage и переходы
Покрываются:
- legacy mapping старых stage values
- `PREPARING -> LABOR` через схватки и воды
- `LABOR -> BABY_BORN`
- ручной возврат в `LABOR` после рождения
- базовые правила `HomeContentBuilder`

Файлы:
- `StageManagementTest`
- `LaborLifecycleUseCasesTest`
- `SettingsStageGuardUseCasesTest`

### События и журнал
Покрываются:
- stage-aware состав секций `Событий`
- отсутствие labor-only действий в `BABY_BORN`
- фильтрация журнала по `stageAtCreation`
- фильтр `Мои заметки` по `entryType`

Файлы:
- `EventsProviderTest`
- `TimelineFilterPolicyTest`

### Удаления и пересчет
Покрываются:
- удаление схватки и пересчет интервалов
- удаление записи о водах и пересчет рекомендации
- удаленный дефолтный чек-лист не восстанавливается автоматически

Файлы:
- `EventDeletionRecalculationTest`
- `ChecklistRepositoryImplTest`

## Instrumentation tests
Покрываются:
- bottom navigation smoke
- drawer stage preview screen
- `Home -> Checklists -> Home`
- карточка ребенка на `Главной` после рождения
- открытие `Событий`, `Контактов`, `Настроек`
- smoke-сценарии после `BABY_BORN`

Файлы:
- `DeviceScenarioUiTest`
- `NavigationUiTest`
- `SettingsValidationUiTest`
- `ContractionAnalyticsUiTest`

## Последний подтвержденный прогон
28 марта 2026 года:
- `:app:testDebugUnitTest` — успешно
- `:app:connectedDebugAndroidTest` — успешно, `12/12`
- `:app:assembleDebug` — успешно
