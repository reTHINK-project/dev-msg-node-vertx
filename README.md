## dev-msg-node-vertx

### Setup Environment

####JavaScript
On the first time you are cloning this repository, you need to run the command ```npm run init-setup```;

After running successfully this command you will have 2 folders (node_modules and vendor), these folders are excluded from the commit process, and are only for development.

If you already have the project configured on your machine, you only need run the command ```npm install``` to add new dependencies;

If you have some trouble with the environment, you can open an issue;

####Java
Follow the link to [Install Maven](https://maven.apache.org/install.html).

Build the project with: mvn package
Run vertx node with: mvn exec:java
