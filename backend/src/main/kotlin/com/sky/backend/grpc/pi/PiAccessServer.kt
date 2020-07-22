package com.sky.backend.grpc.pi

import com.sky.backend.grpc.pi.access.PiAccessImpl
import io.grpc.Server
import io.grpc.ServerBuilder
import kotlin.concurrent.thread

class PiAccessServer(private val port: Int) {

    private val piAccessService = PiAccessService()

    val server: Server =
        ServerBuilder
            .forPort(port)
            .addService(piAccessService)
            .build()

    fun start() {
        server.start()
        piAccessService.piServer = server
        println("server is started and listening to port $port")

        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** shutting down gRPC server since JVM is shutting down")
                server.shutdown()
                println("*** server shut down")
            }
        )
    }

    private class PiAccessService : PiAccessGrpcKt.PiAccessCoroutineImplBase() {
        var piServer: Server? = null

        private val piAccessImpl = PiAccessImpl()

        override suspend fun getBoardInfo(request: GeneralRequest): BoardInfoResponse {
            println("board info request")
            return piAccessImpl.getBoardInfo()
        }

        override suspend fun setSwitchState(request: SwitchState): GeneralResponse {
            println("switch request ${request.pinNo}  ${request.isOn}")
            return piAccessImpl.setSwitchState(request.pinNo, request.isOn)
        }

        override suspend fun setPwm(request: PwmRequest): GeneralResponse {
            println("pwm request ${request.pin}  ${request.dutyCycle} ${request.frequency}")
            return piAccessImpl.setPwm(request.pin, request.dutyCycle, request.frequency)
        }

        override suspend fun listenPinState(request: GeneralRequest): GeneralResponse {
            println("pin state request")
            return piAccessImpl.listenPinState()
        }

        override suspend fun getDigitalInput(request: GeneralRequest): GeneralResponse {
            println("get Digital Input request")
            return super.getDigitalInput(request)
        }

        override suspend fun shutdown(request: GeneralRequest): GeneralResponse {
            println("shutdown request")
            try {
                piAccessImpl.shutdown()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            thread {
                Thread.sleep(2000)
                piServer?.shutdown()
            }
            val builder = GeneralResponse.newBuilder()
            builder.isCommandSuccess = true
            return builder.build()
        }
    }

}

fun main() {
    val port = 50053
    val piAccessServer = PiAccessServer(port)
    piAccessServer.start()
    piAccessServer.server.awaitTermination()
}