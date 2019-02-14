#include <iostream>
#include <string>
//time dependencies
#include <time.h>
#include <unistd.h>
#include "data.h"
/*
#include <cpprest/http_client.h>
#include <cpprest/filestream.h>
*/

using namespace std;

/** Partie a changer plus tard **/
Execution* e = new Execution();

Execution* getCurrentExecution() {
    e->startTime = 3000;
    e->endTime = 15000;
    e->frequency = 2000;
    e->title = "Test";

    return  e;
}

void sleepMs(long ms) {
    usleep(ms * 1000L); // it's in nanosec
}

bool isRunning(Execution* e) { //has started and is not finished
    return true;
}

const long WAIT_TIME_MILLIS = 1000;


void takePicture() {
    //TODO Maryam
}

void sendPicture() {
    //TODO Nelson
}

int main() {

    //TODO Maryam: INITIALISER LA CAMERA
    cout << "Starting Camera...\n";

    time_t time0;
    time_t time1;

    time_t lastPictureTime;
    time(&lastPictureTime);   // get current time.

    bool running = true;
    cout << "Camera ready\nStarting processing\n";
    while (running) { //TODO Nelson: check for commands to run
        Execution* e = getCurrentExecution();
        if (!isRunning(e)) {
            cout << "No execution running, waiting...\n";
            sleepMs(WAIT_TIME_MILLIS);
            continue;
        }

        time(&time0);   // get current time.
        double timeSinceLastPicture = time0 - lastPictureTime; //ATTENTION!!! EN SECONDES seconds
        if (timeSinceLastPicture * 1000 >= e->frequency) {
            cout << "Taking picture...\n";
            takePicture();
            time(&lastPictureTime);   // get current time.
            cout << "Picture took\nSending picture to server\n";
            sendPicture();
        } else {
            cout << "Waiting for next picture to be taken\n";
        }

        time(&time1);   // get current time.
        long processTime = 1000 * ((long) (double) time1 - time0);
        if (processTime < WAIT_TIME_MILLIS) {
            sleepMs(WAIT_TIME_MILLIS - processTime);
        }
    }

    //TODO Maryam: FERMER LA CAMERA

    return 0;
}
/*
void test() {
    usleep(1000000); // will sleep for 1 s

    time(&time1);   // get current time after time pass.

    std::cout << "The time1 is: " << time1 <<'\n';
    std::cout << "The time0 is: " << time0 <<'\n';

    double seconds = time1 - time0;

    std::cout << "seconds since start: " << seconds <<'\n';
}
 */