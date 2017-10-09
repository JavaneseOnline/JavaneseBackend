package online.javanese.template

import online.javanese.model.Course
import online.javanese.repository.CourseTree

class CoursePageTemplate(
        private val render: (String, Map<String, Any>) -> String
) : (Course, CourseTree, Course.BasicInfo?, Course.BasicInfo?) -> String {

    override fun invoke(
            course: Course, courseTree: CourseTree, previous: Course.BasicInfo?, next: Course.BasicInfo?
    ): String {
        val map = HashMap<String, Any>(4)
        map.put("course", course)
        map.put("courseTree", courseTree)
        previous?.let { map.put("previousCourse", it) }
        next?.let { map.put("nextCourse", it) }

        return render("course", map)
    }

}
