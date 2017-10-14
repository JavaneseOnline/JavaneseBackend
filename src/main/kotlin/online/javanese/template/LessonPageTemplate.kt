package online.javanese.template

import online.javanese.model.Lesson
import online.javanese.repository.LessonTree
import online.javanese.repository.TaskTree

class LessonPageTemplate(
        private val urlOfLesson: (LessonTree) -> String,
        private val urlOfTask: (TaskTree) -> String,
        private val render: (templateName: String, args: Map<String, Any>) -> String
) : (Lesson, LessonTree, LessonTree?, LessonTree?) -> String {

    override fun invoke(lesson: Lesson, lessonTree: LessonTree, previous: LessonTree?, next: LessonTree?): String {
        val map = hashMapOf(
                "lesson" to lesson,
                "lessonTree" to lessonTree,
                "urlOfLesson" to urlOfLesson,
                "urlOfTask" to urlOfTask
        )

        previous?.let { map["previousLesson"] = previous }
        next?.let { map["nextLesson"] = next }

        return render("lesson", map)
    }

}
