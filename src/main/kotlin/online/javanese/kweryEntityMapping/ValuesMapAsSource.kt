package online.javanese.kweryEntityMapping

import org.jetbrains.ktor.util.ValuesMap

class ValuesMapAsSource(
        private val valuesMap: ValuesMap,
        private val nameTransform: (String) -> String = { it }
): ValueFactory.Source {
    override fun contains(key: String): Boolean = valuesMap.contains(nameTransform(key))
    override fun get(key: String): String = valuesMap[nameTransform(key)]!!
}
