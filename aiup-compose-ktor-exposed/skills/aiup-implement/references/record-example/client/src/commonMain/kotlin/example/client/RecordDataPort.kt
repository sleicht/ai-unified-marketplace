package example.client

import example.shared.RecordListItem

fun interface RecordDataPort {
    suspend fun listRecords(query: String, limit: Int): List<RecordListItem>
}

fun interface AccessTokenProvider {
    suspend fun currentAccessToken(): String?
}
