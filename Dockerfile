FROM java:8

#Install maven
RUN apt-get update
RUN apt-get install -y maven

ENV NPM_CONFIG_LOGLEVEL info
ENV NODE_VERSION 6.3.1

RUN curl -SLO "https://nodejs.org/dist/v$NODE_VERSION/node-v$NODE_VERSION-linux-x64.tar.xz" \
    && curl -SLO "https://nodejs.org/dist/v$NODE_VERSION/SHASUMS256.txt.asc" \
    && gpg --batch --decrypt --output SHASUMS256.txt SHASUMS256.txt.asc \
    && grep " node-v$NODE_VERSION-linux-x64.tar.xz\$" SHASUMS256.txt | sha256sum -c - \
    && tar -xJf "node-v$NODE_VERSION-linux-x64.tar.xz" -C /usr/local --strip-components=1 \
    && rm "node-v$NODE_VERSION-linux-x64.tar.xz" SHASUMS256.txt.asc SHASUMS256.txt

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
