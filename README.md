# Time-lapse project
This was a group project. I was the project manager (and one of the developers). The goal was to create an autonomous time-lapse camera from scratch with a Raspberry and Raspberry-pi camera and to be able to see the photos took on an Android device. 
## Compenents
The software part of this project contains 3 components
### Android app
The app where we can program photo sessions in a given time-frame with a given period (for the time-lapse to take photos). We can see the photo took by the camera, also in a form of a video.
### Raspberry
The camera, responsible of taken photos (if there is a photo session running)

### Server
An intermediate server between the Android app and the Raspberry, where all the images and other datas would be stored. 

![alt text](https://github.com/nelson888/timelapse-project/tree/master/images/schema.png "Timelapse example")

## My part
I was in charge of developing the program run by the Raspberry, the API (server) and the API client that would be used by the android app and the Raspberry.

### API (server)
The server was developed in Spring Boot. It is secured by JWT Authentication. The images sent by the camera are stored in Firebase Storage, although, there is also a version where you can store files locally.

### API Client
The API client uses the HTTP client library I developed.

### Raspberry program
The program was developped in Groovy, it uses a Java library to manipulate the Raspberry Camera.


![alt text](https://github.com/nelson888/timelapse-project/tree/master/images/timelapse.png "Timelapse example")

