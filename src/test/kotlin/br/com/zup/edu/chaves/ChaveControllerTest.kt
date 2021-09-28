package br.com.zup.edu.chaves

import br.com.zup.edu.ChaveResponse
import br.com.zup.edu.RegistrarChaveServiceGrpc
import br.com.zup.edu.chaves.dto.ChavesRestRequest
import br.com.zup.edu.chaves.dto.TipoChaveLocal
import br.com.zup.edu.chaves.dto.TipoContaLocal
import br.com.zup.edu.config.error.ErrorHandlerInterceptor
import br.com.zup.edu.grpc.GrpcClientFactory
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.hateoas.JsonError
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito

@MicronautTest(transactional = false)
internal class ChaveControllerTest(
    val grpcClient: RegistrarChaveServiceGrpc.RegistrarChaveServiceBlockingStub
) {

    @field:Inject
    @field:Client("/")
    lateinit var clientHttp: HttpClient

    val clienteId = "0d1bb194-3c52-4e67-8c35-a93c0af9284f"
    val uriBase = "/clientes/$clienteId/chaves"

    lateinit var grpcResponse: ChaveResponse

    @BeforeEach
    internal fun setUp() {
        grpcResponse = ChaveResponse.newBuilder().setPixId(1).build()
    }

    @Nested
    inner class EnumTests {

        @Test
        internal fun `deve validar chave cpf valida`() {
            val tipoChave = TipoChaveLocal.CPF

            assertTrue(tipoChave.valida("00000000000"))
        }

        @Test
        internal fun `deve validar chave email valida`() {
            val tipoChave = TipoChaveLocal.EMAIL

            assertTrue(tipoChave.valida("mail@mail.com"))
        }

        @Test
        internal fun `deve validar chave telefone valida`() {
            val tipoChave = TipoChaveLocal.TELEFONE

            assertTrue(tipoChave.valida("+5599999999999"))
        }

        @Test
        fun `deve validar chave aleatoria vazia ou nulo`() {
            val tipoChave = TipoChaveLocal.RANDOM

            assertTrue(tipoChave.valida(null))
            assertTrue(tipoChave.valida(""))
        }

        @Test
        internal fun `nao deve validar chave aleatoria preenchida`() {
            val tipoChave = TipoChaveLocal.RANDOM

            assertFalse(tipoChave.valida("ABC"))
        }

        @Test
        internal fun `nao deve validar chaves nulas que nao sejam aleatorias`() {
            val cpf = TipoChaveLocal.CPF
            val email = TipoChaveLocal.EMAIL
            val telefone = TipoChaveLocal.TELEFONE

            assertFalse(cpf.valida(""))
            assertFalse(email.valida(""))
            assertFalse(telefone.valida(""))
        }
    }

    @Nested
    inner class HandlerTests {
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

    @Test
    internal fun `deve cadastrar uma chave aleatoria`() {
        val requestRandom = ChavesRestRequest(
            tipo = TipoChaveLocal.RANDOM,
            chave = "",
            tipoConta = TipoContaLocal.CONTA_CORRENTE
        )

        val grpcRequest = requestRandom.toRegistraChaveGrpc(clienteId)

        given(grpcClient.gerarChave(grpcRequest)).willReturn(grpcResponse)

        val request = HttpRequest.POST(uriBase, requestRandom)
        val response = clientHttp.toBlocking().exchange(request, Any::class.java)

        assertEquals(HttpStatus.CREATED, response.status)
        assertTrue(response.headers.contains("Location"))
        assertTrue(response.header("Location")!!.matches("$uriBase/\\d".toRegex()))
    }

    @Factory
    @Replaces(factory = GrpcClientFactory::class)
    internal class GrpcClientMock {
        @Singleton
        fun registrarChaveMock() = Mockito.mock(RegistrarChaveServiceGrpc.RegistrarChaveServiceBlockingStub::class.java)
    }
}