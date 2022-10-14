# Astra CLI

[![License Apache2](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)

[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-cli&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=clun_astra-cli)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-cli&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=clun_astra-cli)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-cli&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=clun_astra-cli)

[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-cli&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=clun_astra-cli)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-cli&metric=bugs)](https://sonarcloud.io/summary/new_code?id=clun_astra-cli)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-cli&metric=coverage)](https://sonarcloud.io/summary/new_code?id=clun_astra-cli)

Astra CLI provides a command line interface in a terminal to operate Datastax Astra. The goal is to offer access to any feature without accessing the user interface. The component is still under development (version `0.1.alpha5`), it will keep moving before the release GA.

## Getting started

The extensive documentation will be provided in [Awesome Astra](https://awesome-astra.github.io/docs/pages/astra/astra-cli/)

### QuickStar

- Install the CLI with 

```bash
curl -Ls "https://dtsx.io/get-astra-cli" | bash
```

- Open a new terminal to enable the autocompletion and add Astra in the path.

- Enter your token 

```
astra setup
```

- You are good to go 

```
astra db list
```



### Developement Commands

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

[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-cli&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=clun_astra-cli)

