package online.javanese.link

import kotlinx.html.FlowContent
import kotlinx.html.nav


class LinkSet1<in T>(
        private val link: Link<T>
) : (FlowContent, @ParameterName("classes") String, T) -> Unit {

    override fun invoke(p1: FlowContent, classes: String, t: T) = with(p1) {
        nav(classes = classes) {
            link.render(this, t)
        }
    }

}

class LinkSet2<in T, in U>(
        private val link1: Link<T>,
        private val link2: Link<U>,
        private val betweenLinks: FlowContent.() -> Unit
) : (FlowContent, T, U) -> Unit {

    override fun invoke(p1: FlowContent, p2: T, p3: U) = with(p1) {
        nav {
            link1.render(this, p2)
            betweenLinks()
            link2.render(this, p3)
        }
    }

}
