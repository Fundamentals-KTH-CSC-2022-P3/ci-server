# Continuous Integration Server

Welcome to our implementation of a minimalistic CI-Server.

## Core CI Features

### Compilation
#### Implemented
#### Unit-tested

### Testing
#### Implemented
#### Unit-tested

### Notification

We have implemented one notification mechanism which is that
the CI server sets the commit status.

#### Implemented

We created a new Github account called *dd2480-ci-user* which we added to our
repository. Furthermore, we generated a personal access token for this account.
Using the personal access token for basic authorization we could create a POST
request to set the status of a commit.
We followed the [commits API](https://docs.github.com/en/rest/reference/commits#create-a-commit-status) specification
to make this work.

#### Unit-tested

We check most of the fields in the generated HTTP request to ensure that they contain valid values.
The values that are valid are specified in [commits API](https://docs.github.com/en/rest/reference/commits#create-a-commit-status).
We check the following parts of our HTTP request.
- The URL path.
- The HTTP method.
- *Content-Type* and *Accept* headers.
- Basic authorization credentials.
- The JSON object stored in the HTTP body.


## Setup

This project uses the following tools:

- Java 17
- Maven
- IntelliJ Ultimate

### About the java version

We'd recommend using the OpenJDK version of Java 17, though it should still work independently of JVM implementation.

### IntelliJ IDEA for students

This project is created and coded with IntelliJ IDEA Ultimate. You can follow the link here for the [license for the IDEA](https://www.jetbrains.com/community/education/#students), and then install it through any package manager of your choice. Use your license to verify your copy.

The community edition of IntelliJ IDEA should work as well, but it isn't tested.

### Maven

Install maven through a package manager. Open the project in IntelliJ and go to:

1. Click File -> Settings.
2. Expand Build, Execution, Deployment -> Build Tools -> Maven.
3. Check Use plugin registry.
4. Click OK or Apply.

Restart IntelliJ for the changes to take effect. 

After restart, open the right Maven pane and click the Reload All Maven Projects button, and then run the _clean_ and 
_install_ Lifecycles.  

## Run/Debug Config

Set up a run and debug config by:

1. Click Configurations -> Edit Configurations.
2. Add a configuration to the list of type Application
3. Choose Java 17 as SDK and choose main class as _fundamentals.server.ContinuousIntegrationServer_
4. Click OK or Apply