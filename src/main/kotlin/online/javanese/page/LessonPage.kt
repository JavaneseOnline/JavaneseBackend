package online.javanese.page

import kotlinx.html.*
import online.javanese.locale.Language
import online.javanese.model.*

class LessonPage(
        private val index: Page,
        private val treePage: Page,
        private val course: Course.BasicInfo,
        private val chapter: Chapter.BasicInfo,
        private val lesson: Lesson,
        private val lessonTree: LessonTree,
        private val previous: LessonTree?,
        private val next: LessonTree?,
        private val static: String,
        private val pageLink: Link<Page>,
        private val courseLink: Link<Course.BasicInfo>,
        private val chapterLink: Link<Pair<Course.BasicInfo, Chapter.BasicInfo>>,
        private val urlOfLesson: (LessonTree) -> String,
        private val language: Language
) : Layout.Page {

    override val meta: Meta get() = lesson.meta

    override fun additionalHeadMarkup(head: HEAD) = with(head) {
        styleLink("$static/sandbox/codemirror_ambiance.min.css")
    }

    override fun bodyMarkup(body: BODY) = with(body) {
        contentCardMain {
            nav {
                pageLink.insert(this, index)
                +" / "
                pageLink.insert(this, treePage)
                +" / "
                courseLink.insert(this, course)
                +" / "
                chapterLink.insert(this, course to chapter)
            }

            h1(classes = "content-padding-v") {
                +lesson.h1
            }

            article {
                unsafe {
                    +lesson.bodyMarkup
                }
            }

            section(classes = "content-padding-v") {
                script(src = "https://vk.com/js/api/openapi.js?136")
                script(src = "https://vk.com/js/api/share.js?94") {
                    charset = "windows-1251"
                }
                div {
                    id ="vk_like"
                    style = "display: inline-block"
                }

                // Put this script tag to the place, where the Share button will be
                div {
                    style = "display: inline-block"
                    script {
                        unsafe {
                            +VkInitWidgetsScriptLine
                            +"VK.Widgets.Like('vk_like', { type: 'button' }, '${lesson.basicInfo.id}');"
                            +"document.write(VK.Share.button(false, { type: 'round', text: 'Сохранить' }));"
                        }
                    }
                }
            }

            prevNextPane(
                    previous, next, urlOfLesson, language.previousLesson,
                    language.nextLesson
            )
        }

        cardWithTabs {
            val hasTasks = !lessonTree.tasks.isEmpty()

            tabBar {
                if (hasTasks) {
                    tabLink(id = "tasks", text = language.lessonTasks, active = true)
                }
                tabLink(id = "comments", text = language.lessonComments, active = !hasTasks)
            }

            if (hasTasks) {
                tabPanelSection(id = "tasks", active = true, moreClasses = "no-pad") {
                    lessonTree.tasks.forEach { task ->
                        section(classes = "content") {
                            h3(classes = "no-pad-top") {
                                id = task.urlPathComponent

                                +task.task.heading
                            }

                            unsafe {
                                +task.task.condition
                            }

                            div(classes = "sandbox no-pad mdl-grid mdl-grid--no-spacing") {
                                attributes["data-task"] = task.id.toString()

                                div(classes = "mdl-cell mdl-cell--12-col-tablet mdl-cell--7-col") {

                                    textArea(classes = "editor") {
                                        +task.task.technicalInfo.initialCode
                                    }

                                    colouredRaisedMaterialButton(type = ButtonType.submit, moreClasses = "content-margin m-r-0") {
                                        attributes["v-on:click"] = "run"
                                        attributes["v-bind:disabled"] = "running"

                                        +language.sandbox.frontendMessages.run
                                    }

                                    raisedMaterialButton(type = ButtonType.button, moreClasses = "content-margin") {
                                        attributes["v-on:click"] = "reportError"

                                        +language.sandbox.frontendMessages.reportError
                                    }
                                }

                                div(classes = "mdl-cell mdl-cell--12-col-tablet mdl-cell--5-col ") {
                                    div(classes = "content-padding-top messages") {
                                        onClick = "$(this).closest('.sandbox').find('.cin').focus();"
                                        pre(classes = "content-padding runtime-{{ message.type | lowercase }}") {
                                            attributes["v-for"] = "message in messages"

                                            +"{{ message.data }}"
                                        }
                                    }

                                    if (task.task.technicalInfo.allowSystemIn) {
                                        form {
                                            attributes["v-on:submit"] = "send"
                                            input(type = InputType.text, classes = "cin") {
                                                placeholder = "System.in"
                                                attributes["v-bind:readonly"] = "!running"
                                                attributes["v-model"] = "systemIn"
                                            }

                                            input(type = InputType.submit) {
                                                value = "⏎"
                                                attributes["v-bind:disabled"] = "!running"
                                            }
                                        }
                                    }
                                }
                            }

                            hr(classes = "no-pad")
                        }
                    }
                }
            }

            tabPanelSection(id = "comments", active = !hasTasks, moreClasses = "no-pad content-padding-v") {
                vkComments(lesson.basicInfo.id.toString(), init = false)
            }

            prevNextPane(
                    previous, next, urlOfLesson, language.previousLesson,
                    language.nextLesson
            )
        }
    }

    override fun scripts(body: BODY) = with(body) {

        val msg = language.sandbox.frontendMessages

        materialDialog(id = "sandbox_reportError") {
            materialDialogTitle {
                +msg.reportError
            }

            // todo: URL as object
            form(action = "/task/report", method = FormMethod.post) {
                attributes["data-success-message"] = msg.errorReportedSuccessfully
                attributes["data-error-message"] = msg.errorNotReported

                materialDialogBody {

                    TaskErrorReport.ErrorKind.values().forEach { kind ->
                        radio(name = "errorKind", value = kind.name) {
                            +msg.errorKind(kind)
                        }
                    }

                    div(classes = "mdl-textfield mdl-js-textfield") {
                        textArea(classes = "mdl-textfield__input") {
                            id = "sandbox_errorReport_text"
                            name = "text"
                        }
                        label(classes = "mdl-textfield__label") {
                            for_ = "sandbox_errorReport_text"
                            +msg.errorDescription
                        }
                    }

                    hiddenInput(name = "code")
                    hiddenInput(name = "taskId")
                }

                materialDialogActions {
                    materialButton(ButtonType.submit) {
                        +language.sendButton
                    }

                    materialButton(ButtonType.button, moreClasses = "close") {
                        +language.cancelButton
                    }
                }
            }
        }

        script(src = "$static/sandbox/codemirror_clike_sandbox.min.js")
        script {
            val rt = language.sandbox.runtimeMessages

            +"var sandboxLocale = {"
            pair("compiling", rt.compiling)
            pair("compiled", rt.compiled)
            pair("exitCode", rt.exitCode)
            pair("noRequiredVar", rt.varNotFound)
            pair("noRequiredEq", rt.eqNotFound)
            pair("illegalOutput", rt.illegalOutput)
            pair("correctSolution", rt.correctSolution)
            pair("notMatches", rt.notMatches)
            pair("webSocketError", msg.webSocketError, false)
            +"};"
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun SCRIPT.pair(key: String, message: String, comma: Boolean = true) {
        unsafe {
            +key
            +":'"
            +message
            +"'"
            if (comma)
                +","
        }
    }

}
