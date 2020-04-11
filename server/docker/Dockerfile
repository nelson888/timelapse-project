FROM openjdk:11

ENV MONGO_URI=mongodb://localhost:27017
ENV TEMP_DIR_ROOT=/temp
ENV LOCAL_STORAGE_ROOT=/localStorage

ARG jarfile
COPY $jarfile /server.jar

EXPOSE 8080

CMD java -jar server.jar --executions.fake.data=true