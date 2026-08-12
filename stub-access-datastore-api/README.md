# stub-access-datastore-api

A standalone Spring Boot application providing a WireMock stub for the Access Datastore API.

## Running locally

Start the stub server from the root of the project:

```shell
./gradlew :stub-access-datastore-api:bootRun
```

The WireMock server runs on port `8093`.

## Quick sanity check

```shell
curl http://localhost:8093/hello-world
```

Expected response:

```text
Hello World
```