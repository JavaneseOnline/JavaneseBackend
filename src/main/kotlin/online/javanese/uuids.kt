package online.javanese

import com.github.andrewoma.kwery.mapper.SimpleConverter
import java.util.*

object UuidConverter : SimpleConverter<UUID>(
        { row, name -> row.obj(name) as UUID }
)

val DefaultUuid = UUID(0L, 0L)
