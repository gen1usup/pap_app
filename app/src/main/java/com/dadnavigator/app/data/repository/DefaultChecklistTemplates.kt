package com.dadnavigator.app.data.repository

import com.dadnavigator.app.domain.model.AppStage

/**
 * Structured default checklists grouped by app stage and scenario category.
 */
internal data class ChecklistTemplate(
    val title: String,
    val stage: AppStage,
    val category: String,
    val sortOrder: Int,
    val items: List<String>
)

internal val defaultChecklistTemplates: List<ChecklistTemplate> = listOf(
    ChecklistTemplate(
        title = "Документы",
        stage = AppStage.PREPARING,
        category = "До родов",
        sortOrder = 0,
        items = listOf(
            "Паспорт мамы и ваш паспорт под рукой",
            "Полис ОМС или ДМС и СНИЛС",
            "Обменная карта и свежие анализы",
            "Договор с роддомом или направление, если есть",
            "Все документы лежат в одной папке"
        )
    ),
    ChecklistTemplate(
        title = "Сумка в роддом для мамы",
        stage = AppStage.PREPARING,
        category = "До родов",
        sortOrder = 1,
        items = listOf(
            "Ночная рубашка, халат и тапочки",
            "Средства гигиены и ухода",
            "Бутылка воды и легкий перекус",
            "Зарядка, кабель и пауэрбанк",
            "Резинка для волос, бальзам для губ и мелочи для комфорта"
        )
    ),
    ChecklistTemplate(
        title = "Сумка для ребенка",
        stage = AppStage.PREPARING,
        category = "До родов",
        sortOrder = 2,
        items = listOf(
            "Подгузники на первые дни",
            "Боди, шапочка и носочки",
            "Пеленка или плед на выписку",
            "Влажные салфетки и крем под подгузник"
        )
    ),
    ChecklistTemplate(
        title = "Сумка для отца",
        stage = AppStage.PREPARING,
        category = "До родов",
        sortOrder = 3,
        items = listOf(
            "Документы и банковская карта",
            "Вода, перекус и сменная футболка",
            "Зарядка для телефона и наушники",
            "Наличные на дорогу, парковку или аптеку"
        )
    ),
    ChecklistTemplate(
        title = "Подготовка дома",
        stage = AppStage.PREPARING,
        category = "До родов",
        sortOrder = 4,
        items = listOf(
            "Подготовить место для сна ребенка",
            "Приготовить одежду на первые дни",
            "Проверить аптечку и термометр",
            "Сделать запас воды, перекусов и бытовых мелочей"
        )
    ),
    ChecklistTemplate(
        title = "Машина и дорога",
        stage = AppStage.PREPARING,
        category = "До родов",
        sortOrder = 5,
        items = listOf(
            "Заправить машину или продумать запасной вариант",
            "Проверить маршрут и время до роддома",
            "Убедиться, что автокресло установлено",
            "Сохранить телефоны роддома, врача и такси"
        )
    ),
    ChecklistTemplate(
        title = "Если роды начнутся",
        stage = AppStage.PREPARING,
        category = "До родов",
        sortOrder = 6,
        items = listOf(
            "Не паниковать и открыть раздел События",
            "Запустить счетчик схваток, если схватки стали регулярными",
            "Зафиксировать воды, если они отошли",
            "Подготовить документы и сумки к выходу"
        )
    ),
    ChecklistTemplate(
        title = "Что проверить сразу",
        stage = AppStage.LABOR,
        category = "Когда начались роды",
        sortOrder = 10,
        items = listOf(
            "Оценить самочувствие мамы и тревожные признаки",
            "Понять, регулярные ли схватки",
            "Проверить, отошли ли воды",
            "Связаться с врачом или роддомом, если есть сомнения"
        )
    ),
    ChecklistTemplate(
        title = "Что подготовить",
        stage = AppStage.LABOR,
        category = "Когда начались роды",
        sortOrder = 11,
        items = listOf(
            "Поставить телефоны на зарядку",
            "Положить документы и сумки у выхода",
            "Подготовить воду и перекус в дорогу",
            "Проверить машину или заранее вызвать такси"
        )
    ),
    ChecklistTemplate(
        title = "Что не забыть",
        stage = AppStage.LABOR,
        category = "Когда начались роды",
        sortOrder = 12,
        items = listOf(
            "Открыть счетчик схваток или раздел События",
            "Следить за тем, что говорит врач",
            "Фиксировать важные отметки в журнале",
            "Брать коммуникацию и организацию на себя"
        )
    ),
    ChecklistTemplate(
        title = "Перед выездом",
        stage = AppStage.LABOR,
        category = "Перед выездом",
        sortOrder = 13,
        items = listOf(
            "Документы и сумки уже в машине или у двери",
            "Телефоны, зарядка, вода и ключи взяты",
            "Окна закрыты, плита и утюг выключены",
            "Маршрут до роддома открыт",
            "В роддом позвонили, если это нужно"
        )
    ),
    ChecklistTemplate(
        title = "Документы ребенка",
        stage = AppStage.AFTER_BIRTH,
        category = "После родов",
        sortOrder = 20,
        items = listOf(
            "Уточнить порядок оформления свидетельства о рождении",
            "Подготовить документы для полиса и СНИЛС",
            "Понять сроки регистрации ребенка",
            "Собрать список документов для пособий"
        )
    ),
    ChecklistTemplate(
        title = "Медицина и визиты",
        stage = AppStage.AFTER_BIRTH,
        category = "После родов",
        sortOrder = 21,
        items = listOf(
            "Уточнить, когда ждать педиатра",
            "Сохранить контакты поликлиники",
            "Не забыть про наблюдение мамы у врача",
            "Записать вопросы к врачам в журнал"
        )
    ),
    ChecklistTemplate(
        title = "Организация дома",
        stage = AppStage.AFTER_BIRTH,
        category = "После родов",
        sortOrder = 22,
        items = listOf(
            "Организовать тихий режим дома",
            "Подготовить место для подгузников и кормления",
            "Сделать запас бытовых расходников на несколько дней",
            "Ограничить лишние визиты и гостей"
        )
    ),
    ChecklistTemplate(
        title = "Помощь маме",
        stage = AppStage.AFTER_BIRTH,
        category = "Помощь маме",
        sortOrder = 23,
        items = listOf(
            "Следить, чтобы мама ела и пила воду",
            "Взять на себя быт и коммуникацию",
            "Давать маме время на отдых и сон",
            "Помогать с ребенком без ожидания просьбы",
            "Сдерживать поток гостей, если это утомляет"
        )
    )
)
