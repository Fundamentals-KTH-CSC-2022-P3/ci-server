# Continuous Integration Server

Welcome to our implementation of a minimalistic CI-Server.

## Browse the javadocs

To create a _browsable_ version of the documentation run the following two commands

```bash
mvn javadoc:javadoc
python -m http.server 8080 --directory target/site/apidocs
```

If you navigate to [http://localhost:8080/](http://localhost:8080/) a html rendered version of the documentation should now be available.

## Core CI Features

### Compilation

#### Implementation

The CI server clones the repository specified in the payload provided by the GitHub webhook,
checks out the correct branch and then runs `mvn compile` in the root of the
repository. If the return code of `mvn compile` is 0, the build is considered
successful, otherwise it is considered a failure.

#### Unit-tested

The compilation is unit tested by cloning a repository that is guaranteed (by
GitHub actions) to be in a compilable state, and then trying to compile that repo
and verifying that it passes. Another unit test is constituted by disturbing the
source code of that repository to an uncompilable state and verifying that compilation
fails. **This should change to mocking the necessary behaviour.**

### Testing

#### Implementation

The CI server runs the tests of the tested repo by running `mvn test` in the root of the tested repo.
This generates a Surefire test report, which is what the CI server looks through to find any failed tests.


#### Unit-tested

This functionality is unit tested by creating dummy reports in a directory that is empty save for the
correct Surefire report folder structure, and checking the result of looking through these dummy reports.

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


## Statement of Contributions

- Set up of the project; maven, GitHub actions, initial server code etc.
  - Samuel Philipson
  - Ludwig Kristoffersson
- Communication with GitHub, setting repository status
  - Marcus Alevärn
- Local file storage
  - Marcus Alevärn
- Dope web UI
  - Marcus Alevärn
- Managing the local repository
  - Arvid Siberov
- Compiling project
  - Ludwig Kristoffersson
  - Arvid Siberov
- Running tests
  - Arvid Siberov
  - Katrina Liang
  - Ludwig Kristoffersson
- Writing Essence report
  - Katrina Liang
- Authentication
  - Samuel Philipson
- Code review, planning and issue creation
  - Marcus Alevärn
  - Ludwig Kristoffersson
  - Katrina Liang
  - Samuel Philipson
  - Arvid Siberov

