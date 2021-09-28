package br.com.zup.edu.grpc

import br.com.zup.edu.ConsultarChaveServiceGrpc
import br.com.zup.edu.RegistrarChaveServiceGrpc
import br.com.zup.edu.RemoverChaveServiceGrpc
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import jakarta.inject.Singleton

@Factory
open class GrpcClientFactory(@GrpcChannel("chaves") val channel: ManagedChannel) {

    @Singleton
    open fun registraChave() = RegistrarChaveServiceGrpc.newBlockingStub(channel)

    @Singleton
    fun removerChave() = RemoverChaveServiceGrpc.newBlockingStub(channel)

    @Singleton
    fun consultarChave() = ConsultarChaveServiceGrpc.newBlockingStub(channel)
}