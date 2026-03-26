# Папа рядом (DadNavigator)

Android-приложение для будущего отца: перед родами, во время родов и сразу после.  
Проект работает офлайн, хранит данные локально и архитектурно готов к синхронизации/бэкенду в будущем.

## Быстрый старт на другом ПК (10-15 минут)

### 1) Что нужно установить
- `Git`
- `Android Studio` (актуальная стабильная версия)
- `JDK 17` (в Android Studio обычно уже есть встроенный JBR 17)

### 2) Клонировать проект
```bash
git clone https://github.com/gen1usup/pap_app.git
cd pap_app
```

### 3) Установить Android SDK компоненты
В Android Studio: `Settings` -> `Android SDK`:
- `Android SDK Platform 34`
- `Android SDK Build-Tools 34.0.0`
- `Android SDK Platform-Tools`

### 4) Прописать путь к SDK (критично)
Создай `local.properties` в корне проекта:

Windows (PowerShell):
```powershell
"sdk.dir=C\:\\Users\\$env:USERNAME\\AppData\\Local\\Android\\Sdk" | Out-File -FilePath local.properties -Encoding ascii
```

macOS/Linux:
```bash
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties
```

Если SDK в другом месте, укажи свой путь.

### 5) Собрать debug APK
Windows:
```powershell
.\gradlew.bat assembleDebug
```

macOS/Linux:
```bash
./gradlew assembleDebug
```

Готовый APK: `app/build/outputs/apk/debug/app-debug.apk`

### 6) Запустить на телефоне
- Включи на телефоне `Режим разработчика` и `Отладка по USB`.
- Подключи телефон к ПК.
- Установи APK:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Если видишь ошибку "sdk.dir / ANDROID_HOME not found"
Почти всегда причина в отсутствии `local.properties` или неверном `sdk.dir`.  
Проверь:
1. Файл `local.properties` есть в корне.
2. Путь к SDK реальный.
3. В пути используются двойные `\\` для Windows.

## Если Android пишет "Пакет поврежден / Приложение не установлено"
Основные причины:
1. APK скачан/скопирован не полностью (передай файл заново).
2. Ставишь `release` APK без подписи (нужен подписанный APK).
3. Конфликт подписи с уже установленной версией (удали старую версию или подпиши тем же ключом).
4. Несовместимая сборка ABI/SDK (для этого проекта стандартный APK должен ставиться на современные Samsung).

Для установки в обход передачи файла на телефон надежнее использовать `adb install -r ...`.

## Полная локальная проверка проекта
Windows:
```powershell
.\gradlew.bat clean :app:assembleDebug :app:assembleRelease :app:testDebugUnitTest :app:lintDebug
```

## Release сборка и подпись

### Собрать release APK
```bash
./gradlew assembleRelease
```
Файл: `app/build/outputs/apk/release/app-release.apk`

### Подписать release APK (Android Studio)
1. `Build` -> `Generate Signed Bundle / APK`
2. Выбери `APK`
3. Создай/укажи `keystore (.jks)`, `alias`, пароли
4. Выбери `release`
5. Заверши мастер и получи подписанный APK

### App Bundle
```bash
./gradlew bundleRelease
```
Файл: `app/build/outputs/bundle/release/app-release.aab`

## Назначение приложения
- Быстрые подсказки "что делать сейчас"
- Счетчик схваток (сессии, интервалы, рекомендации)
- Таймер после отхождения вод
- Decision-логика "когда ехать в роддом"
- Чек-листы (включая пользовательские пункты)
- SOS-сценарии
- Помощь маме
- Разделы "в родах" и "после родов"
- Трекеры: кормление, подгузники, сон, заметки
- Хронология событий

## Технологии
- Kotlin
- Jetpack Compose + Material 3
- MVVM + Clean Architecture + UDF
- Navigation Compose
- Room
- DataStore
- Coroutines + Flow
- Hilt (DI)
- Repository + UseCase layer
- Unit/UI tests
- Gradle Kotlin DSL

## UI/UX

