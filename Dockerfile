FROM java:8

#Install maven
RUN apt-get update
RUN apt-get install -y maven

WORKDIR /build

# Dependencies
ADD pom.xml /build/pom.xml
RUN ["mvn", "dependency:resolve"]
RUN ["mvn", "verify"]

# Compile and package jar
ADD src /build/src
RUN ["mvn", "package"]

#Setup Config
ENV MSG_NODE_CONFIG dev
ADD node.config.json /build/node.config.json
ADD server-keystore.jks /build/server-keystore.jks

#Run msg-node
EXPOSE 9090
CMD ["mvn", "exec:java", "-Dexec.args='9090'"]
