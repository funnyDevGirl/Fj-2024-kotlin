package io.demo.service

import io.demo.dto.Place
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive


@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Place::class)
object PlaceSerializer : KSerializer<Place> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Place", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Place {

        return when (val jsonElement = (decoder as JsonDecoder).decodeJsonElement()) {
            is JsonObject -> Place(
                title = jsonElement["title"]?.jsonPrimitive?.content ?: "Неизвестно",
                address = jsonElement["address"]?.jsonPrimitive?.content ?: "Неизвестно"
            )
            is JsonNull -> Place(title = "Неизвестно", address = "Неизвестно")
            else -> Place(title = "Неизвестно", address = "Неизвестно")
        }
    }
}
