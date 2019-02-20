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

verbose = args.length > 0
client = new TimelapseBasicClient('https://timelapse-server.herokuapp.com/')
DELAY = 1000
STATE_DELAY = 2 * DELAY
running = new AtomicBoolean(true)
sleeping = new AtomicBoolean(false) //en veille
state = new CameraState(false, null, sleeping.get(), running.get())
Executor executor = Executors.newFixedThreadPool(2)
executor.submit({ -> processExecutions() })
executor.submit({ -> updateState() })

void processExecutions() {
    println('Building camera object...')
    Camera camera = buildCamera()
    println('Camera built. Waiting 5s for camera to stabilize...')
    Thread.sleep(5000) //wait 5s
    println('Camera ready. Starting processing executions')
    state.cameraWorking = true

    long lastPictureTime = Integer.MIN_VALUE
    while (running.get()) {
        if (sleeping.get()) {
            Thread.sleep(10 * DELAY)
            continue
        }
        TimelapseResponse<Execution> executionResponse = client.getCurrentExecution()
        if (handleError(executionResponse, 'Got error while getting soonest execution')) {
            Thread.sleep(DELAY)
            continue
        }
        Execution execution = executionResponse.data
        if (!execution) {
            Thread.sleep(DELAY)
            continue
        }
        long time0 = System.currentTimeMillis()
        long delaySinceLastPicture = time0 - lastPictureTime
        if (delaySinceLastPicture >= execution.getFrequency() * 1000) { //frequency is in ms
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
            if (picture) {
                TimelapseResponse<FileData> response = client.putImage(picture, execution.id)
                lastPictureTime = time0
                handleError(response, 'Error while sending picture')
            }
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

void updateState() {
    CameraState state = new CameraState()
    while (running.get()) {
        TimelapseResponse<Command> commandResponse = client.consumeCommand()
        if (commandResponse.isError()) {
            println('Got error while getting command')
            ErrorResponse error = commandResponse.getError()
            println("$error.title: $error.message")
            Thread.sleep(STATE_DELAY)
            continue
        }
        Command command = commandResponse.data
        if (command != null) {
            switch (command) {
                case Command.SLEEP:
                case Command.WAKE_UP:
                    boolean sleep = command == Command.SLEEP
                    println(sleep ? 'Starting sleeping' : 'Waking up')
                    sleeping.set(sleep)
                    state.sleeping = sleep
                    break
                case Command.TURN_OFF:
                    println('Received turn off command')
                    running.set(false)
                    state.turnedOn = false
                    break
            }
        }
        client.putCameraState(state)
        Thread.sleep(STATE_DELAY)
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

@Override
void println(def value) {
    if (verbose) {
        super.println(value)
    }
}