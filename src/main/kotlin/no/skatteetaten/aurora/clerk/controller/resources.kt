package no.skatteetaten.aurora.clerk.controller

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JsonNode

data class ClerkFailure(
    @JsonIgnore val error: Throwable? = null
) {
    val errorMessage: String = error?.let { it.message ?: "Unknown error (${it::class.simpleName})" } ?: ""
}

data class ClerkResponse<T>(
    val items: List<T> = emptyList(),
    val success: Boolean = true,
    val message: String = "OK",
    val count: Int = items.size
) {

    companion object {
        fun fromFailure(failure: ClerkFailure) =
            ClerkResponse<ClerkFailure>(
                success = false,
                message = failure.errorMessage
            )
    }
}

data class PodItem(
    val name: String,
    val applicationName: String?,
    val startTime: String,
    val status: String
)

data class ScaleCommand(
    val name: String,
    val replicas: Int
)

data class ScalePayload(
    val apps: List<ScaleCommand>
)

data class ScaleResult(
    val command: ScaleCommand,
    val result: JsonNode?,
    val pods: List<PodItem>
)
