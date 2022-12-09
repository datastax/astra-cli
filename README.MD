# Astra CLI

[![License Apache2](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)
![Latest Release](https://img.shields.io/github/v/release/datastax/astra-cli)

[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-cli&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=clun_astra-cli)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-cli&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=clun_astra-cli)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-cli&metric=sqale_rating)](https://sonarcloud.io/summary/overall?id=clun_astra-cli)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-cli&metric=vulnerabilities)](https://sonarcloud.io/summary/overall?id=clun_astra-cli)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-cli&metric=bugs)](https://sonarcloud.io/summary/new_code?id=clun_astra-cli)

[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-cli&metric=coverage)](https://sonarcloud.io/summary/new_code?id=clun_astra-cli)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=clun_astra-cli&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=clun_astra-cli)
![downloads](https://img.shields.io/github/downloads/datastax/astra-cli/total)

Astra CLI provides a command line interface in a terminal to operate Datastax Astra. The goal is to offer access to any feature without accessing the user interface. The component is still under development (version `0.1.alpha5`), it will keep moving before the release GA.

> ```
> The component is still under development (version `0.1.alpha5`)
> ```

## Getting started

The extensive documentation will be provided in [Awesome Astra](https://awesome-astra.github.io/docs/pages/astra/astra-cli/)

### QuickStart

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

