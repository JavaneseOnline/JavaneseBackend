package online.javanese.handler

import online.javanese.locale.Language
import online.javanese.model.Page
import online.javanese.page.CardsPage
import online.javanese.page.Link


fun List<Page>.toCards(
        link2lessons: Link<Page>,
        link2tasks: Link<Page>,
        link2normalPage: Link<Page>,
        descs: Language.IndexCards
): List<CardsPage.Card<Page>> =
        flatMap {
            when (it.magic) {
                Page.Magic.Index -> emptyList()
                Page.Magic.Tree -> listOf(
                        CardsPage.Card(link2lessons, it, "icon ic_lessons_gray128", descs.lessons),
                        CardsPage.Card(link2tasks, it, "icon ic_tasks_gray128", descs.tasks)
                )
                Page.Magic.Articles -> listOf(
                        CardsPage.Card(link2normalPage, it, "icon ic_articles_gray128", descs.articles)
                )
                Page.Magic.CodeReview -> listOf(
                        CardsPage.Card(link2normalPage, it, "icon ic_review_gray128", descs.review)
                )
            }
        }
