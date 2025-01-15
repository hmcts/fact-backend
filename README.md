# FACT-API

[![Build Status](https://travis-ci.org/hmcts/fact-api.svg?branch=master)](https://travis-ci.org/hmcts/fact-api)

API for the find a court or tribunal service.


## Prerequisites

- [JDK 17](https://www.oracle.com/java)
- Project requires Spring Boot v3.x to be present

## Building and deploying the application.

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Create docker image:

```bash
  docker-compose build
```

Run the distribution (created in `build/install/fact-api` directory)
by executing the following command:

```bash
  docker-compose up
```

This will start the API container exposing the application's port
(set to `8080` in this template app).

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:8080/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Alternative script to run application

To skip all the setting up and building, just execute the following command:

```bash
./bin/run-in-docker.sh
```

For more information:

```bash
./bin/run-in-docker.sh -h
```

Script includes bare minimum environment variables necessary to start api instance. Whenever any variable is changed or any other script regarding docker image/container build, the suggested way to ensure all is cleaned up properly is by this command:

```bash
docker-compose rm
```

It clears stopped containers correctly. Might consider removing clutter of images too, especially the ones fiddled with:

```bash
docker images

docker image rm <image-id>
```

There is no need to remove postgres and java or similar core images.

## Troubleshooting

### Environment Variables

If you encounter any issues, please ensure that you have the correct environment variables set. Please refer to one of the developers for the correct API keys.

This service does use LaunchDarkly for feature flags, so you will need to have the correct API key set in your environment variables.
```
LAUNCH_DARKLY_SDK_KEY = "test_launch_darkly_sdk_key"
```

This service also uses Mapit for geolocation, so you may need to have the correct API key set in your environment variables.
Although using a Mapit key is optional since you'll by default use the public Mapit key, it is recommended to use the subscription key
when running heavy codecept functional tests to avoid rate limiting.

```
MAPIT_KEY = "subscription_mapit_key"
```

### Database

If you are having issues with Docker, please ensure that you have the correct version installed. This service uses Docker Compose, so you will need to have that installed as well.
There are some commands you can use to resolve database related issues
First you can try to restart the database container by running the following commands.
```
  docker-compose down -v
  docker-compose up -d fact-database
```
Then you can check if the container is running by running the logs command.
```
  docker-compose logs
```
Then try restarting the application to build the api.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

