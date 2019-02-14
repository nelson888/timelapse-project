#include "apiclient.h"


ApiClient::ApiClient(std::string baseUrl) {
    this->baseUrl = std::move(baseUrl);
}

GlobalState ApiClient::getGlobalState() {

}


void sendImage(std::string fileName) {
    CkHttpRequest req;

    //  The ContentType, HttpVerb, and Path properties should
    //  always be explicitly set.
    req.put_HttpVerb("POST");
    req.put_Path("/something");
    req.put_ContentType("multipart/form-data");

    req.AddBytesForUpload("image.jpg", "image", TODOOOOO);

    //  View the request that would be sent if SynchronousRequest was called:
    const char *requestMime = req.generateRequestText();
    std::cout << requestMime << "\r\n";

}


auto getRequest()