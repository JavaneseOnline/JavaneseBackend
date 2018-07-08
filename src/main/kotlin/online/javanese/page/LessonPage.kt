package online.javanese.page

import kotlinx.html.*
import online.javanese.link.Action
import online.javanese.link.HtmlBlock
import online.javanese.link.Link
import online.javanese.link.LinkWithFragment
import online.javanese.locale.Language
import online.javanese.model.*


class LessonPage(
        private val lesson: Lesson,
        private val tasks: List<Task>,
        private val previousAndNext: Pair<Lesson.BasicInfo?, Lesson.BasicInfo?>,
        private val static: String,
        private val lessonLink: Link<Lesson.BasicInfo, *>,
        private val reportTaskAction: Action<Unit, *>,
        private val language: Language,
        private val sandboxScript: String,
        private val codeMirrorStylePath: String,
        private val beforeContent: HtmlBlock
) : Layout.Page {

    override val meta: Meta get() = lesson.meta

    override fun additionalHeadMarkup(head: HEAD) = with(head) {
        styleLink("$static/$codeMirrorStylePath")
    }

    override fun bodyMarkup(body: BODY) = with(body) {
        contentCardMain {

            beforeContent.render(this)

            h1(classes = "content-padding-v") {
                +lesson.heading
            }

            if (lesson.programmingLanguages.size > 1) {
                fieldSet(classes = "content-padding-v") {
                    id = "languageSwitcher"
                    attributes["data-languages"] = lesson.programmingLanguages.joinToString("|")

                    legend { +"Язык примеров" }
                    form {
                        lesson.programmingLanguages.forEach {
                            radio("code-language", input = {
                                attributes["v-model"] = "codeLanguage"
                                attributes["v-on:change"] = "codeLanguageChanged"
                                value = it.name
                            }) { +it.name }
                        }
                    }
                }
            }

            article { // language chooser depends on this element
                unsafe {
                    +lesson.bodyMarkup
                }
            }

            section(classes = "content-padding-v") {
                vkOpenApiScript()
                vkShareScript()

                vkLikeButton()

                // Put this script tag to the place, where the Share button will be
                div {
                    style = "display: inline-block"
                    script {
                        unsafe {
                            +VkInitWidgetsScriptLine
                            +initVkWidgetJs(lesson.basicInfo.id.toString())
                            +documentWriteVkShareButton(language.shareLessonButtonLabel)
                        }
                    }
                }
            }

            prevNextPane(previousAndNext, lessonLink, language.previousLesson, language.nextLesson)
        }

        cardWithTabs {
            val hasTasks = !tasks.isEmpty()

            tabBar {
                if (hasTasks) {
                    tabLink(id = "tasks", text = language.lessonTasks, active = true)
                }
                tabLink(id = "comments", text = language.lessonComments, active = !hasTasks)
            }

            if (hasTasks) {
                tabPanelSection(id = "tasks", active = true, moreClasses = "no-pad") {
                    tasks.forEach { task ->
                        section(classes = "content") {
                            h3(classes = "no-pad-top") {
                                id = task.basicInfo.urlPathComponent

                                +task.heading
                            }

                            unsafe {
                                +task.condition
                            }

                            div(classes = "sandbox no-pad mdl-grid mdl-grid--no-spacing") {
                                attributes["data-task"] = task.basicInfo.id.toString()

                                div(classes = "mdl-cell mdl-cell--12-col-tablet mdl-cell--7-col") {

                                    textArea(classes = "editor") {
                                        +task.technicalInfo.initialCode
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
                                        pre {
                                            attributes["v-for"] = "message in messages"
                                            attributes["v-bind:class"] = "'content-padding runtime-' + message.type.toLowerCase()"

                                            +"{{ message.data }}"
                                        }
                                    }

                                    if (task.technicalInfo.allowSystemIn) {
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

            prevNextPane(previousAndNext, lessonLink, language.previousLesson, language.nextLesson)
        }
    }

    override fun scripts(body: BODY) = with(body) {

        val msg = language.sandbox.frontendMessages

        materialDialog(id = "sandbox_reportError") {
            materialDialogTitle {
                +msg.reportError
            }

            reportTaskAction.renderForm(this, Unit) {
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
                            htmlFor = "sandbox_errorReport_text"
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

        script(src = "$static/$sandboxScript") {}
        script {
            val rt = language.sandbox.runtimeMessages

            unsafe { +"var sandboxLocale = {" }
            pair("compiling", rt.compiling)
            pair("compiled", rt.compiled)
            pair("exitCode", rt.exitCode)
            pair("noRequiredVar", rt.varNotFound)
            pair("noRequiredEq", rt.eqNotFound)
            pair("illegalOutput", rt.illegalOutput)
            pair("correctSolution", rt.correctSolution)
            pair("notMatches", rt.notMatches)
            pair("webSocketError", msg.webSocketError, false)
            unsafe { +"};" }
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
