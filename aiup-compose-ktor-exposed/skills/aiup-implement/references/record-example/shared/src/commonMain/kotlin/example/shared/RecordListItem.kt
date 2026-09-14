package example.shared

import kotlinx.serialization.Serializable

@Serializable
data class RecordListItem(
    val id: Long,
    val externalReference: String,
    val displayName: String,
    val category: String,
    val active: Boolean = true,
)
