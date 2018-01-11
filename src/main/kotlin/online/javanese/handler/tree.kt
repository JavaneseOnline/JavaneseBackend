package online.javanese.handler

import online.javanese.model.*


typealias Courses = List<Pair<Course.BasicInfo, Chapters>>
typealias Chapters = List<Pair<Chapter.BasicInfo, Lessons>>
typealias Lessons = List<Pair<Lesson.BasicInfo, Tasks>>
typealias Tasks = List<Task.BasicInfo>


fun courses(courseDao: CourseDao, chapterDao: ChapterDao, lessonDao: LessonDao, taskDao: TaskDao): Courses =
        courseDao.findAllBasicSorted().map { course ->
            course to chapters(course, chapterDao, lessonDao, taskDao)
        }

fun chapters(course: Course.BasicInfo, chapterDao: ChapterDao, lessonDao: LessonDao, taskDao: TaskDao): Chapters =
        chapterDao.findAllBasicSorted(course.id).map { chapter ->
                    chapter to lessons(chapter, lessonDao, taskDao)
                }

fun lessons(chapter: Chapter.BasicInfo, lessonDao: LessonDao, taskDao: TaskDao): Lessons =
        lessonDao.findAllBasicSorted(chapter.id).map { lesson ->
                    lesson to tasks(lesson, taskDao)
                }

fun tasks(lesson: Lesson.BasicInfo, taskDao: TaskDao): Tasks =
        taskDao.findAllBasicSorted(lesson.id)
