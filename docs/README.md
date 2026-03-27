# Документация проекта

Этот каталог содержит актуальную документацию по Android-приложению `Папа рядом` / `Dad Navigator`.

Документы синхронизированы с текущей кодовой базой после перехода на 4-stage модель приложения и нового navigation shell:

- этапы: `PREPARING`, `CONTRACTIONS`, `AT_HOSPITAL`, `AT_HOME`;
- bottom navigation: `Главная`, `События`, `Чек-листы`;
- `Журнал` открывается через иконку в top bar на top-level экранах;
- drawer разделен на группы `Этапы` и `Сервис`;
- главная собирается по context-aware правилам и показывает reminder о ПДР, пока дата не заполнена.

## Состав

- [01-product-overview.md](./01-product-overview.md) — обзор продукта, этапов и пользовательского контекста.
- [02-functional-requirements.md](./02-functional-requirements.md) — функциональные и нефункциональные требования по текущей реализации.
- [03-use-cases.md](./03-use-cases.md) — пошаговые сценарии пользователя с предусловиями и результатом.
- [04-architecture.md](./04-architecture.md) — структура проекта, shell-навигация, state management и orchestration.
- [05-database.md](./05-database.md) — Room/DataStore, таблицы, ключи и правила совместимости.
- [06-stage-transitions.md](./06-stage-transitions.md) — точные правила переходов между этапами.
- [07-visibility-rules.md](./07-visibility-rules.md) — условия показа экранов, карточек, CTA и вспомогательных блоков.
- [08-contraction-spec.md](./08-contraction-spec.md) — спецификация счетчика схваток и аналитики.
- [09-on-device-validation.md](./09-on-device-validation.md) — подтвержденные на эмуляторе и устройстве сценарии.
- [10-test-cases.md](./10-test-cases.md) — реестр unit и instrumented тестов, включая последний подтвержденный прогон.

## Для чего использовать

Эта документация нужна для:

- продуктовой доработки без потери текущего поведения;
- ревью навигации, этапов и контекстного UI;
- онбординга разработчиков и тестировщиков;
- контроля фактического поведения приложения относительно требований.

## Актуальное состояние

На момент обновления документации приложение остается:

- offline-first;
- single-activity + Compose navigation;
- построенным на `Kotlin`, `Jetpack Compose`, `Hilt`, `Room`, `DataStore`;
- эволюционно рефакторящимся без переписывания с нуля.

Если нужен быстрый вход в проект, стоит читать в таком порядке:

1. [01-product-overview.md](./01-product-overview.md)
2. [04-architecture.md](./04-architecture.md)
3. [06-stage-transitions.md](./06-stage-transitions.md)
4. [07-visibility-rules.md](./07-visibility-rules.md)
5. [10-test-cases.md](./10-test-cases.md)
