package br.com.zup.edu.chaves.dto

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class ContaResponse(
    @field:NotBlank
    val tipoConta: String,

    @field:NotBlank
    val instituicao: String,

    @field:NotBlank
    val nomeTitular: String,

    @field:NotBlank
    val cpfTitular: String,

    @field:NotBlank
    val agencia: String,

    @field:NotBlank
    val numeroConta: String
)
