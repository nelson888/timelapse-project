#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64/
CURRENT_DIR=$(pwd)

cd ../
mvn install -DskipTests || exit
cd "$CURRENT_DIR" || exit
cp ../target/timelapseserver-0.0.1-SNAPSHOT.jar  ./timelapse-server.jar || exit
docker build . -t tambapps/timelapse-server --build-arg jarfile=./timelapse-server.jar --build-arg mongoUri=mongodb://springboot-mongo:27017
rm ./timelapse-server.jar