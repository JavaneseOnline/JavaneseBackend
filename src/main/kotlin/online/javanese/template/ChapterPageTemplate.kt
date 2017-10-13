package online.javanese.template

import online.javanese.model.Chapter
import online.javanese.model.Course
import online.javanese.repository.ChapterTree

class ChapterPageTemplate(
        private val urlOfChapter: (ChapterTree) -> String,
        private val render: (templateName: String, args: Map<String, Any>) -> String
) : (Course, Chapter, ChapterTree, ChapterTree?, ChapterTree?) -> String {
// fixme: should be Chapter.BasicInfo ^          ^

    override fun invoke(
            course: Course, chapter: Chapter, chapterTree: ChapterTree, previous: ChapterTree?, next: ChapterTree?
    ): String {

        val params = hashMapOf(
                "course" to course,
                "chapter" to chapter,
                "chapterTree" to chapterTree,
                "urlOfChapter" to urlOfChapter
        )
        previous?.let { params["previousChapter"] = it }
        next?.let { params["nextChapter"] = it }

        return render("chapter", params)
    }

}
