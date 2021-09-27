package br.com.zup.edu.config.error

import io.grpc.Status.Code.*
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.hateoas.JsonError
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class ErrorHandlerInterceptor: ExceptionHandler<StatusRuntimeException, HttpResponse<Any>> {

    val logger = LoggerFactory.getLogger(this::class.java)

    override fun handle(request: HttpRequest<*>, exception: StatusRuntimeException): HttpResponse<Any> {
        val statusCode = exception.status.code
        val description = exception.status.description

        val (httpStatus, message) = when(statusCode) {
            ALREADY_EXISTS -> Pair(HttpStatus.UNPROCESSABLE_ENTITY, description)
            NOT_FOUND -> Pair(HttpStatus.NOT_FOUND, description)
            INVALID_ARGUMENT -> Pair(HttpStatus.BAD_REQUEST, description)
            UNAVAILABLE -> Pair(HttpStatus.UNPROCESSABLE_ENTITY, description)
            else -> {
                logger.error(exception.message)
                Pair(HttpStatus.INTERNAL_SERVER_ERROR, exception.message)
            }
        }

        return HttpResponse.status<JsonError>(httpStatus).body(JsonError(message))
    }
}