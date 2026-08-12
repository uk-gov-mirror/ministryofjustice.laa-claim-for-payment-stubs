# stub-provider-details-api

A standalone Spring Boot application providing a WireMock stub for the Provider Details API.

## Running locally

Start the stub server from the root of the project:

```shell
./gradlew :stub-provider-details-api:bootRun
```

The WireMock server runs on port `8093`.

## Quick sanity check

```shell
curl http://localhost:8093/health
```

Expected response:

```json
{
  "status": "UP"
}
```

## Running tests

```shell
./gradlew :stub-provider-details-api:integrationTest
```
