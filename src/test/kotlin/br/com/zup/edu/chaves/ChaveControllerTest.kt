package br.com.zup.edu.chaves

import br.com.zup.edu.*
import br.com.zup.edu.chaves.dto.*
import br.com.zup.edu.grpc.GrpcClientFactory
import com.fasterxml.jackson.annotation.JsonFormat
import com.google.protobuf.Timestamp
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.times
import org.mockito.Mockito
import java.time.LocalDateTime
import java.time.ZoneOffset

@MicronautTest(transactional = false)
internal class ChaveControllerTest(
    val grpcRegistrarClient: RegistrarChaveServiceGrpc.RegistrarChaveServiceBlockingStub,
    val grpcRemoverClient: RemoverChaveServiceGrpc.RemoverChaveServiceBlockingStub,
    val grpcConsultarChaveClient: ConsultarChaveServiceGrpc.ConsultarChaveServiceBlockingStub,
    val grpcListaChavesClient: ListarChaveServiceGrpc.ListarChaveServiceBlockingStub
) {

    @field:Inject
    @field:Client("/")
    lateinit var clientHttp: HttpClient

    val clienteId = "0d1bb194-3c52-4e67-8c35-a93c0af9284f"
    val uriBase = "/clientes/$clienteId/chaves"

    lateinit var timestamp: Timestamp
    lateinit var grpcResponse: ChaveResponse
    lateinit var grpcDetalhesReponse: ConsultaResponse

    @BeforeEach
    internal fun setUp() {
        grpcResponse = ChaveResponse.newBuilder().setPixId(1).build()

        val instant = LocalDateTime.now().toInstant(ZoneOffset.UTC)

        timestamp = Timestamp.newBuilder()
            .setSeconds(instant.epochSecond)
            .setNanos(instant.nano)
            .build()

        val contaResponse = ConsultaResponse.Chave.Conta.newBuilder()
            .setTipo("CONTA_POUPANCA")
            .setInstituicao("ITAÚ UNIBANCO S.A.")
            .setNomeTitular("Titular teste")
            .setCpfTitular("11111111111")
            .setAgencia("0001")
            .setNumeroConta("1234")
            .build()

        val chaveResponse = ConsultaResponse.Chave.newBuilder()
            .setTipo("EMAIL")
            .setChave("mail@mail.com")
            .setConta(contaResponse)
            .setCriadaEm(timestamp)
            .build()

        grpcDetalhesReponse = ConsultaResponse.newBuilder()
            .setClienteId(clienteId)
            .setPixId(1)
            .setChave(chaveResponse)
            .build()
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
    inner class RegistrarChaveTest {
        @Test
        internal fun `deve cadastrar uma chave aleatoria`() {
            val requestRandom = ChavesRestRequest(
                tipo = TipoChaveLocal.RANDOM,
                chave = "",
                tipoConta = TipoContaLocal.CONTA_CORRENTE
            )

            val grpcRequest = requestRandom.toRegistraChaveGrpc(clienteId)

            given(grpcRegistrarClient.gerarChave(grpcRequest)).willReturn(grpcResponse)

            val request = HttpRequest.POST(uriBase, requestRandom)
            val response = clientHttp.toBlocking().exchange(request, Any::class.java)

            assertEquals(HttpStatus.CREATED, response.status)
            assertTrue(response.headers.contains("Location"))
            assertTrue(response.header("Location")!!.matches("$uriBase/\\d".toRegex()))
        }
    }

    @Nested
    inner class RemoverChaveTest {
        @Test
        internal fun `deve remover uma chave existente`() {
            val grpcRequest = ChavesRestRequest.toRemoverChaveGrpc(1, clienteId)
            given(grpcRemoverClient.removerChave(grpcRequest)).willReturn(
                RemoverResponse.newBuilder().setStatus("OK").build()
            )

            val request = HttpRequest.DELETE<String>("$uriBase/1")
            val response = clientHttp.toBlocking().exchange(request, Any::class.java)

            assertEquals(HttpStatus.OK, response.status)
        }
    }

    @Nested
    inner class DetalhesChaveTest {

        @Test
        internal fun `deve retornar os dados de uma chave valida`() {
            val grpcRequest = ConsultaRequest.newBuilder()
                .setPixId(
                    ConsultaRequest.ConsultaInterna.newBuilder()
                        .setPixId(1)
                        .setClienteId(clienteId)
                        .build()
                )
                .build()

            given(grpcConsultarChaveClient.consultarChave(grpcRequest)).willReturn(grpcDetalhesReponse)

            val request = HttpRequest.GET<String>("$uriBase/1")
            val response = clientHttp.toBlocking().exchange(request, DetalhesChaveResponse::class.java)

            assertEquals(HttpStatus.OK, response.status)
            assertNotNull(response.body())
            assertTrue(response.body().tipo == "EMAIL")
        }
    }

    @Nested
    inner class ListarChavesTest {
        @Test
        internal fun `deve listar as chaves do cliente`() {
            val grpcRequest = ListaRequest.newBuilder()
                .setClienteId(clienteId)
                .build()

            val chave = ListaResponse.Chave.newBuilder()
                .setPixId(1)
                .setClienteId(clienteId)
                .setTipoChave("EMAIL")
                .setTipoConta("CONTA_POUPANCA")
                .setChave("mail@mail.com")
                .setCriadaEm(timestamp)
                .build()

            val grpcResponse = ListaResponse.newBuilder()
                .addAllChaves(listOf(chave))
                .build()

            given(grpcListaChavesClient.listarChave(grpcRequest)).willReturn(grpcResponse)

            val request = HttpRequest.GET<String>(uriBase)
            val response = clientHttp.toBlocking().exchange(request, Any::class.java)

            assertEquals(HttpStatus.OK, response.status)
            assertNotNull(response.body())
            assertTrue((response.body() as List<*>).isNotEmpty())
        }
    }

    @Factory
    @Replaces(factory = GrpcClientFactory::class)
    internal class GrpcClientMock {
        @Singleton
        fun registrarChaveMock() = Mockito.mock(RegistrarChaveServiceGrpc.RegistrarChaveServiceBlockingStub::class.java)

        @Singleton
        fun removerChaveMock() = Mockito.mock(RemoverChaveServiceGrpc.RemoverChaveServiceBlockingStub::class.java)

        @Singleton
        fun consultarChaveMock() = Mockito.mock(ConsultarChaveServiceGrpc.ConsultarChaveServiceBlockingStub::class.java)

        @Singleton
        fun listarChavesMock() = Mockito.mock(ListarChaveServiceGrpc.ListarChaveServiceBlockingStub::class.java)
    }
}