FROM openjdk:11

ENV MONGO_URI=mongodb://springboot-mongo:27017
ENV TEMP_DIR_ROOT=/temp
ENV LOCAL_STORAGE_ROOT=/LOCAL_STORAGE_ROOT

ARG jarfile
COPY $jarfile /server.jar

CMD java -jar server.jar