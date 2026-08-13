fun interface AccessTokenProvider {
    suspend fun currentAccessToken(): String?
}

class FixedAccessTokenProvider(private val token: String?) : AccessTokenProvider {
    override suspend fun currentAccessToken(): String? = token
}

class HttpClient

class ServiceApiClient(
    val baseUrl: String,
    val accessTokenProvider: AccessTokenProvider,
    val httpClient: HttpClient,
) {
    suspend fun authorizationHeader(): String? =
        accessTokenProvider.currentAccessToken()?.let { "Bearer $it" }
}

fun exampleApiClient(): ServiceApiClient =
    ServiceApiClient(
        baseUrl = "https://service.invalid",
        accessTokenProvider = FixedAccessTokenProvider("test-token"),
        httpClient = HttpClient(),
    )
