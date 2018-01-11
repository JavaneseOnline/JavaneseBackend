package online.javanese

import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.mapper.Dao
import online.javanese.krud.AdminPanel
import online.javanese.krud.RoutedModule
import online.javanese.krud.crud.Crud
import online.javanese.krud.kwery.*
import online.javanese.krud.stat.HardwareStat
import online.javanese.krud.stat.HitStat
import online.javanese.krud.template.MaterialTemplate
import online.javanese.krud.template.control.*
import online.javanese.model.*


fun JavaneseAdminPanel(
        adminRoute: String,
        session: Session,
        taskDao: Dao<Task, Uuid>, lessonDao: Dao<Lesson, Uuid>, chapterDao: Dao<Chapter, Uuid>,
        courseDao: Dao<Course, Uuid>, articleDao: Dao<Article, Uuid>, pageDao: Dao<Page, Uuid>,
        taskErrorReportDao: Dao<TaskErrorReport, Uuid>, codeReviewCandidateDao: Dao<CodeReviewCandidate, Uuid>,
        codeReviewDao: Dao<CodeReview, Uuid>,
        hitStat: HitStat
): AdminPanel {

    val escape = session.dialect::escapeName
    val uuidGenerator = UuidGeneratingSource("id")

    return AdminPanel("/$adminRoute", MaterialTemplate("/$adminRoute/", "/admin-static"),
            RoutedModule("crud", Crud(
                    KweryTable("task",
                            TaskTable, taskDao,
                            SelectCount(session, session.dialect.escapeName(TaskTable.name)),
                            getTitleOf = TaskTable.LinkText.property,
                            transformColumn = { when (it) {
                                TaskTable.LessonId -> EnumeratedCol(TaskTable.LessonId, KweryForeignEnumeratedColAdapter(LessonTable, lessonDao, LessonTable.LinkText.property))
                                TaskTable.Condition -> TextCol(TaskTable.Condition, createControlFactory = online.javanese.krud.template.control.CodeMirror.Html)
                                TaskTable.InitialCode -> TextCol(TaskTable.InitialCode, createControlFactory = TextArea)
                                TaskTable.CodeToAppend -> TextCol(TaskTable.CodeToAppend, createControlFactory = TextArea)
                                TaskTable.CheckRules -> TextCol(TaskTable.CheckRules, createControlFactory = TextArea)
                                TaskTable.ExpectedOutput -> TextCol(TaskTable.ExpectedOutput, createControlFactory = TextArea)
                                TaskTable.SortIndex -> TextCol(TaskTable.SortIndex, createControlFactory = online.javanese.krud.template.control.TextInput.Editable, editControlFactory = online.javanese.krud.template.control.TextInput.ReadOnly)
                                else -> KweryTable.TransformKweryColumn<Task>()(it)
                            } },
                            sort = KweryExplicitSort(session, escape, TaskTable, TaskTable.SortIndex),
                            fallbackSource = uuidGenerator
                    ),
                    KweryTable("lesson",
                            LessonTable, lessonDao,
                            SelectCount(session, session.dialect.escapeName(LessonTable.name)),
                            getTitleOf = LessonTable.LinkText.property,
                            transformColumn = { when (it) {
                                LessonTable.ChapterId -> EnumeratedCol(LessonTable.ChapterId, KweryForeignEnumeratedColAdapter(ChapterTable, chapterDao, ChapterTable.LinkText.property))
                                LessonTable.BodyMarkup -> TextCol(LessonTable.BodyMarkup, createControlFactory = online.javanese.krud.template.control.CodeMirror.Html)
                                LessonTable.SortIndex -> TextCol(LessonTable.SortIndex, createControlFactory = online.javanese.krud.template.control.TextInput.Editable, editControlFactory = online.javanese.krud.template.control.TextInput.ReadOnly)
                                else -> KweryTable.TransformKweryColumn<Lesson>()(it)
                            } },
                            sort = KweryExplicitSort(session, escape, LessonTable, LessonTable.SortIndex),
                            fallbackSource = uuidGenerator
                    ),
                    KweryTable("chapter",
                            ChapterTable, chapterDao,
                            SelectCount(session, session.dialect.escapeName(ChapterTable.name)),
                            getTitleOf = ChapterTable.LinkText.property,
                            transformColumn = { when (it) {
                                ChapterTable.CourseId -> EnumeratedCol(ChapterTable.CourseId, KweryForeignEnumeratedColAdapter(CourseTable, courseDao, CourseTable.LinkText.property))
                                ChapterTable.Description -> TextCol(ChapterTable.Description, createControlFactory = online.javanese.krud.template.control.CodeMirror.Html)
                                ChapterTable.SortIndex -> TextCol(ChapterTable.SortIndex, createControlFactory = online.javanese.krud.template.control.TextInput.Editable, editControlFactory = online.javanese.krud.template.control.TextInput.ReadOnly)
                                else -> KweryTable.TransformKweryColumn<Chapter>()(it)
                            } },
                            sort = KweryExplicitSort(session, escape, ChapterTable, ChapterTable.SortIndex),
                            fallbackSource = uuidGenerator
                    ),
                    KweryTable("course",
                            CourseTable, courseDao,
                            SelectCount(session, session.dialect.escapeName(CourseTable.name)),
                            getTitleOf = CourseTable.LinkText.property,
                            transformColumn = { when (it) {
                                CourseTable.Description -> TextCol(CourseTable.Description, createControlFactory = online.javanese.krud.template.control.CodeMirror.Html)
                                CourseTable.SortIndex -> TextCol(CourseTable.SortIndex, createControlFactory = online.javanese.krud.template.control.TextInput.Editable, editControlFactory = online.javanese.krud.template.control.TextInput.ReadOnly)
                                else -> KweryTable.TransformKweryColumn<Course>()(it)
                            } },
                            sort = KweryExplicitSort(session, escape, CourseTable, CourseTable.SortIndex),
                            fallbackSource = uuidGenerator
                    ),
                    KweryTable("article",
                            ArticleTable, articleDao,
                            SelectCount(session, session.dialect.escapeName(ArticleTable.name)),
                            getTitleOf = ArticleTable.LinkText.property,
                            transformColumn = { when (it) {
                                ArticleTable.BodyMarkup -> TextCol(ArticleTable.BodyMarkup, createControlFactory = online.javanese.krud.template.control.CodeMirror.Html)
                                else -> KweryTable.TransformKweryColumn<Article>()(it)
                            } },
                            fallbackSource = uuidGenerator
                    ),
                    KweryTable("page",
                            PageTable, pageDao,
                            SelectCount(session, session.dialect.escapeName(PageTable.name)),
                            getTitleOf = PageTable.H1.property,
                            transformColumn = { when (it) {
                                PageTable.BodyMarkup -> TextCol(PageTable.BodyMarkup, createControlFactory = online.javanese.krud.template.control.CodeMirror.Html)
                                PageTable.HeadMarkup -> TextCol(PageTable.HeadMarkup, createControlFactory = online.javanese.krud.template.control.CodeMirror.Html)
                                PageTable.BeforeBodyEndMarkup -> TextCol(PageTable.BeforeBodyEndMarkup, createControlFactory = online.javanese.krud.template.control.CodeMirror.Html)
                                else -> KweryTable.TransformKweryColumn<Page>()(it)
                            } },
                            fallbackSource = uuidGenerator
                    ),
                    KweryTable("taskErrorReport", // todo: make read-only
                            TaskErrorReportTable, taskErrorReportDao,
                            SelectCount(session, session.dialect.escapeName(TaskErrorReportTable.name)),
                            getTitleOf = { it.errorKind.name },
                            transformColumn = { when (it) {
                                TaskErrorReportTable.Code -> TextCol(TaskErrorReportTable.Code, createControlFactory = TextArea)
                                TaskErrorReportTable.Text -> TextCol(TaskErrorReportTable.Text, createControlFactory = TextArea)
                                else -> KweryTable.TransformKweryColumn<TaskErrorReport>()(it)
                            } },
                            fallbackSource = uuidGenerator
                    ),
                    KweryTable("codeReviewCandidate", // todo: make read-only
                            CodeReviewCandidateTable, codeReviewCandidateDao,
                            SelectCount(session, session.dialect.escapeName(CodeReviewCandidateTable.name)),
                            getTitleOf = CodeReviewCandidateTable.SenderName.property,
                            transformColumn = { when (it) {
                                CodeReviewCandidateTable.ProblemStatement -> TextCol(CodeReviewCandidateTable.ProblemStatement, createControlFactory = TextArea)
                                CodeReviewCandidateTable.Code -> TextCol(CodeReviewCandidateTable.Code, createControlFactory = TextArea)
                                else -> KweryTable.TransformKweryColumn<CodeReviewCandidate>()(it)
                            } },
                            fallbackSource = uuidGenerator
                    ),
                    KweryTable("codeReview",
                            CodeReviewTable, codeReviewDao,
                            SelectCount(session, session.dialect.escapeName(CodeReviewTable.name)),
                            getTitleOf = CodeReviewTable.MetaTitle.property,
                            transformColumn = { when (it) {
                                CodeReviewTable.ProblemStatement -> TextCol(CodeReviewTable.ProblemStatement, createControlFactory = TextArea)
                                CodeReviewTable.ReviewMarkup -> TextCol(CodeReviewTable.ReviewMarkup, createControlFactory = CodeMirror.Html)
                                else -> KweryTable.TransformKweryColumn<CodeReview>()(it)
                            } },
                            fallbackSource = uuidGenerator
                    )
            )),
            RoutedModule("hw", HardwareStat()),
            RoutedModule("hits", hitStat)
    )
}
