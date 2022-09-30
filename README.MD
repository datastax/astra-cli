# Astra CLI

[![License Apache2](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Coverage Status](https://coveralls.io/repos/github/datastax/astra-cli/badge.svg?branch=main)](https://coveralls.io/github/datastax/astra-cli?branch=main)
[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-white.svg)](https://sonarcloud.io/summary/new_code?id=clun_astra-cli)


Astra Shell is a command line interface to operate the Datastax Astra platform through the terminal. 

The component is still under development (version `0.1.alpha5`) stay tuned.

Astra Cli official documentation is located on [Awesome-Astra](https://awesome-astra.github.io/docs/pages/astra/astra-cli/).


### Native Compilation

To avoid the pre-requisites of JRE we want to build a native image.

#### Documentation

https://www.graalvm.org/dev/reference-manual/native-image/guides/use-native-image-maven-plugin/
https://simply-how.com/fix-graalvm-native-image-compilation-issues
https://github.com/oracle/graal/blob/master/docs/reference-manual/native-image/Reflection.md
https://github.com/oracle/graal/blob/master/docs/reference-manual/native-image/Limitations.md


#### Commands

- Build without native

```
mvn clean install -Dmaven.test.skip=true
```

- Run with Agent:

```
java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image -jar target/astra-cli-0.1.alpha5-shaded.jar org list
```

- Native build

```
mvn clean install -Pnative -Dmaven.test.skip=true
```

- Sonar source

```
mvn verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=clun_astra-cli
```

- Coverall

```
mvn clean test jacoco:report coveralls:report
```

