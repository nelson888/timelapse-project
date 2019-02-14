package com.app4.project.timelapse.camera

import com.app4.project.timelapse.api.client.TimelapseBasicClient
import com.app4.project.timelapse.api.client.TimelapseResponse
import com.app4.project.timelapse.model.Command
import com.app4.project.timelapse.model.ErrorResponse
import com.app4.project.timelapse.model.Execution

import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

FILE_NAME = "pic.jpg"
COMMAND = "sleep 1" //TODO a changer avant de mettre sur la raspberry
client = new TimelapseBasicClient('https://timelapse-server.herokuapp.com/')
DELAY = 1000
SLEEP_DELAY = 2 * DELAY
running = new AtomicBoolean(true)
sleeping = new AtomicBoolean(false) //en veille
Executor executor = Executors.newFixedThreadPool(3)
println("Starting app")
executor.submit({ -> processExecutions() })
//executor.submit({ -> checkState() })

void processExecutions() {
    long lastPictureTime = System.currentTimeMillis()
    while (running.get()) {
        if (sleeping.get()) {
            wait(2 * DELAY)
            continue
        }
        TimelapseResponse<Execution> executionResponse = client.getSoonestExecution()
        if (executionResponse.isError()) {
            println("Got error while getting soonest execution")
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
            COMMAND.execute().waitFor()
            sendPicture(client, FILE_NAME, execution.getId())
        }
        long processTime = System.currentTimeMillis() - time0
        if (processTime < DELAY) {
            wait(DELAY - processTime)
        }

    }
}

static void sendPicture(TimelapseBasicClient client, String fileName, int executionId) {
    File file = new File(fileName) //TODO filename is not a path
    client.putImage(file, executionId)
}

void checkState() {
    TimelapseBasicClient client
    while (true) {
        TimelapseResponse<Command> commandResponse = client.consumeCommand()
        if (commandResponse.isError()) {
            println("Got error while getting soonest execution")
            ErrorResponse error = commandResponse.getError()
            println("$error.title: $error.message")
            wait(SLEEP_DELAY)
            continue
        }
        Command command = commandResponse.data
        //TODO FAIRE JUSTE TYPE ENUMERER
    }
}