#ifndef TIMELAPSE_RASPBERRY_DATA_H
#define TIMELAPSE_RASPBERRY_DATA_H

#include <iostream>
#include <string>

class GlobalState {

};

class CameraState {

};

class Execution {
public:
    std::string title;
    long startTime;
    long endTime;
    int id;
    long frequency;


};

#endif //TIMELAPSE_RASPBERRY_DATA_H
