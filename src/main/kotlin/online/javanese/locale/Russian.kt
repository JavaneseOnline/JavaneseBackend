package online.javanese.locale

import online.javanese.model.TaskErrorReport

object Russian : Language {

    override val sandbox: Language.SandboxLanguage = object : Language.SandboxLanguage {

        override val runtimeMessages: Language.SandboxLanguage.RuntimeMessages = object : Language.SandboxLanguage.RuntimeMessages {
            override val compiling: String get() = "Компиляция"
            override val compiled: String get() = "завершена."
            override val deadline: String get() = "Превышено максимальное время выполнения."
            override val exitCode: String get() = "Код завершения:"
            override val varNotFound: String get() = "Переменная {0} типа {1} не найдена в коде."
            override val eqNotFound: String get() = "Присвоение переменной {0} значения {1} не найдено в коде."
            override val notMatches: String get() = "Решение не подходит."
            override val illegalOutput: String get() = "Решение неверно."
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

    override val comments: Language.Comments = object : Language.Comments {
        override fun author(source: String, name: String): String =
                "$source / $name"

        override val removed: String get() = "Комментарий удалён."
        override val removalFailed: String get() = "Не удалилось."

        override val authPrompt: String get() = "Чтобы оставить комментарий, зайдите через "

        override val addPlaceholderHtml: String get() = "Текст комментария<br/><i>_поддерживается markdown_</i>"
        override val add: String get() = "Отправить"
        override val addFailed: String get() = "Не отправилось."

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

    override val codeReviews: Language.CodeReviews = object : Language.CodeReviews {
        override val readTab: String get() = "Читать"
        override val submitTab: String get() = "Разберите мой код!"
        override val authPrompt: String get() = "Чтобы отправить код на ревью, зайдите через "
        override val submissionError: String get() = "Произошла ошибка"

        override val nameLabel: String get() = "Имя или творческий псевдоним"
        override val nameExplanation: String get() = "Будет видно всем читателям"

        override val textLabel: String get() = "Вопросы или проблемы, возникшие в процессе написания кода"

        override val codeLabel: String get() = "Проблемный фрагмент кода на Java или Kotlin. Или ссылка на Gist или GitHub."
        override val codeExplanation: String get() = "Для наиболее подробного разбора ограничьтесь парой тысяч строк."

        override val contactLabel: String get() = "E-mail или Telegram"
        override val contactExplanation: String get() = "Для уведомления о публикации ревью"

        override val permissionLabel: String get() = "У меня есть право на публикацию этого кода"
        override val warning: String get() = "Учтите, что ваш Gist или репозиторий будет форкнут."

        override val submit: String get() = "Отправить"
        override val submitted: String get() = "Заявка отправлена."
    }

}
