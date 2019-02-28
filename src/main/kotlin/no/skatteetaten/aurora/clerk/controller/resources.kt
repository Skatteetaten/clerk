package no.skatteetaten.aurora.clerk.controller

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.stereotype.Component
import uk.q3c.rest.hal.HalResource

data class AuroraFailure(
    @JsonIgnore val error: Throwable? = null
) {
    val errorMessage: String = error?.let { it.message ?: "Unknown error (${it::class.simpleName})" } ?: ""
}

data class AuroraResponse<T : HalResource?>(
    val items: List<T> = emptyList(),
    val failure: List<AuroraFailure> = emptyList(),
    val success: Boolean = true,
    val message: String = "OK",
    val failureCount: Int = failure.size,
    val successCount: Int = items.size,
    val count: Int = failureCount + successCount
) : HalResource() {

    companion object {
        fun fromFailure(failure: AuroraFailure) =
            AuroraResponse<HalResource>(
                success = false,
                message = failure.errorMessage,
                failure = listOf(failure)
            )
    }
}
