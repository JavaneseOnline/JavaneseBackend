package online.javanese.link

import kotlinx.html.FlowContent


class AppliedLinkSet1<in T>(
        private val links: LinkSet1<T>,
        private val t: T
) : (FlowContent, @ParameterName("classes") String) -> Unit {

    override fun invoke(p1: FlowContent, classes: String) {
        links(p1, classes, t)
    }

}
