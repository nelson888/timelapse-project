package com.app4.project.timelapse.camera

import com.app4.project.timelapse.api.client.TimelapseBasicClient
import com.app4.project.timelapse.api.client.TimelapseResponse
import com.app4.project.timelapse.model.CameraState
import com.app4.project.timelapse.model.Command
import com.app4.project.timelapse.model.ErrorResponse
import com.app4.project.timelapse.model.Execution
import com.app4.project.timelapse.model.FileData
import com.app4.project.timelapse.model.User
import uk.co.caprica.picam.ByteArrayPictureCaptureHandler
import uk.co.caprica.picam.Camera
import uk.co.caprica.picam.CameraConfiguration
import uk.co.caprica.picam.enums.Encoding

import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

final User USER = new User("timelapse", "mlijmbstrhlz")
verbose = args.length > 0
client = new TimelapseBasicClient('https://timelapse-server.herokuapp.com/')
while (!client.authenticated) {
    println('Authenticating to the server')
    TimelapseResponse response = client.authenticate(USER)
    if (response.successful && response.data) {
        println('Authenticated successfully')
    } else {
        println('Authentication failed, trying again in 5s')
        Thread.sleep(5000)
    }
}
DELAY = 1000
STATE_DELAY = 10000
running = new AtomicBoolean(true)
sleeping = new AtomicBoolean(false) //en veille
state = new CameraState(false, null, sleeping.get(), running.get(), 0)
Executor executor = Executors.newFixedThreadPool(2)
executor.submit({ -> processExecutions() })
executor.submit({ -> updateState() })
executor.shutdown()
println('Exiting program')

void processExecutions() {
    println('Building camera object...')
    Camera camera = null
    while (camera == null) {
        try {
            camera = buildCamera()
        } catch (RuntimeException | IOException e) {
            println("Failed to instanciate camera object: $e.message")
            println('Retrying in 10 seconds')
            Thread.sleep(10000)
        }
    }
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
        TimelapseResponse<Execution> executionResponse = client.currentExecution
        if (handleError(executionResponse, 'Got error while getting soonest execution')) {
            Thread.sleep(DELAY)
            continue
        }
        Execution execution = executionResponse.data
        if (!execution) {
            println('No execution running, waiting...')
            Thread.sleep(DELAY)
            continue
        }
        long time0 = System.currentTimeMillis()
        long delaySinceLastPicture = time0 - lastPictureTime
        if (delaySinceLastPicture >= execution.period * 1000) { //period is in s
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
                if (handleError(response, 'Error while sending picture')) {
                    Thread.sleep(DELAY)
                    continue
                } else {
                    println('Picture took')
                }
                lastPictureTime = time0
            }
        }
        long processTime = System.currentTimeMillis() - lastPictureTime
        if (processTime < execution.period * 1000) {
            long waitDelay = execution.period * 1000 - processTime
            println("Waiting ${String.format("%.2f", waitDelay.toFloat() / 1000f)}s for next picture (period=$execution.period)")
            Thread.sleep(waitDelay)
        }

    }
    camera.close()
}
boolean handleError(TimelapseResponse response, String errorMessage) {
    if (response.isError()) {
        println(errorMessage)
        ErrorResponse error = response.getError()
        println("$error.title: $error.message")
        return true
    }
    return false
}

void updateState() {
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
            println("Received command $command")
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
        handleError(client.putCameraState(state), 'Failed updated Camera State')
        println('Updated camera state')
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
            .rotation(180)
    return new Camera(config)
}

@Override
void println(def value) {
    if (verbose) {
        super.println(value)
    }
}