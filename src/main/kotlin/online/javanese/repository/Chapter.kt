package online.javanese.repository

import online.javanese.Uuid
import online.javanese.model.ChapterDao
import online.javanese.model.Course

class ChapterRepository internal constructor(
        private val chapterDao: ChapterDao
) {

    fun findTreeSortedBySortIndex(course: Course.BasicInfo): List<ChapterTree> =
            chapterDao.findBasicSortedBySortIndex(course)
                    .map { ChapterTree(
                            id = it.id,
                            courseId = it.courseId,
                            urlPathComponent = it.urlPathComponent,
                            linkText = it.linkText,
                            course = course
                    ) }

}

class ChapterTree(
        val id: Uuid,
        val courseId: Uuid,
        val urlPathComponent: String,
        val linkText: String,
        val course: Course.BasicInfo
        // todo: lessons
)
