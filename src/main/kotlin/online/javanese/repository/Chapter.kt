package online.javanese.repository

import online.javanese.Uuid
import online.javanese.model.ChapterDao

class ChapterRepository internal constructor(
        private val chapterDao: ChapterDao,
        private val lessonRepo: LessonRepository
) {

    fun findTreeSortedBySortIndex(course: CourseTree): List<ChapterTree> =
            chapterDao.findBasicSortedBySortIndex(course.id)
                    .map { ChapterTree(
                            id = it.id,
                            courseId = it.courseId,
                            urlPathComponent = it.urlPathComponent,
                            linkText = it.linkText,
                            course = course,
                            lessons = lessonRepo::findTreeSortedBySortIndex
                    ) }

}

class ChapterTree internal constructor(
        val id: Uuid,
        val courseId: Uuid,
        val urlPathComponent: String,
        val linkText: String,
        val course: CourseTree,
        lessons: (ChapterTree) -> List<LessonTree>
) {

    val lessons = lessons(this)

}
