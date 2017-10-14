package online.javanese.repository

import online.javanese.Uuid
import online.javanese.model.Task
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
                        urlPathComponent = it.urlPathComponent,
                        task = { taskDao.findById(it)!! }
                )
            }

}

class TaskTree internal constructor(
        val id: Uuid,
        val lessonId: Uuid,
        val lesson: LessonTree,
        val linkText: String,
        val urlPathComponent: String,
        task: (Uuid) -> Task
) {

    val task by lazy { task(id) }

}
