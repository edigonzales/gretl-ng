# Developer manual

## System requirements
For building the current version of _GRETL_, you will need a JRE (Java Runtime Environment) installed on your system. It works only with Java 8 since some dependencies (iox-wkf -> GeoTools) will not run with Java 11.

## Subprojects
The _GRETL_ repository is organized as Gradle multi-project:

* `gretl`: _GRETL_ source code with unit tests _and_ integration tests.
* `runtimeImage`: Subproject for building the _GRETL_ runtime (docker) image. The docker image is tested against the integration tests, too.

## Building
- java...
- docker image

## Testing
- falls Kategorien
- unit vs. integration tests
- jar und docker


