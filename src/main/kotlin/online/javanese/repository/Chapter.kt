package online.javanese.repository

import online.javanese.Uuid
import online.javanese.model.Chapter
import online.javanese.model.ChapterDao

class ChapterRepository internal constructor(
        private val chapterDao: ChapterDao,
        private val lessonRepo: LessonRepository
) {

    fun findTreeSortedBySortIndex(course: CourseTree): List<ChapterTree> =
            chapterDao
                    .findBasicSortedBySortIndex(course.id)
                    .map { it.toTree(course) }

    /*fun findTreeById(chapter: Chapter): ChapterTree? =
            chapterDao // ah, lol, it requires whole graph to be fetched
                    .findBasicById(chapter.basicInfo.id)
                    ?.toTree(chapter)*/

    fun findByUrlComponent(component: String): Chapter? =
            chapterDao.findByUrlComponent(component)

    private fun Chapter.BasicInfo.toTree(course: CourseTree) = ChapterTree(
            id = id,
            courseId = courseId,
            urlPathComponent = urlPathComponent,
            linkText = linkText,
            course = course,
            lessons = lessonRepo::findTreeSortedBySortIndex
    )

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
