## dev-msg-node-vertx

### Setup Environment

#### JavaScript
On the first time you are cloning this repository, you need to run the command ```npm run init-setup```;

After running successfully this command you will have 2 folders (node_modules and vendor), these folders are excluded from the commit process, and are only for development.

If you already have the project configured on your machine, you only need run the command ```npm install``` to add new dependencies;

If you have some trouble with the environment, you can open an issue;

#### Java
Follow the link to [Install Maven](https://maven.apache.org/install.html).
* Build the project with: mvn package
* Run vertx node with: mvn exec:java -Dexec.args="\<domain\> \<port\>"

### Use of VertxProtoStub
Once the MessageNode is active, we are able to connect with the ProtoStub. The best example of how this is done is in the test/VertxProtoStub.js in "runtime connectivity" test. It's important to send the "runtimeURL" in the config parameter, because it will be used to link the connection channel to the runtime.

With this it's possible to send messages between runtimes, but Hyperty registration is something that should be done externally.

The connection is auto managed. It means, there is no need to call "connect()" explicitly, and it will always try to be in "connected" until "disconnect()" is called. Status messages are sent to "runtimeProtoStubURL/status".

### Component Integration
There are 2 types of components that can integrate in the Vertx Message Node implementation.
* Addressable, based in one destination address. Messages are deliver based on the "header.to" field of the message.
* Interceptors that can intercept and verify every message that enters the Message Node, whatever the destination address.

![](vertx_impl_arch.png)

#### Addressable Components
These are implementations of the interface ```IComponent extends Handler<PipeContext>```, and are added to the MessageNode with the method ```PipeRegistry.install(IComponent component)```. The only difference on the interface (between IComponent and Handler\<PipeContext\>) is an additional method to get the component address name, used for EventBus registration.

#### Interceptor Components
Implementations of ```Handler<PipeContext>```, and are added to the pipeline with ```Pipeline.addHandler(Handler<PipeContext> handler)```.

#### Use of PipeContext
Both types receive a ```PipeContext``` in the **handle** method when a message should be processed by the component. PipeContext gives access to the message with the ```getMessage()``` method, but also provides other useful methods like:
* ```next()``` method used in Interceptors that order the pipeline to execute the next interceptor. If no other interceptor exits, a delivery is proceed.
* ```deliver()``` used internally by the pipeline, but can be also used to ignore all other pipeline handlers and deliver the message directly to the component that has the address of "header.to".
* ```fail(String from, String error)``` interrupts the pipeline flow and sends an error message back to the original "header.from". The "header.from" of the reply is configured with the first parameter.
* ```reply(PipeMessage reply)``` does nothing to the pipeline flow and sends a reply back. Other similar and useful methods exists: ```replyOK(String from)``` and ```replyError(String from, String error)```
* ```disconnect()``` order the underlying resource channel to disconnect.

### Unit testing
**DO NOT SUBMIT CODE WITHOUT ALL UNIT TESTS ARE OK**
* Run 2 instances of the message-node: **mvn exec:java -Dexec.args="ua.pt 9090"** and **mvn exec:java -Dexec.args="ua.pt 9091"**
* Run **karma start**
