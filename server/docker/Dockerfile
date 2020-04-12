FROM openjdk:11

ARG jarfile
ARG mongoUri

ENV MONGO_URI=$mongoUri
ENV TEMP_DIR_ROOT=/temp
ENV LOCAL_STORAGE_ROOT=/localStorage

COPY $jarfile /server.jar

EXPOSE 8080

CMD java -jar server.jar --executions.fake.data=true