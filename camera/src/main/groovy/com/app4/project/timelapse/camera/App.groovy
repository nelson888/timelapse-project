package com.app4.project.timelapse.camera

import com.app4.project.timelapse.api.client.TimelapseBasicClient
import com.app4.project.timelapse.api.client.TimelapseResponse
import com.app4.project.timelapse.model.CameraState
import com.app4.project.timelapse.model.Command
import com.app4.project.timelapse.model.ErrorResponse
import com.app4.project.timelapse.model.Execution
import com.app4.project.timelapse.model.FileData
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
state = new CameraState(false, null, sleeping.get(), running.get())
Executor executor = Executors.newFixedThreadPool(4)
executor.submit({ -> processExecutions() })
//executor.submit({ -> checkState() })

void processExecutions() {
    println('Building camera object...')
    Camera camera = buildCamera()
    println('Camera built. Waiting for camera to take focus...')
    Thread.sleep(5000) //wait 5s
    println('Camera ready. Starting processing executions')
    state.cameraWorking = true

    long lastPictureTime = System.currentTimeMillis()
    while (running.get()) {
        if (sleeping.get()) {
            Thread.sleep(2 * DELAY)
            continue
        }
        TimelapseResponse<Execution> executionResponse = client.getSoonestExecution()
        if (handleError(executionResponse, 'Got error while getting soonest execution')) {
            Thread.sleep(DELAY)
            continue
        }
        Execution execution = executionResponse.data
        if (!execution || !execution.isRunning()) {
            println('No execution running, waiting')
            Thread.sleep(DELAY)
            continue
        }
        long time0 = System.currentTimeMillis()
        long delaySinceLastPicture = time0 - lastPictureTime
        if (delaySinceLastPicture >= execution.getFrequency()) {
            println("Taking picture...")
            byte[] picture
            try {
                picture = takePicture(camera)
            } catch (IOException e) {
                println('Error while taking picture:')
                println("$e.message")
                Thread.sleep(DELAY)
                continue
            }
            println("Picture took. Sending it")
            TimelapseResponse<FileData> response = client.putImage(picture, execution.id)
            handleError(response, 'Error while sending picture')
        }
        long processTime = System.currentTimeMillis() - time0
        if (processTime < DELAY) {
            Thread.sleep(DELAY - processTime)
        }

    }
}
boolean handleError(TimelapseResponse response, String errorMessage) {
    if (response.isError()) {
        println(errorMessage)
        ErrorResponse error = response.getError()
        println("$error.title: $error.message")
    }
    return false
}

void checkState() {
    CameraState state = new CameraState()
    while (true) {
        TimelapseResponse<Command> commandResponse = client.consumeCommand()
        if (commandResponse.isError()) {
            println('Got error while getting soonest execution')
            ErrorResponse error = commandResponse.getError()
            println("$error.title: $error.message")
            Thread.sleep(SLEEP_DELAY)
            continue
        }
        Command command = commandResponse.data
        if (command != null) {
            switch (command) {
                case Command.SLEEP:
                case Command.WAKE_UP:
                    boolean s = command == Command.WAKE_UP
                    sleeping.set(s)
                    state.sleeping = s
                    break
                case Command.TURN_OFF:
                    running.set(false)
                    state.turnedOn = false
                    break
            }
            client.putCameraState(state)
        }
        Thread.sleep(SLEEP_DELAY)
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