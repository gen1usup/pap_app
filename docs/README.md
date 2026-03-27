# Документация проекта

Этот каталог содержит рабочую документацию по Android-приложению `Папа рядом` / `Dad Navigator`.

## Состав

- [01-product-overview.md](./01-product-overview.md) — обзор продукта, целей и основных пользовательских сценариев.
- [02-functional-requirements.md](./02-functional-requirements.md) — функциональные и нефункциональные требования.
- [03-use-cases.md](./03-use-cases.md) — детализированные use cases с четкими шагами, предусловиями, альтернативными потоками и конечным результатом.
- [04-architecture.md](./04-architecture.md) — текущая архитектура проекта, навигация и ответственность слоев.
- [05-database.md](./05-database.md) — структура локального хранения данных: Room + DataStore.
- [06-stage-transitions.md](./06-stage-transitions.md) — правила переходов между этапами приложения.
- [07-visibility-rules.md](./07-visibility-rules.md) — правила отображения блоков, экранов, состояний и контекстной информации.
- [08-contraction-spec.md](./08-contraction-spec.md) — детальная спецификация счетчика схваток, аналитики и отображаемых данных.
- [09-on-device-validation.md](./09-on-device-validation.md) — подтвержденный на телефоне walkthrough по экранам, этапам и навигации.
- [10-test-cases.md](./10-test-cases.md) — полный реестр автотестов: unit, instrumented, позитивные и негативные сценарии.

## Для кого это

- Для разработки и онбординга новых участников.
- Для уточнения продуктовой логики перед доработками.
- Для ревью изменений в навигации, данных и ключевых сценариях.

## Актуальность

Документация описывает текущую кодовую базу в репозитории `pap_app` и опирается на:

- Jetpack Compose UI
- Material 3
- Navigation Compose
- MVVM + Use Cases
- Hilt
- Room
- DataStore
- offline-first модель хранения

## Что читать для детальной спецификации

Если нужен не обзор, а пошаговое описание поведения системы, в первую очередь стоит смотреть:

- [03-use-cases.md](./03-use-cases.md)
- [06-stage-transitions.md](./06-stage-transitions.md)
- [07-visibility-rules.md](./07-visibility-rules.md)
- [08-contraction-spec.md](./08-contraction-spec.md)
- [10-test-cases.md](./10-test-cases.md)
