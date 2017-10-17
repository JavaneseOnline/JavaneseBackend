package online.javanese

import com.github.andrewoma.kwery.mapper.*
import org.jetbrains.ktor.util.ValuesMap
import java.math.BigDecimal
import java.util.*

class ValuesMapToKweryEntityMapper<out T : Any, ID>(
        private val table: Table<T, ID>
) : (ValuesMap) -> T {

    // TODO validation: return an instance of a sealed class ( success | error )

    private val valueFactory = ValueFactory(table)

    override fun invoke(map: ValuesMap): T =
            table.create(valueFactory.from(map))

}

private class ValueFactory<T : Any>(
        table: Table<T, *>
) {

    private val converters: Map<String, (String) -> Any> =
            table.allColumns.associateByTo(HashMap(table.allColumns.size), { it.name }, {
                val kweryConverter = it.converter
                if (kweryConverter is EnumByNameConverter<*>) {
                    val from = kweryConverter.from
                    val typeField = from.javaClass.getDeclaredField("\$type")
                    typeField.isAccessible = true
                    val type = typeField.get(from) as Class<*>
                    @Suppress("UPPER_BOUND_VIOLATED", "UNCHECKED_CAST")
                    enumAdapterFor<Enum<*>>(type)
                } else {
                    val type = kweryConverterToType[it.converter]!!
                    typeToConverter[type]!!
                }
            })

    private fun <T : Enum<T>> enumAdapterFor(type: Class<*>): (String) -> T {
        val enumType = type as Class<T>
        return { name: String ->
            java.lang.Enum.valueOf<T>(enumType, name)
        }
    }

    fun from(map: ValuesMap): Value<T> =
            object : Value<T> {
                override fun <R> of(column: Column<T, R>): R {
                    val name = column.name
                    return if (name in map) converters[name]!!(map[name]!!) as R
                    else column.defaultValue
                }
            }

    private companion object {
        private val kweryConverterToType: Map<Converter<*>, Class<*>>
        private val typeToConverter: Map<Class<*>, (String) -> Any>

        private val BooleanConverter: (String) -> Boolean = java.lang.Boolean::parseBoolean
        private val ByteConverter: (String) -> Byte = java.lang.Byte::parseByte
        private val ShortConverter: (String) -> Short = java.lang.Short::parseShort
        private val IntConverter: (String) -> Int = java.lang.Integer::parseInt
        private val LongConverter: (String) -> Long = java.lang.Long::parseLong
        private val FloatConverter: (String) -> Float = java.lang.Float::parseFloat
        private val DoubleConverter: (String) -> Double = java.lang.Double::parseDouble

        init {
            val kweryConverters =
                    standardConverters + (Uuid::class.java to UuidConverter)

            kweryConverterToType = kweryConverters
                    .map { (k, v) -> k to v }
                    .associateByTo(HashMap(kweryConverters.size), { it.second }, { it.first })

            typeToConverter = mapOf(
                    java.lang.Boolean::class.java to BooleanConverter,
                    java.lang.Boolean.TYPE to BooleanConverter,
                    java.lang.Byte::class.java to ByteConverter,
                    java.lang.Byte.TYPE to ByteConverter,
                    java.lang.Short::class.java to ShortConverter,
                    java.lang.Short.TYPE to ShortConverter,
                    java.lang.Integer::class.java to IntConverter,
                    java.lang.Integer.TYPE to IntConverter,
                    java.lang.Long::class.java to LongConverter,
                    java.lang.Long.TYPE to LongConverter,
                    java.lang.Float::class.java to FloatConverter,
                    java.lang.Float.TYPE to FloatConverter,
                    java.lang.Double::class.java to DoubleConverter,
                    java.lang.Double.TYPE to DoubleConverter,
                    BigDecimal::class.java to ::BigDecimal,
                    String::class.java to { s: String -> s },
                    // ByteArray, Timestamp, Time, Date ignored
                    Uuid::class.java to UUID::fromString
            )
        }
    }

}
