package br.com.zup.edu.chaves

import br.com.zup.edu.RegistrarChaveServiceGrpc
import br.com.zup.edu.chaves.dto.ChavesRestRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import org.slf4j.LoggerFactory
import javax.validation.Valid

@Controller("/clientes/{clienteId}")
class ChaveController(
    val grpcRegistrarChaveClient: RegistrarChaveServiceGrpc.RegistrarChaveServiceBlockingStub,
//    val grpcConsultarChaveCliente: ConsultarChaveServiceGrpc.ConsultarChaveServiceBlockingStub
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

//    @Get("/chaves/{id}")
//    fun buscarChave(@PathVariable clienteId: String, @PathVariable id: Long): HttpResponse<Any> {
//        val requestGrpc = ChaveRequest.toBuscarChaveGrpc(id, clienteId)
//
//        val response = grpcConsultarChaveCliente.consultarChave(requestGrpc)
//
//        return HttpResponse.ok(ChaveResponse.fromConsultaResponse(response))
//    }

    private fun location(clienteId: String, pixId: Long) = HttpResponse.uri("/clientes/$clienteId/chaves/$pixId")
}