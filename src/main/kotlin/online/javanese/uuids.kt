package online.javanese

import com.github.andrewoma.kwery.mapper.SimpleConverter

typealias Uuid = java.util.UUID

object UuidConverter : SimpleConverter<Uuid>(
        { row, name -> row.obj(name) as Uuid }
)

val DefaultUuid = Uuid(0L, 0L)
