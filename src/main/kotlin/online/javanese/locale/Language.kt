package online.javanese.locale

import online.javanese.model.TaskErrorReport

interface Language {

    val indexCardDescriptions: IndexCards

    interface IndexCards {
        val lessons: String
        val tasks: String
        val articles: String
        val review: String
    }

    val sandbox: SandboxLanguage

    interface SandboxLanguage {
        val runtimeMessages: RuntimeMessages
        val frontendMessages: FrontendMessages

        interface RuntimeMessages {
            val compiling: String
            val compiled: String
            val deadline: String
            val exitCode: String
            val varNotFound: String
            val eqNotFound: String
            val notMatches: String
            val illegalOutput: String
            val correctSolution: String
        }

        interface FrontendMessages {
            val run: String
            val reportError: String
            val errorReportedSuccessfully: String
            val errorNotReported: String
            fun errorKind(kind: TaskErrorReport.ErrorKind): String
            val errorDescription: String
            val webSocketError: String
        }
    }

    val articlesFeedInfo: FeedInfo

    interface FeedInfo {
        val title: String
        val description: String
    }

    val siteTitle: String
    val error: String
    val httpErrors: Map<Int, String>

    val sendButton: String
    val cancelButton: String

    val lessonsTreeTab: String
    val tasksTreeTab: String

    val nextCourse: String
    val previousCourse: String

    val nextChapter: String
    val previousChapter: String

    val lessonTasks: String
    val lessonComments: String
    val nextLesson: String
    val previousLesson: String
    val shareLessonButtonLabel: String

    val articleComments: String

    val readCodeReviews: String
    val submitCodeReview: String

}
