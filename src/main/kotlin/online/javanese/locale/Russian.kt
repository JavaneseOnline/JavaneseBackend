package online.javanese.locale

import online.javanese.model.TaskErrorReport

object Russian : Language {

    override val indexCardDescriptions: Language.IndexCards = object : Language.IndexCards {
        override val lessons: String get() = "Теория для новичков и продвинутых"
        override val tasks: String get() = "Практические упражнения для\u00A0проверки знаний"
        override val articles: String get() = "Полезные\u00A0советы, решения\u00A0проблем"
        override val review: String get() = "Пришлите нам свой\u00A0код, и\u00A0мы\u00A0постараемся вам\u00A0помочь"
    }

    override val sandbox: Language.SandboxLanguage = object : Language.SandboxLanguage {

        override val runtimeMessages: Language.SandboxLanguage.RuntimeMessages = object : Language.SandboxLanguage.RuntimeMessages {
            override val compiling: String get() = "Компиляция"
            override val compiled: String get() = "завершена."
            override val deadline: String get() = "Превышено максимальное время выполнения."
            override val exitCode: String get() = "Код завершения:"
            override val varNotFound: String get() = "Переменная {0} типа {1} не найдена в коде."
            override val eqNotFound: String get() = "Присвоение переменной {0} значения {1} не найдено в коде."
            override val notMatches: String get() = "Решение не подходит."
            override val illegalOutput: String get() = "Решение неверно. Ожидалось, что код выведет:"
            override val correctSolution: String get() = "Решение верно."
        }

        override val frontendMessages: Language.SandboxLanguage.FrontendMessages = object : Language.SandboxLanguage.FrontendMessages {
            override val run: String get() = "Запустить"
            override val reportError: String get() = "Сообщить об ошибке"
            override val errorReportedSuccessfully: String get() = "Спасибо, Ваше сообщение об ошибке отправлено."
            override val errorNotReported: String get() = "К сожалению, отправить сообщение об ошибке не удалось."
            override fun errorKind(kind: TaskErrorReport.ErrorKind): String = when (kind) {
                TaskErrorReport.ErrorKind.RightSolutionNotAccepted -> "моё решение правильное, но оно не принимается"
                TaskErrorReport.ErrorKind.BadCondition -> "некорректное или недостаточно чёткое условие"
                TaskErrorReport.ErrorKind.InsufficientMaterial -> "материала урока недостаточно для решения"
                TaskErrorReport.ErrorKind.Other -> "другое"
            }
            override val errorDescription: String get() = "Описание проблемы"
            override val webSocketError: String get() = "Соединение не удалось."
        }
    }

    override val articlesFeedInfo: Language.FeedInfo = object : Language.FeedInfo {
        override val title: String get() = "Статьи на Javanese.Online"
        override val description: String get() = "Статьи о Java и Kotlin, JVM и Android"
    }

    override val siteTitle: String get() = "Javanese"
    override val error: String get() = "Ошибка"
    override val httpErrors: Map<Int, String> = hashMapOf(
            404 to "Не найдено"
    )

    override val sendButton: String get() = "Отправить"
    override val cancelButton: String get() = "Отмена"

//    index.title=Главная
//    course.title=Курс

    override val lessonsTreeTab: String get() = "Уроки"
    override val tasksTreeTab: String get() = "Задачи"
//    tree.course=Курс
//    tree.chapter=Глава
//    tree.lesson=Урок
//    tree.task=Задача

    override val nextCourse: String get() = "Следующий курс"
    override val previousCourse: String get() = "Предыдущий курс"

    override val nextChapter: String get() = "Следующая глава"
    override val previousChapter: String get() = "Предыдущая глава"

    override val lessonTasks: String get() = "Задачи к уроку"
    override val lessonComments: String get() = "Комментарии к уроку"
    override val nextLesson: String get() = "Следующий урок"
    override val previousLesson: String get() = "Предыдущий урок"
    override val shareLessonButtonLabel: String get() = "Поделиться"

    override val articleComments: String get() = "Комментарии к статье"

    override val readCodeReviews: String get() = "Читать"
    override val submitCodeReview: String get() = "Разберите мой код!"

}
