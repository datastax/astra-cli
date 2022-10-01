# Astra CLI

[![License Apache2](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Coverage Status](https://coveralls.io/repos/github/datastax/astra-cli/badge.svg?branch=main)](https://coveralls.io/github/datastax/astra-cli?branch=main)

[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-cli&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=clun_astra-cli)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-cli&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=clun_astra-cli)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-cli&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=clun_astra-cli)

Astra CLI provides a command line interface in a terminal to operate Datastax Astra. The goal is to offer access to any feature without accessing the user interface.


The component is still under development (version `0.1.alpha5`) stay tuned.

### Installation 

#### 1. Prequisites

On your machine you will need the following components:
- A bash shell (Bourne Again SHell)
- The `curl` command.

Complete those steps, you need a security token to setup the `cli`. 
- [Create an Astra account](https://astra.dev/3B7HcYo)
- [Generate an Astra Token](https://awesome-astra.github.io/docs/pages/astra/create-token/)

#### 2. Installation

To install or reinstall the CLI use the following command. Previous installations will be cleaned but configuration will NOT be lost. The cli is installed in ~/.astra/cli folder whereas your configuration is saved in ~/.astrarc file.

```bash
curl -Ls "https://dtsx.io/get-astra-cli" | bash
```

### Developement 

- Generate Jacoco report and push to Coverall

```bash
mvn clean test jacoco:report coveralls:report
```

- Generate Sonar report and push to Coverall

```bash
export SONAR_TOKEN=...
mvn verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=clun_astra-cli
```

- Build the project

```
mvn clean install
```

- Run with Agent:

```
java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image -jar target/astra-cli-0.1.alpha5-shaded.jar org list
```

- Build as a native image

```
mvn clean install -Pnative
```


