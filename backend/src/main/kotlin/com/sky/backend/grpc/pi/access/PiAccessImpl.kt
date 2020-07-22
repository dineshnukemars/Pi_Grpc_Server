package com.sky.backend.grpc.pi.access

import com.diozero.api.DigitalOutputDevice
import com.diozero.api.GpioDevice
import com.diozero.api.PwmOutputDevice
import com.diozero.util.DeviceFactoryHelper
import com.sky.backend.grpc.pi.BoardInfoResponse
import com.sky.backend.grpc.pi.GeneralResponse
import java.util.*


class PiAccessImpl {

    private val pins: HashMap<Int, GpioDevice> = hashMapOf()

    fun getBoardInfo(): BoardInfoResponse {

        return with(DeviceFactoryHelper.getNativeDeviceFactory().boardInfo) {
            BoardInfo(
                adcVRef = adcVRef,
                libraryPath = libraryPath,
                make = make,
                memory = memory,
                model = model
            ).build()
        }
    }

    fun setSwitchState(pin: Int, isOn: Boolean): GeneralResponse {
        val device = pins[pin]
        if (device == null) {
            pins[pin] = DigitalOutputDevice(pin)
        } else if (device !is DigitalOutputDevice) {
            device.close()
            pins[pin] = DigitalOutputDevice(pin)
        }
        val outputDevice = pins[pin] as DigitalOutputDevice
        if (isOn) outputDevice.on() else outputDevice.off()

        return GeneralRes(true).build()
    }

    fun setPwm(pin: Int, dutyCycle: Float, frequency: Int): GeneralResponse {
        println("pwm request $pin  $dutyCycle $frequency")

        val device = pins[pin]
        if (device == null) {
            pins[pin] = PwmOutputDevice(pin, frequency, 0.5f)
        } else if (device !is PwmOutputDevice) {
            device.close()
            pins[pin] = PwmOutputDevice(pin, frequency, 0.5f)
        }
        (pins[pin] as PwmOutputDevice).value = dutyCycle

        return GeneralRes(true).build()
    }

    fun getDigitalInput(pin: Int): GeneralResponse {
        TODO("Not yet implemented")
    }


    fun listenPinState(): GeneralResponse {
        TODO("Not yet implemented")
    }

    fun shutdown() {
        DeviceFactoryHelper.getNativeDeviceFactory().close()
    }
}

interface ResponseBuilder<T> {
    fun build(): T
}

data class BoardInfo(
    val make: String = "make out",
    val model: String = "ultra model",
    val memory: Int = 1300,
    val libraryPath: String = "test/path",
    val adcVRef: Float = 50.5f
) {
    fun build(): BoardInfoResponse = BoardInfoResponse
        .newBuilder()
        .setMake(make)
        .setModel(model)
        .setMemory(memory)
        .setLibraryPath(libraryPath)
        .setAdcVRef(adcVRef)
        .build()
}

data class GeneralRes(val isCommandSuccess: Boolean) : ResponseBuilder<GeneralResponse> {

    override fun build(): GeneralResponse {
        val builder = GeneralResponse.newBuilder()
        builder.isCommandSuccess = isCommandSuccess
        return builder.build()
    }
}
