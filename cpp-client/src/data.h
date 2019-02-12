#ifndef TIMELAPSE_RASPBERRY_DATA_H
#define TIMELAPSE_RASPBERRY_DATA_H

#include <iostream>
#include <string>

class GlobalState {

};

class CameraState {

};

class ApiClient
{
    std::string baseUrl;
public:

    ApiClient(std::string);

    GlobalState getGlobalState();
    CameraState getState();
    void print() const;
};

#endif //TIMELAPSE_RASPBERRY_DATA_H
