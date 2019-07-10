package online.javanese

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.application.ApplicationCall
import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.OAuthServerSettings
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.response.readText
import kotlinx.html.ButtonType
import kotlinx.html.FlowContent
import kotlinx.html.HTMLTag
import kotlinx.html.LI
import kotlinx.html.a
import kotlinx.html.address
import kotlinx.html.br
import kotlinx.html.div
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.small
import kotlinx.html.ul
import kotlinx.html.unsafe
import kotlinx.html.visit
import online.javanese.social.User
import online.javanese.social.Comments
import online.javanese.social.CommentsFor
import online.javanese.social.OAuthSource
import online.javanese.link.withFragment
import online.javanese.locale.Language
import online.javanese.model.ArticleTable
import online.javanese.model.LessonTable
import online.javanese.page.a
import online.javanese.page.colouredRaisedMaterialButton
import online.javanese.page.materialIconButton
import online.javanese.page.materialTextArea
import java.io.CharArrayWriter


private val jackson = ObjectMapper()
fun oauthSources(config: Config, client: HttpClient) = listOf(
    OAuthSource("Vk", null) { error("VK auth is not supported") },
    OAuthSource("GitHub", OAuthServerSettings.OAuth2ServerSettings(
            name = "GitHub",
            authorizeUrl = "https://github.com/login/oauth/authorize",
            accessTokenUrl = "https://github.com/login/oauth/access_token",
            clientId = config.gitHubClientId,
            clientSecret = config.gitHubClientSecret
    )) { tokenResponse ->
        client.call("https://api.github.com/user") {
            headers.append("Accept", "application/vnd.github.v3+json")
            headers.append("Authorization", "token " + when (tokenResponse) {
                is OAuthAccessTokenResponse.OAuth1a -> tokenResponse.token // unexpected
                is OAuthAccessTokenResponse.OAuth2 -> tokenResponse.accessToken // expected
            })
        }.response.readText()
                .let(jackson::readTree)
                .let { node ->
                    User(
                            id = node.get("login").textValue(),
                            displayName = node.get("name").textValue() ?: node.get("login").textValue(),
                            avatarUrl = node.get("avatar_url").textValue(),
                            pageUrl = node.get("html_url").textValue(),
                            source = "GitHub"
                    )
                }
    }
).associateBy(OAuthSource::name)


