FROM java:8

#Install maven
RUN apt-get update
RUN apt-get install -y maven

WORKDIR /build

# Dependencies
ADD pom.xml /build/pom.xml
ADD package.json /build/package.json
RUN ["mvn", "dependency:resolve"]
RUN ["mvn", "verify"]
RUN ["npm", "install"]

# Compile and package jar
ADD src /build/src
RUN ["mvn", "package"]

#Setup Config
ENV MSG_NODE_CONFIG dev
ADD node.config.json /build/node.config.json
ADD server-keystore.jks /build/server-keystore.jks
ADD policy.json /build/policy.json

#Run msg-node
EXPOSE 9090
CMD ["mvn", "exec:java"]
