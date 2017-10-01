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
                    .map { CourseTree(it.id, it.urlPathComponent, it.linkText, chapterRepo.findTreeSortedBySortIndex(it)) }


}

class CourseTree(
        val id: Uuid,
        val urlPathComponent: String,
        val linkText: String,
        val chapters: List<ChapterTree>
)
