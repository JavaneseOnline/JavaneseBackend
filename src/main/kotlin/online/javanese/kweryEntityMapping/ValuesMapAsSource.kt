package online.javanese.kweryEntityMapping

import org.jetbrains.ktor.util.ValuesMap

class ValuesMapAsSource(
        private val valuesMap: ValuesMap
): ValueFactory.Source {
    override fun contains(key: String): Boolean = valuesMap.contains(key)
    override fun get(key: String): String = valuesMap[key]!!
}
