#ifndef TIMELAPSE_RASPBERRY_API_CLIENT_H
#define TIMELAPSE_RASPBERRY_API_CLIENT_H

#include <iostream>
#include <string>

#include "data.h"
class ApiClient
{
    std::string baseUrl;
public:

    ApiClient(std::string);

    GlobalState getGlobalState();
    CameraState getState();
    void print() const;
};

#endif //TIMELAPSE_RASPBERRY_API_CLIENT_H
