package example.server.modules.record.domain.model

import java.time.OffsetDateTime

data class Record(
    val externalReference: String,
    val category: String,
    val displayName: String,
    val id: Long? = null,
    val active: Boolean = true,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
) {
    init {
        require(externalReference.isNotBlank() && externalReference.length <= 50)
        require(category.isNotBlank() && category.length <= 50)
        require(displayName.isNotBlank() && displayName.length <= 100)
    }
}
