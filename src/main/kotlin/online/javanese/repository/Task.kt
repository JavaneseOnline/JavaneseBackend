package online.javanese.repository

import online.javanese.Uuid
import online.javanese.model.TaskDao

class TaskRepository internal constructor(
        private val taskDao: TaskDao
) {

    fun findTreeSortedBySortIndex(lesson: LessonTree) =
            taskDao.findBasicSortedBySortIndex(lesson.id).map {
                TaskTree(
                        id = it.id,
                        lessonId = it.lessonId,
                        lesson = lesson,
                        linkText = it.linkText,
                        urlPathComponent = it.urlPathComponent
                )
            }

}

class TaskTree(
        val id: Uuid,
        val lessonId: Uuid,
        val lesson: LessonTree,
        val linkText: String,
        val urlPathComponent: String
)
