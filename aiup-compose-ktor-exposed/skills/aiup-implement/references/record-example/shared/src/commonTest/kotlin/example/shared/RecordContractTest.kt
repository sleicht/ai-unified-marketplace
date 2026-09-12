package example.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class RecordContractTest {
    @Test
    fun oldPayloadUsesDefaultAndRetainsWireNames() {
        val oldPayload =
            """{"id":1,"externalReference":"REC-1","displayName":"Ada","category":"Standard"}"""
        val item = Json.decodeFromString<RecordListItem>(oldPayload)
        assertEquals(true, item.active)
        val encoded = Json.parseToJsonElement(Json.encodeToString(item)).jsonObject
        assertEquals(setOf("id", "externalReference", "displayName", "category"), encoded.keys)
        assertEquals(item, Json.decodeFromString<RecordListItem>(Json.encodeToString(item)))
    }
}
