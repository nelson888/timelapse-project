package com.app4.project.timelapse.camera

import com.app4.project.timelapse.api.client.TimelapseBasicClient
import com.app4.project.timelapse.api.client.TimelapseResponse
import com.app4.project.timelapse.model.Command
import com.app4.project.timelapse.model.ErrorResponse
import com.app4.project.timelapse.model.Execution

import uk.co.caprica.picam.ByteArrayPictureCaptureHandler
import uk.co.caprica.picam.Camera
import uk.co.caprica.picam.CameraConfiguration
import uk.co.caprica.picam.enums.Encoding

import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

client = new TimelapseBasicClient('https://timelapse-server.herokuapp.com/')
DELAY = 1000
SLEEP_DELAY = 2 * DELAY
running = new AtomicBoolean(true)
sleeping = new AtomicBoolean(false) //en veille
Executor executor = Executors.newFixedThreadPool(3)
executor.submit({ -> processExecutions() })
//executor.submit({ -> checkState() })

void processExecutions() {
    println('Building camera object...')
    Camera camera = buildCamera()
    println('Camera built. Waiting for camera to take focus...')
    wait(5000) //wait 5s
    println('Camera ready. Starting processing executions')

    long lastPictureTime = System.currentTimeMillis()
    while (running.get()) {
        if (sleeping.get()) {
            wait(2 * DELAY)
            continue
        }
        TimelapseResponse<Execution> executionResponse = client.getSoonestExecution()
        if (executionResponse.isError()) {
            println('Got error while getting soonest execution')
            ErrorResponse error = executionResponse.getError()
            println("$error.title: $error.message")
            wait(DELAY)
            continue
        }
        Execution execution = executionResponse.data
        if (!execution || !execution.isRunning()) {
            println('No execution running, waiting')
            wait(DELAY)
            continue
        }
        long time0 = System.currentTimeMillis()
        long delaySinceLastPicture = time0 - lastPictureTime
        if (delaySinceLastPicture >= execution.getFrequency()) {
            byte[] picture = takePicture(camera)
            client.putImage(picture, execution.id)
        }
        long processTime = System.currentTimeMillis() - time0
        if (processTime < DELAY) {
            wait(DELAY - processTime)
        }

    }
}

void checkState() {
    TimelapseBasicClient client
    while (true) {
        TimelapseResponse<Command> commandResponse = client.consumeCommand()
        if (commandResponse.isError()) {
            println('Got error while getting soonest execution')
            ErrorResponse error = commandResponse.getError()
            println("$error.title: $error.message")
            wait(SLEEP_DELAY)
            continue
        }
        Command command = commandResponse.data
        //TODO FAIRE JUSTE TYPE ENUMERER
    }
}

static byte[] takePicture(Camera camera) {
    ByteArrayPictureCaptureHandler bapch = new ByteArrayPictureCaptureHandler()
    camera.takePicture(bapch)
    return bapch.result().toByteArray()

}
static Camera buildCamera() {
    CameraConfiguration config = CameraConfiguration.cameraConfiguration()
            .width(1920)
            .height(1080)
            .encoding(Encoding.JPEG)
            .quality(85)
    return new Camera(config)
}