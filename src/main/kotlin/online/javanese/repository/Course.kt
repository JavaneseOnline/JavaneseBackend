package online.javanese.repository

import online.javanese.Uuid
import online.javanese.model.CourseDao

class CourseRepository internal constructor(
        private val courseDao: CourseDao,
        private val chapterRepo: ChapterRepository
) {

    fun findTreeSortedBySortIndex(): List<CourseTree> =
            courseDao
                    .findAllBasicSortedBySortIndex()
                    .map { CourseTree(
                            id = it.id,
                            urlPathComponent = it.urlPathComponent,
                            linkText = it.linkText,
                            chapters = chapterRepo::findTreeSortedBySortIndex
                    ) }

}

class CourseTree internal constructor(
        val id: Uuid,
        val urlPathComponent: String,
        val linkText: String,
        chapters: (CourseTree) -> List<ChapterTree>
) {

    val chapters = chapters(this)

}
