package online.javanese.repository

import online.javanese.Uuid
import online.javanese.model.Lesson
import online.javanese.model.LessonDao

class LessonRepository internal constructor(
        private val lessonDao: LessonDao,
        private val taskRepo: TaskRepository
) {

    fun findTreeSortedBySortIndex(chapter: ChapterTree): List<LessonTree> =
            lessonDao.findBasicSortedBySortIndex(chapter.id).map { LessonTree(
                    id = it.id,
                    chapterId = it.chapterId,
                    urlPathComponent = it.urlPathComponent,
                    linkText = it.linkText,
                    chapter = chapter,
                    tasks = taskRepo::findTreeSortedBySortIndex
            ) }

    fun findById(id: Uuid): Lesson? =
            lessonDao.findById(id)

}

class LessonTree internal constructor(
        val id: Uuid,
        val chapterId: Uuid,
        val urlPathComponent: String,
        val linkText: String,
        val chapter: ChapterTree,
        tasks: (LessonTree) -> List<TaskTree>
) {

    val tasks = tasks(this)

}
