package br.com.zup.edu.chaves.dto

import br.com.zup.edu.ConsultaResponse
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime
import java.time.ZoneOffset

@Introspected
data class DetalhesChaveResponse(
    val tipo: String,
    val chave: String,
    val conta: ContaResponse,
    @JsonFormat(shape = STRING)
    val criadaEm: LocalDateTime
) {

    companion object {
        fun fromConsultaResponse(response: ConsultaResponse): DetalhesChaveResponse {
            val criacao = LocalDateTime.ofEpochSecond(
                response.chave.criadaEm.seconds,
                response.chave.criadaEm.nanos,
                ZoneOffset.UTC
            )

            return DetalhesChaveResponse(
                tipo = response.chave.tipo,
                chave = response.chave.chave,
                conta = ContaResponse(
                    tipoConta = response.chave.conta.tipo,
                    instituicao = response.chave.conta.instituicao,
                    nomeTitular = response.chave.conta.nomeTitular,
                    cpfTitular = response.chave.conta.cpfTitular,
                    agencia = response.chave.conta.agencia,
                    numeroConta = response.chave.conta.numeroConta
                ),
                criadaEm = criacao
            )
        }
    }
}