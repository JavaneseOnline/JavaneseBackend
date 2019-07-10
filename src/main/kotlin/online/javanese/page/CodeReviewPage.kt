package online.javanese.page

import kotlinx.html.*
import online.javanese.link.Action
import online.javanese.social.UserSessions
import online.javanese.social.User
import online.javanese.link.HtmlBlock
import online.javanese.link.Link
import online.javanese.link.withFragment
import online.javanese.locale.Language
import online.javanese.model.CodeReview
import online.javanese.model.Meta
import online.javanese.model.Page


class CodeReviewPage(
        private val model: Page,
        private val reviews: List<CodeReview>,
        private val codeReviewLink: Link<CodeReview, *>,
        private val language: Language.CodeReviews,
        private val beforeContent: HtmlBlock,
        private val userSessions: UserSessions,
        private val currentUser: User?,
        private val redirectUri: String,
        private val submitCodeReview: Action<Unit, *>
) : Layout.Page {

    override val meta: Meta get() = model.meta

    override fun additionalHeadMarkup(head: HEAD) = with(head) {
        unsafe { +model.headMarkup }
    }

    override fun bodyMarkup(body: BODY) = with(body) {
        contentCardMain {

            beforeContent.render(this)

            div(classes = "no-pad mdl-tabs mdl-js-tabs mdl-js-ripple-effect content-padding-b") {

                menu(classes = "mdl-tabs__tab-bar mdl-color-text--grey-600") {
                    a(href = "#reviews", classes = "mdl-tabs__tab is-active") { +language.readTab }
                    a(href = "#submit", classes = "mdl-tabs__tab") { +language.submitTab }
                }

                nav(classes = "mdl-tabs__panel is-active") {
                    id = "reviews"

                    ul {
                        reviews.forEach { review ->
                            li {
                                codeReviewLink.render(this, review)
                            }
                        }
                    }
                }

                nav(classes = "mdl-tabs__panel") {
                    id = "submit"

                    h2 {
                        +model.heading
                    }

                    val requestLogin = currentUser === null
                    if (requestLogin) {
                        p {
                            with(userSessions) { authPrompt(language.authPrompt, redirectUri.withFragment("submit")) }
                        }
                    } else {
                        codeReviewForm()
                    }
                }
            }
        }
    }

    private fun NAV.codeReviewForm() {
        div {
            id = "vue-form"

            submitCodeReview.renderForm(this, Unit, classes = "ajax-form") {
                attributes["v-on:submit"] = "submit"
                attributes["data-complete-callback"] = "formSent"
                attributes["data-error-message"] = language.submissionError
                attributes["v-if"] = "state != 'SENT'"

                unsafe {
                    +model.bodyMarkup
                }

                materialInput(
                        inputId = "field_name", inputName = "name",
                        inputBlock = {
                            attributes["maxlength"] = "256"
                            value = currentUser?.displayName ?: ""
                        },
                        labelBlock = { +language.nameLabel },
                        strangePlace = { small { +language.nameExplanation } }
                )

                materialTextArea(
                        areaId = "field_text", areaName = "text", areaVModel = "text",
                        labelBlock = { +language.textLabel }
                )

                materialTextArea(
                        areaId = "field_code", areaName = "code", areaVModel = "code",
                        labelBlock = { +language.codeLabel },
                        strangePlace = { small { +language.codeExplanation } }
                )

                materialInput(
                        inputId = "field_email", inputName = "email",
                        inputBlock = {
                            attributes["maxlength"] = "256"
                        },
                        labelBlock = { +language.contactLabel },
                        strangePlace = { small { +language.contactExplanation } }
                )

                div {
                    materialCheckBox(
                            inputId = "list-checkbox-1",
                            inputVModel = "permission",
                            labelBlock = { +language.permissionLabel }
                    )

                    br()
                    p { +language.warning }
                }

                div {
                    materialButton(ButtonType.submit, "mdl-button--raised mdl-button--colored") {
                        name = "submit"
                        attributes["v-bind:disabled"] = "state === 'INVALID'"

                        +language.submit
                    }

                    // div(classes = "mdl-spinner mdl-js-spinner is-active") does not work within tabs
                    div(classes = "mdl-progress mdl-js-progress mdl-progress__indeterminate") {
                        attributes["v-show"] = "state === 'SENDING'"
                    }

                }

            }

            div(classes = "content-padding-v") {
                attributes["v-if"] = "state === 'SENT'"
                +language.submitted
            }

        }
    }

    override fun scripts(body: BODY) = with(body) {
        script { unsafe {
            val watch = '$' + "watch"
            +"""'use strict';
var FormState =
    Object.freeze({INVALID:"INVALID", VALID: "VALID", SENDING:"SENDING", SENT:"SENT"})
var vueForm = new Vue({
  el: '#vue-form',
  data: {
    state: FormState.INVALID,
    watches: null,
    text: '',
    code: '',
    permission: false
  },
  created: function() {
    this.subscribe(true);
  },
  methods: {
    somethingChanged: function(to, from) {
      this.state = this.text && this.code && this.permission
        ? FormState.VALID : FormState.INVALID;
    },
    submit: function() {
      this.subscribe(false);
      this.state = FormState.SENDING;
    },
    subscribe(flag) {
      if (flag) {
        this.watches = [
          this.$watch('text', this.somethingChanged),
          this.$watch('code', this.somethingChanged),
          this.$watch('permission', this.somethingChanged)
        ];
      } else {
        this.watches.forEach(function(unwatch) { unwatch(); });
        this.watches = null;
      }
    }
  }
});
function formSent(success) { // <form data-complete-callback="formSent"
  if (success) {
    vueForm.state = FormState.SENT;
  } else {
    vueForm.subscribe(true);
    vueForm.somethingChanged(); // update state depending on form values
  }
  return !success; // don't enable submit button if successful
}""" } }
        unsafe {
            +model.beforeBodyEndMarkup
        }
    }

}
