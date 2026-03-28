# Документация pap_app

Актуальная документация описывает текущую реализацию с 3-stage моделью:
- `PREPARING`
- `LABOR`
- `BABY_BORN`

## Состав
- [01-product-overview.md](./01-product-overview.md) — обзор продукта и пользовательского контекста
- [02-functional-requirements.md](./02-functional-requirements.md) — функциональные требования
- [03-use-cases.md](./03-use-cases.md) — ключевые пользовательские сценарии
- [04-architecture.md](./04-architecture.md) — архитектура и основные сервисы
- [05-database.md](./05-database.md) — структура хранения и миграции
- [06-stage-transitions.md](./06-stage-transitions.md) — правила переходов между этапами
- [07-visibility-rules.md](./07-visibility-rules.md) — правила показа блоков и экранов
- [08-contraction-spec.md](./08-contraction-spec.md) — спецификация `Активных родов`
- [09-on-device-validation.md](./09-on-device-validation.md) — подтвержденные проверки на устройстве и эмуляторе
- [10-test-cases.md](./10-test-cases.md) — тестовые сценарии и актуальный прогон

## Что важно
- этап приложения не равен экрану и не равен физическому месту
- автоматические переходы ограничены только понятными milestone-событиями
- журнал хранит `stageAtCreation` и `entryType`
- системные чек-листы можно удалить, и они не восстанавливаются автоматически
