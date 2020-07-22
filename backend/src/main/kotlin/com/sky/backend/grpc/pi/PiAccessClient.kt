package com.sky.backend.grpc.pi

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.*
import java.io.Closeable
import java.util.concurrent.TimeUnit

class PiAccessClient(private val channel: ManagedChannel) : Closeable {
    private val stub: PiAccessGrpcKt.PiAccessCoroutineStub = PiAccessGrpcKt.PiAccessCoroutineStub(channel)

    suspend fun getInfo(deviceId: String) = coroutineScope {
        val request = GeneralRequest
            .newBuilder()
            .setDeviceID(deviceId)
            .build()

        val boardInfo: BoardInfoResponse = stub.getBoardInfo(request)
        println("board info $boardInfo")
    }

    suspend fun setPinState(state: Boolean, pinNo: Int) = coroutineScope {
        val builder = SwitchState.newBuilder()
        builder.isOn = state
        builder.pinNo = pinNo

        val response = stub.setSwitchState(builder.build())
        println("pin response $response")
    }

    suspend fun setPwm(pinNo: Int, dutyCycle: Float, frequency: Int) = coroutineScope {
        val builder = PwmRequest.newBuilder()
        builder.dutyCycle = dutyCycle
        builder.frequency = frequency
        builder.pin = pinNo
        val response = stub.setPwm(builder.build())
        println("pwm response $response")
    }


    suspend fun shutdownServer() {
        val request = GeneralRequest
            .newBuilder()
            .setDeviceID("shut down req")
            .build()
        val response = stub.shutdown(request)
        println("shut $response")
    }

    override fun close() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }

}

fun main() = runBlocking {
    val piAccessClient = PiAccessClient(
        ManagedChannelBuilder
            .forAddress("192.168.0.16", 50053)
            .usePlaintext()
            .executor(Dispatchers.Default.asExecutor())
            .build()
    )

    piAccessClient.getInfo("java client")

//    piAccessClient.setPinState(true, 19)
//    delay(2000)
//    piAccessClient.setPinState(false, 19)
//    delay(2000)
    piAccessClient.setPwm(19, 0.25f, 1000)
    delay(1000)
    piAccessClient.setPwm(19, 0.5f, 1000)
    delay(1000)
    piAccessClient.setPwm(19, 0.75f, 1000)
    delay(1000)
    piAccessClient.setPwm(19, 1f, 1000)
    delay(1000)
    piAccessClient.shutdownServer()
    piAccessClient.close()
}
