package online.javanese.kweryEntityMapping

import com.github.andrewoma.kwery.mapper.Table
import org.jetbrains.ktor.util.ValuesMap

class ValuesMapToKweryEntityMapper<out T : Any, ID>(
        private val table: Table<T, ID>
) : (ValuesMap) -> T {

    // TODO validation: return an instance of a sealed class ( success | error )

    private val valueFactory = ValueFactory(table)

    override fun invoke(map: ValuesMap): T =
            table.create(valueFactory.from(ValuesMapAsSource(map)))

}
