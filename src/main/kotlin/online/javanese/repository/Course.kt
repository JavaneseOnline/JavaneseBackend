package online.javanese.repository

import online.javanese.Uuid
import online.javanese.model.ChapterDao
import online.javanese.model.ChapterTree
import online.javanese.model.Course
import online.javanese.model.CourseDao

class CourseRepository internal constructor(
        private val courseDao: CourseDao,
        private val chapterDao: ChapterDao
) {

    fun findTreeSortedBySortIndex(): List<CourseTree> =
            courseDao
                    .findAllBasicSortedBySortIndex()
                    .map { toTree(it) }

    fun findTree(courseId: Uuid): CourseTree? =
            courseDao
                    .findBasicById(courseId)
                    ?.let { toTree(it) }

    fun findByUrlComponent(component: String): Course? =
            courseDao.findByUrlComponent(component)

    fun findPrevious(course: Course): Course.BasicInfo? =
            courseDao.findPrevious(course)

    fun findNext(course: Course): Course.BasicInfo? =
            courseDao.findNext(course)

    private fun toTree(info: Course.BasicInfo): CourseTree = CourseTree(
            id = info.id,
            urlPathComponent = info.urlPathComponent,
            linkText = info.linkText,
            chapters = chapterDao::findTreeSortedBySortIndex
    )

}

class CourseTree internal constructor(
        val id: Uuid,
        val urlPathComponent: String,
        val linkText: String,
        chapters: (CourseTree) -> List<ChapterTree>
) {

    val chapters = chapters(this)

}
