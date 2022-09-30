# Astra CLI

[![License Apache2](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Coverage Status](https://coveralls.io/repos/github/datastax/astra-cli/badge.svg?branch=main)](https://coveralls.io/github/datastax/astra-cli?branch=main)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-cli&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=clun_astra-cli)

Astra Shell is a command line interface to operate the Datastax Astra platform through the terminal. 

The component is still under development (version `0.1.alpha5`) stay tuned.


### Code Quality

- Coverall reports

```
mvn clean test jacoco:report coveralls:report
```

- Sonar Report

```
mvn verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=clun_astra-cli
```


### Native Compilation

To avoid the pre-requisites of JRE we want to build a native image.

- Build without native

```
mvn clean install
```

- Run with Agent:

```
java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image -jar target/astra-cli-0.1.alpha5-shaded.jar org list
```

- Native build

```
mvn clean install -Pnative
```


