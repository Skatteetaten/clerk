package no.skatteetaten.aurora.clerk.controller

import com.fasterxml.jackson.annotation.JsonIgnore

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
