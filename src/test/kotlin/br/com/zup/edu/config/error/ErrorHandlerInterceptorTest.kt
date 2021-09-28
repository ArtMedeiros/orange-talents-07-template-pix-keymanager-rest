package br.com.zup.edu.config.error

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.hateoas.JsonError
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

@MicronautTest(transactional = false)
internal class ErrorHandlerInterceptorTest {
    val requestHttp = HttpRequest.GET<Any>("/")

    @Test
    internal fun `deve retornar not found quando resposta for not found`() {
        val mensagem = "Recurso não encontrado"
        val grpcException = StatusRuntimeException(Status.NOT_FOUND.withDescription(mensagem))

        val resposta = ErrorHandlerInterceptor().handle(requestHttp, grpcException)

        assertEquals(HttpStatus.NOT_FOUND, resposta.status)
        assertNotNull(resposta.body())
        assertEquals(mensagem, (resposta.body() as JsonError).message)
    }

    @Test
    internal fun `deve retornar unprocessable entity quando resposta for already exists`() {
        val mensagem = "Chave já cadastrada"
        val grpcException = StatusRuntimeException(Status.ALREADY_EXISTS.withDescription(mensagem))

        val resposta = ErrorHandlerInterceptor().handle(requestHttp, grpcException)

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, resposta.status)
        assertNotNull(resposta.body())
        assertEquals(mensagem, (resposta.body() as JsonError).message)
    }

    @Test
    internal fun `deve retornar bad request quando resposta for invalid argument`() {
        val mensagem = "Request inválida"
        val grpcException = StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription(mensagem))

        val resposta = ErrorHandlerInterceptor().handle(requestHttp, grpcException)

        assertEquals(HttpStatus.BAD_REQUEST, resposta.status)
        assertNotNull(resposta.body())
        assertEquals(mensagem, (resposta.body() as JsonError).message)
    }

    @Test
    internal fun `deve retornar unprocessable entity quando resposta for unavailable`() {
        val mensagem = "Recurso não disponível"
        val grpcException = StatusRuntimeException(Status.UNAVAILABLE.withDescription(mensagem))

        val resposta = ErrorHandlerInterceptor().handle(requestHttp, grpcException)

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, resposta.status)
        assertNotNull(resposta.body())
        assertEquals(mensagem, (resposta.body() as JsonError).message)
    }

    @Test
    internal fun `deve retornar server erro quando resposta nao for mapeada`() {
        val mensagem = "Algo deu errado, não foi possível concluir a operação"
        val grpcException = StatusRuntimeException(Status.UNKNOWN.withDescription(mensagem))

        val resposta = ErrorHandlerInterceptor().handle(requestHttp, grpcException)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resposta.status)
        assertNotNull(resposta.body())
        assertEquals("UNKNOWN: $mensagem", (resposta.body() as JsonError).message)
    }
}