fun <E> comments(
        lang: Language.Comments,
        call: ApplicationCall, comments: Comments, join: CommentsFor<E>,
        entity: E, redirectUri: String
): FlowContent.(fragment: String) -> Unit {
    val current = comments.userSessions.currentUser(call)
    val tree = comments.fetch(join, entity)

    return { fragment ->
        val redirectUriWFragment = redirectUri.withFragment(fragment)

        div(classes = "comments") {
            val output = CharArrayWriter()
            val generator = jackson.factory.createGenerator(output)

            generator.writeStartObject()

            generator.writeFieldName("type")
            generator.writeString(join.joinTableName)

            generator.writeFieldName("id")
            generator.writeString(join.idOf(entity).toString())

            generator.writeFieldName("comments")
            comments.writeCommentTree(generator, tree, current)

            /*if (current != null) {
                generator.writeFieldName("current")
                comments.writeUser(generator, current)
            }*/

            generator.writeEndObject()

            generator.close()

            +output.toString()

            script(type = "x-template") {
                attributes["id"] = "commentsTemplate"

                div(classes = "comments") {
                    ul {
                        unsafe { +"""<v-comment
                        | v-for="comment in comments" :key="comment.id"
                        | v-bind:v-comment="comment" v-bind:v-type="type" v-bind:v-id="id" v-bind:v-users="users"
                        | v-on:comment-removed="onCommentRemoved" />""".trimMargin()
                        }
                    }

                    // add a top-level comment
                    unsafe { +"""<v-comments-form v-bind:v-type="type" v-bind:v-id="id" v-on:comment-added="onCommentAdded" />""" }
                }
            }

            script(type = "x-template") {
                attributes["id"] = "commentTemplate"

                LI(mapOf("class" to "comment"), consumer).visit {
                    attributes["v-bind:id"] = "'comments/' + vComment.id" // see a[href] below; tabs-related code is in scroll_unfocus_tabs.js

                    address {
                        attributes["v-if"] = "!vComment.removed"

                        /*img {
                            attributes["v-if"] = "commenterAvatarUrl != null"
                            attributes["v-bind:src"] = "commenterAvatarUrl"
                            width = "36"
                            height = "36"
                        }*/

                        +lang.author("{{vComment.authorSrc}}", "{{vComment.authorId}}") // todo: link
                    }
                    div {
                        attributes["v-if"] = "!vComment.removed"
                        attributes["v-html"] = "compiledCommentText"
                    }
                    div { small {
                        attributes["v-if"] = "vComment.removed"
                        +lang.removed
                    } }

                    a(classes = "dateTimeLink") {
                        attributes["v-bind:href"] = "'#comments/' + vComment.id"
                        +"{{readableCreationDate}}"
                    }

                    materialIconButton(ButtonType.button, icon = "reply") {
                        attributes["v-on:click"] = "reply"
                        attributes["v-bind:class"] = "{ 'mdl-button--primary': answering }"
                    }
                    materialIconButton(ButtonType.button, icon = "delete_forever") {
                        attributes["v-on:click"] = "remove"
                        attributes["v-if"] = "vComment.canRemove"
                        attributes["v-bind:disabled"] = "removing"
                        attributes["data-error-message"] = lang.removalFailed
                        formAction = comments.commentAction.url(Unit)
                    }

                    ul {
                        unsafe { +"""<v-comment v-for="comment in vComment.answers" :key="comment.id"
                        | v-bind:v-comment="comment" v-bind:v-type="vType" v-bind:v-id="vId" v-bind:v-users="vUsers"
                        | v-on:comment-removed="onCommentRemoved" />""".trimMargin() }

                        HTMLTag("transition", consumer, mapOf(), null, false, false).visit {
                            attributes["v-on:enter"] = "enter"

                            LI(mapOf(), consumer).visit {
                                attributes["v-if"] = "answering"

                                unsafe {
                                    +"""<v-comments-form v-bind:v-type="vType" v-bind:v-id="vId" v-bind:v-parent-id="vComment.id"
                                    | v-on:comment-added="onCommentAdded" />""".trimMargin()
                                }
                            }
                        }
                    }
                }
            }

            script(type = "x-template") {
                attributes["id"] = "commentsFormTemplate"

                if (current === null) {
                    p(classes = "answer") {
                        with(comments.userSessions) { authPrompt(lang.authPrompt, redirectUriWFragment) }
                    }
                } else {
                    comments.commentAction.renderForm(this, Unit, classes = "answer") {
                        attributes["data-error-message"] = lang.addFailed
                        attributes["v-on:submit"] = "send"

                        materialTextArea(null, "text", { unsafe { +lang.addPlaceholderHtml } },
                                "'commentText-' + vType + '-' + vId + '-' + vParentId", "text",
                                // this helps getting events instantaneously on mobile (tested in FF Nightly and Beta on Android):
                                areaOnInput = "text=\$event.target.value"
                        )

                        div(classes = "mdl-grid mdl-grid--no-spacing") {

                            p(classes = "mdl-cell mdl-cell--1-col-desktop mdl-cell--1-col-tablet mdl-cell--1-col-phone") {
                                img(src = current.avatarUrl) {
                                    width = "36"
                                    height = "36"
                                }
                            }

                            p(classes = "mdl-cell mdl-cell--5-col-desktop mdl-cell--3-col-tablet mdl-cell--1-col-phone") {
                                small { +current.displayName }
                                br()
                                a(href = comments.userSessions.logoutLink(redirectUriWFragment), titleAndText = "выйти")
                            }

                            p(classes = "mdl-cell mdl-cell--6-col-desktop mdl-cell--4-col-tablet mdl-cell--2-col-phone mdl-typography--text-right") {
                                colouredRaisedMaterialButton(ButtonType.submit) {
                                    attributes["v-bind:disabled"] = "sending || empty"

                                    +lang.add
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


val forArticle = CommentsFor("articleComments", ArticleTable.Id.property)
val forLesson = CommentsFor("lessonComments", LessonTable.Id.property)

/*
CREATE TABLE public."articleComments" (
	"entityId" uuid NOT NULL,
	"commentId" uuid NOT NULL,
	CONSTRAINT "articleComments_articles_fk" FOREIGN KEY ("entityId") REFERENCES public.articles("id"),
	CONSTRAINT "articleComments_comments_fk" FOREIGN KEY ("commentId") REFERENCES public.comments("id")
);
CREATE UNIQUE INDEX "articleComments_articleId_idx" ON public."articleComments" ("entityId", "commentId");

CREATE TABLE public."lessonComments" (
	"entityId" uuid NOT NULL,
	"commentId" uuid NOT NULL,
	CONSTRAINT "lessonComments_lessons_fk" FOREIGN KEY ("entityId") REFERENCES public.lessons("id"),
	CONSTRAINT "lessonComments_comments_fk" FOREIGN KEY ("commentId") REFERENCES public.comments("id")
);
CREATE UNIQUE INDEX lessonComments_lessonId_idx ON public."lessonComments" ("entityId", "commentId");
 */
