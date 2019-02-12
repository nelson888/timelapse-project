#include <utility> //for std::move
#include <iostream>
#include <string>
#include <CkHttp.h>
#include <CkHttpRequest.h>
#include <CkHttpResponse.h>
#include "data.h"

ApiClient::ApiClient(std::string baseUrl) {
    this->baseUrl = std::move(baseUrl);
}

GlobalState ApiClient::getGlobalState() {

}