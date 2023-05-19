FROM openjdk:8

COPY ./target/app.jar /root/


ENV PATH=/usr/local/openjdk-8/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
ENV    JAVA_HOME=/usr/local/openjdk-8
ENV    LANG=C.UTF-8
ENV   JAVA_VERSION=8u342

EXPOSE 8000-65535

ENV SERVER_PORT=""
ENV RPC_PORT=""
ENV NODE=""

ENTRYPOINT  java -jar -Dserver.port=$SERVER_PORT -DconfigurePath=$NODE -Dspring.profiles.active=beta ~/app.jar --fang.node.bindPort=$RPC_PORT
