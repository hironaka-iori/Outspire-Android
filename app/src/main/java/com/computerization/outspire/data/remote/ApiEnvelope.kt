package com.computerization.outspire.data.remote

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class ApiEnvelope<T>(
    @Serializable(with = ResultTypeSerializer::class)
    val ResultType: Int = 0,
    val Message: String? = null,
    val Data: T? = null,
) {
    val isSuccess: Boolean get() = ResultType == 0
}

object ResultTypeSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ResultType", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Int {
        val jd = decoder as? JsonDecoder ?: return Int.serializer().deserialize(decoder)
        val prim: JsonPrimitive = jd.decodeJsonElement().jsonPrimitive
        return prim.intOrNull ?: prim.content.toIntOrNull() ?: -1
    }

    override fun serialize(encoder: Encoder, value: Int) {
        encoder.encodeInt(value)
    }
}
