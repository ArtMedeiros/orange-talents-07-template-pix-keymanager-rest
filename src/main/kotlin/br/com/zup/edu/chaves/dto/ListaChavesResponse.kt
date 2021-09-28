package br.com.zup.edu.chaves.dto

import br.com.zup.edu.ConsultaResponse
import br.com.zup.edu.ListaResponse
import com.fasterxml.jackson.annotation.JsonFormat
import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime
import java.time.ZoneOffset

@Introspected
data class ListaChavesResponse(
    val pixId: Long,
    val clienteId: String,
    val tipoChave: TipoChaveLocal,
    val chave: String,
    val tipoConta: TipoContaLocal,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val criadaEm: LocalDateTime
) {

    companion object {
        fun fromListaResponse(response: ListaResponse): List<ListaChavesResponse> {
            val lista = response.chavesList.map { resp ->
                val instant = LocalDateTime.ofEpochSecond(
                    resp.criadaEm.seconds,
                    resp.criadaEm.nanos,
                    ZoneOffset.UTC
                )

                ListaChavesResponse(
                    pixId = resp.pixId,
                    clienteId = resp.clienteId,
                    tipoChave = TipoChaveLocal.valueOf(resp.tipoChave),
                    chave = resp.chave,
                    tipoConta = TipoContaLocal.valueOf(resp.tipoConta),
                    criadaEm = instant
                )
            }

            return lista
        }
    }
}