package online.javanese.repository

import online.javanese.Uuid
import online.javanese.model.LessonDao

class LessonRepository internal constructor(
        private val lessonDao: LessonDao
) {

    fun findTreeSortedBySortIndex(chapter: ChapterTree): List<LessonTree> =
            lessonDao.findTreeSortedBySortIndex(chapter.id).map { LessonTree(
                    id = it.id,
                    chapterId = it.chapterId,
                    urlPathComponent = it.urlPathComponent,
                    linkText = it.linkText,
                    chapter = chapter
            ) }

}

class LessonTree(
        val id: Uuid,
        val chapterId: Uuid,
        val urlPathComponent: String,
        val linkText: String,
        val chapter: ChapterTree
)
