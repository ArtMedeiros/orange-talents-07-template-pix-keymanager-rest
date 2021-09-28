package br.com.zup.edu.chaves

import br.com.zup.edu.*
import br.com.zup.edu.chaves.dto.ChavesRestRequest
import br.com.zup.edu.chaves.dto.DetalhesChaveResponse
import br.com.zup.edu.chaves.dto.ListaChavesResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import org.slf4j.LoggerFactory
import javax.validation.Valid

@Controller("/clientes/{clienteId}")
class ChaveController(
    val grpcRegistrarChaveClient: RegistrarChaveServiceGrpc.RegistrarChaveServiceBlockingStub,
    val grpcRemoverChaveClient: RemoverChaveServiceGrpc.RemoverChaveServiceBlockingStub,
    val grpcConsultarChaveClient: ConsultarChaveServiceGrpc.ConsultarChaveServiceBlockingStub,
    val grpcListaChavesClient: ListarChaveServiceGrpc.ListarChaveServiceBlockingStub
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    @Post("/chaves")
    fun registrarChave(@PathVariable clienteId: String, @Body @Valid request: ChavesRestRequest): HttpResponse<Any> {
        val requestGrpc = request.toRegistraChaveGrpc(clienteId)

        logger.info("Registrando chave")
        val responseGrpc = grpcRegistrarChaveClient.gerarChave(requestGrpc)
        logger.info("Chave registrada")

        return HttpResponse.created(location(clienteId, responseGrpc.pixId))
    }

    @Delete("/chaves/{pixId}")
    fun removerChave(@PathVariable clienteId: String, @PathVariable pixId: Long): HttpResponse<Any> {
        val requestGrpc = ChavesRestRequest.toRemoverChaveGrpc(pixId, clienteId)

        logger.info("Removendo chave")
        grpcRemoverChaveClient.removerChave(requestGrpc)
        logger.info("Chave removida")

        return HttpResponse.ok()
    }

    @Get("/chaves/{id}")
    fun buscarChave(@PathVariable clienteId: String, @PathVariable id: Long): HttpResponse<DetalhesChaveResponse> {
        val requestGrpc = ChavesRestRequest.toBuscarChaveGrpc(clienteId, id)

        val response = grpcConsultarChaveClient.consultarChave(requestGrpc)

        return HttpResponse.ok(DetalhesChaveResponse.fromConsultaResponse(response))
    }

    @Get("/chaves")
    fun listaChaves(@PathVariable clienteId: String): HttpResponse<List<ListaChavesResponse>> {
        val requestGrpc = ListaRequest.newBuilder().setClienteId(clienteId).build()

        val response = grpcListaChavesClient.listarChave(requestGrpc)

        return HttpResponse.ok(ListaChavesResponse.fromListaResponse(response))
    }

    private fun location(clienteId: String, pixId: Long) = HttpResponse.uri("/clientes/$clienteId/chaves/$pixId")
}