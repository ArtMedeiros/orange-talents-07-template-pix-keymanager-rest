package br.com.zup.edu.chaves.dto

import br.com.zup.edu.ChaveRequest
import br.com.zup.edu.ConsultaRequest
import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.config.validation.ChaveValida
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@ChaveValida
data class ChavesRestRequest(
    @field:NotNull
    val tipo: TipoChaveLocal,
    @field:Size(max = 77)
    val chave: String?,
    @field:NotNull
    val tipoConta: TipoContaLocal
) {

    companion object {
        fun toBuscarChaveGrpc(id: Long, cliente: String): ConsultaRequest{
            val pixId = ConsultaRequest.ConsultaInterna.newBuilder()
                .setPixId(id)
                .setClienteId(cliente)
                .build()

            return ConsultaRequest.newBuilder()
                .setPixId(pixId)
                .build()
        }
    }

    fun toRegistraChaveGrpc(cliente: String): ChaveRequest {
        return ChaveRequest.newBuilder()
            .setCliente(cliente)
            .setTipoChave(TipoChave.valueOf(tipo.name))
            .setChave(chave ?: "")
            .setConta(TipoConta.valueOf(tipoConta.name))
            .build()
    }
}