### Новый визуальный язык
- Спокойная медицинская палитра с поддержкой light/dark theme
- Material 3 + Material You dynamic color на Android 12+
- Крупные touch targets и выраженная иерархия контента
- Единые токены для spacing, shapes, typography и статусных цветов
- Production-style reusable components вместо разрозненных локальных карточек

### Дизайн-система
- `Theme.kt` — тема приложения, dynamic color, status palette, shape tokens
- `Color.kt` — базовая палитра и статусные цвета
- `Typography.kt` — читаемая типографическая шкала для стрессового сценария
- `Shapes.kt` — округлые формы для карточек, pill buttons и sheets
- `Spacing.kt` — единая шкала отступов и минимальных touch target

### Reusable components
- `PrimaryButton`, `SecondaryButton`, `DangerButton`
- `InfoCard`, `StatusCard`
- `TimelineItem`
- `ChecklistItem`
- `EmptyState`, `ErrorState`, `LoadingState`
- `ScreenScaffold` с roomy top app bar и спокойным background treatment

### UX-улучшения
- Главный экран теперь показывает:
  - card "Что делать сейчас"
  - большую CTA для старта/открытия счетчика схваток
  - быстрый SOS
  - карточку таймера вод
  - прогресс чек-листов
  - последние события timeline
- Счетчик схваток переработан в пользу одной доминирующей кнопки и понятной стадии
- Timeline получил иконки, фильтры и bottom sheet для ручного добавления событий
- Чек-листы и трекеры стали удобнее для одной руки и быстрее для ввода
- SOS-экран стал визуально более срочным: меньше текста, больше ясных действий

### Адаптивность
- Dashboard учитывает `WindowWidthSizeClass`
- Остальные ключевые экраны переведены на responsive spacing и более устойчивые layout patterns
- UI лучше переносится на большие телефоны и планшеты за счет карточной структуры и гибких секций

### Accessibility
- Крупные кнопки и увеличенные touch targets
- Текстовая иерархия не полагается только на цвет
- Иконки сопровождаются текстом
- Улучшена читаемость в dark theme и при стрессовом использовании

### Motion и state-driven UI
- Экранные состояния строятся от данных ViewModel
- Добавлены empty states и snackbar-driven feedback
- Поддержаны confirm dialogs для опасных действий
- Важные CTA подготовлены к haptic feedback в критичных сценариях

## Архитектура
Слои:
- `data` - Room/DataStore/DAO/entity/mappers/repository implementation
- `domain` - модели, repository interfaces, use cases
- `presentation` - ViewModel, UI state/event, Compose UI, navigation

Ключевые принципы:
- Односторонний поток данных (UDF)
- Бизнес-логика вынесена из UI
- Подготовка к мультипользовательскому режиму через `userId` в моделях

## Структура проекта
```text
pap_app/
  app/
    src/main/java/com/dadnavigator/app/
      core/
      data/
      di/
      domain/
      presentation/
    src/main/res/
    src/test/
    src/androidTest/
  gradle/
  build.gradle.kts
  settings.gradle.kts
```

## Основные тесты
- Unit:
  - `CalculateContractionStatsUseCaseTest`
  - `EvaluateHospitalDecisionUseCaseTest`
  - `ChecklistUseCasesTest`
- UI:
  - `NavigationUiTest`

## Оптимизация

### Память
- Состояние хранится во `ViewModel`
- Потоки данных через `Flow`
- Используются ленивые списки (`LazyColumn`)

### Размер APK
- В release включены `minifyEnabled` и `shrinkResources`
- R8/Proguard: `app/proguard-rules.pro`

### Производительность
- UDF + изолированные use case
- Ограничение лишних recomposition
- Room-запросы по релевантным полям

## Ограничения
- Нет облачной синхронизации и авторизации в текущем релизе
- Нет статуса медицинского изделия

## Roadmap
1. Авторизация/профили
2. Синхронизация между устройствами
3. Backup/restore
4. Шифрование и безопасный экспорт

## Дисклеймер
Приложение не заменяет консультацию врача.  
При тревожных симптомах обращайтесь за экстренной медицинской помощью.
