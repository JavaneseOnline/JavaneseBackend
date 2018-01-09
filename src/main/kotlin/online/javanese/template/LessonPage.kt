package online.javanese.template

import kotlinx.html.*
import online.javanese.model.*
import java.util.*

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
        private val messages: Properties
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
                    previous, next, urlOfLesson, LessonTree::linkText,
                    messages.getProperty("lesson.previous"), messages.getProperty("lesson.next")
            )
        }

        cardWithTabs {
            val hasTasks = !lessonTree.tasks.isEmpty()

            tabBar {
                if (hasTasks) {
                    tabLink(id = "tasks", text = messages.getProperty("lesson.tasks"), active = true)
                }
                tabLink(id = "comments", text = messages.getProperty("lesson.comments"), active = !hasTasks)
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

                                        +messages.getProperty("sandbox.run")
                                    }

                                    raisedMaterialButton(type = ButtonType.button, moreClasses = "content-margin") {
                                        attributes["v-on:click"] = "reportError"

                                        +messages.getProperty("sandbox.reportError")
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
                    previous, next, urlOfLesson, LessonTree::linkText,
                    messages.getProperty("lesson.previous"), messages.getProperty("lesson.next")
            )
        }
    }

    override fun scripts(body: BODY) = with(body) {
        materialDialog(id = "sandbox_reportError") {
            materialDialogTitle {
                +messages.getProperty("sandbox.errorReport.caption")
            }

            // todo: URL as object
            form(action = "/task/report", method = FormMethod.post) {
                attributes["data-success-message"] = messages.getProperty("sandbox.errorReport.successMessage")
                attributes["data-error-message"] = messages.getProperty("sandbox.errorReport.errorMessage")

                materialDialogBody {

                    radio(name = "errorKind", value = TaskErrorReport.ErrorKind.RightSolutionNotAccepted.name) {
                        +messages.getProperty("sandbox.errorKind.rightSolutionNotAccepted")
                    }

                    radio(name = "errorKind", value = TaskErrorReport.ErrorKind.BadCondition.name) {
                        +messages.getProperty("sandbox.errorKind.badCondition")
                    }

                    radio(name = "errorKind", value = TaskErrorReport.ErrorKind.InsufficientMaterial.name) {
                        +messages.getProperty("sandbox.errorKind.insufficientMaterial")
                    }

                    radio(name = "errorKind", value = TaskErrorReport.ErrorKind.Other.name) {
                        +messages.getProperty("sandbox.errorKind.other")
                    }

                    div(classes = "mdl-textfield mdl-js-textfield") {
                        textArea(classes = "mdl-textfield__input") {
                            id = "sandbox_errorReport_text"
                            name = "text"
                        }
                        label(classes = "mdl-textfield__label") {
                            for_ = "sandbox_errorReport_text"
                            +messages.getProperty("sandbox.errorReport.text.placeholder")
                        }
                    }

                    hiddenInput(name = "code")
                    hiddenInput(name = "taskId")
                }

                materialDialogActions {
                    materialButton(ButtonType.submit) {
                        +messages.getProperty("sendButton")
                    }

                    materialButton(ButtonType.button, moreClasses = "close") {
                        +messages.getProperty("cancelButton")
                    }
                }
            }
        }

        script(src = "$static/sandbox/codemirror_clike_sandbox.min.js")
        script {
            +"var sandboxLocale = {"
            pair("exitCode", messages, "sandbox.exitCode")
            pair("noRequiredVar", messages, "sandbox.varNotFound")
            pair("noRequiredEq", messages, "sandbox.eqNotFound")
            pair("illegalOutput", messages, "sandbox.illegalOutput")
            pair("correctSolution", messages, "sandbox.correctSolution")
            pair("notMatches", messages, "sandbox.notMatches")
            pair("webSocketError", messages, "sandbox.webSocketError", false)
            +"};"
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun SCRIPT.pair(key: String, messages: Properties, message: String, comma: Boolean = true) {
        unsafe {
            +key
            +":'"
            +messages.getProperty(message)
            +"'"
            if (comma)
                +","
        }
    }

}
