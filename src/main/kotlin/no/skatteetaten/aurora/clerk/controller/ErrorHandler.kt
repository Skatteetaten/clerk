package no.skatteetaten.aurora.clerk.controller

import mu.KotlinLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

private val logger = KotlinLogging.logger {}

@ControllerAdvice
class ErrorHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(RuntimeException::class)
    fun handleGenericError(e: RuntimeException, request: WebRequest): ResponseEntity<Any>? {
        return handleException(e, request, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleResourceUnauthorized(e: BadCredentialsException, request: WebRequest): ResponseEntity<Any>? {
        return handleException(e, request, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(NoSuchResourceException::class)
    fun handleResourceNotFound(e: NoSuchResourceException, request: WebRequest): ResponseEntity<Any>? {
        return handleException(e, request, HttpStatus.NOT_FOUND)
    }

    private fun handleException(e: Exception, request: WebRequest, httpStatus: HttpStatus): ResponseEntity<Any>? {
        val failure = ClerkFailure(e)
        val response = ClerkResponse.fromFailure(failure)
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        logger.info("Handle exception message=${e.message}")
        return handleExceptionInternal(e, response, headers, httpStatus, request)
    }
}

class NoSuchResourceException(message: String) : RuntimeException(message)
