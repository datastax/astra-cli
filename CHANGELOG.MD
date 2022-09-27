# Changelog

To get the latest version:
```
curl -Ls "https://dtsx.io/get-astra-cli" | bash
```

### BACKLOG

- [ ] - Speed up setup with `astra setup --token <token>`
- [ ] - Improved and specialized error messages
- [ ] - Native compilation with GraalVM


- [ ] - Add a region to a db `astra db create-region <myDb> -r <regionName>`
- [ ] - Remove a region to a db `astra db delete-region <myDb> -r <regionName>`
- [ ] - List db regions `astra db list-regions`
- [ ] - Parking a DB (Astra Classic) `astra db park <myDb>`
- [ ] - UnParking a DB (Astra Classic) `astra db unpark <myDb>`
- [ ] - Keyspace backup (leveraging dsbulk) `astra db backup-keyspace <myDb> -k <myKeyspace>`
- [ ] - Keyspace restore (leveraging dsbulk) `astra db restore-keyspace <myDb> -k <myKeyspace> -input <folder>`


## v0.1.alpha5

### 9/13/2022
- [x] - Show current organization `astra org`
- [x] - Show current organization `astra org id`
- [x] - Show current organization `astra org name`
- [x] - Show current organization `astra org list-regions-serverless`
- [x] - Show current organization `astra org list-regions-classic`

### 9/12/2022

**DB:**
- [x] - Show property of db `astra db get --key id`

**STREAMING:**
- [x] - Support for PulsarShell (yeah!) `astra streaming pulsar-shell <my_tenant>`
- [x] - Test Tenant existence `astra streaming exist <my_tenant>`
- [x] - Decoupling PulsarClient from SDK (0.3.4)
- [x] - Tenant creation `astra streaming create <my_tenant>`
- [x] - List Tenants `astra streaming list`
- [x] - Delete Tenants `astra streaming delete <my_tenant>`
- [x] - Get Tenant details `astra streaming get <my_tenant>`
- [x] - Get Tenant token `astra streaming pulsar-token <my_tenant>`
- [x] - Get Tenant status `astra streaming status <my_tenant> `
- [x] - Get Raw Tenant property `astra streaming get <my_tenant> --key cloud`


## v0.1.alpha4

### 9/7/2022
- [x] - Reduce size of the deliverable
- [X] - Update versions: `slf4j`, `junit`, `airline`
- [X] - Fix interactive shell
- [X] - List keyspaces `astra db list-keyspaces <myDb>`
- [X] - Implement guardrail to work with 2 db of same name
- [X] - Mutualisations of exceptions for `DatabaseNotFound`, `DatabaseNotUnique` and `InvalidParams`
- [X] - Create keyspace with if not exist `astra db create-keyspace <myDb> -k <myKeyspace> --if-not-exist`

### 8/30/2022
- [X] - Adding a CHANGELOG files to check changes
- [x] - update to DSBulk `1.10.0`
- [x] - `astra --version` should no fail if no config available
- [X] - Output [INFO] on success / human
- [X] - Wait for async operation to complete with `--wait` for create, resume
- [X] - New command: `db status`
- [X] - Remove DSBulk warning on credentials (0 is too low, 1 is too high)
 
###  8/22/2022
- [X] - Moved to dedicated repository `/datastax/astra-cli`
- [X] - Commands to resume a db `db resume`
- [X] - Commands to download secure bundles `db download-scb`
- [X] - Update Setup to be straight forward

## v0.1.alpha3 (August 2022)
- [X] - DSBulk Integration `astra db dsbulk dbID load ...`
- [X] - Release and Installation script
- [X] - Log file

## v0.1.alpha2 (July 2022)
- [X] - CqlSh Integration `astra db cqlsh XX`
- [X] - Role commands `astra roles`: `list`, `get`
- [X] - User commands `astra users`: `list`, `get`, `invite`, `delete`
- `astra setup` with token saved in `~/.astrarc`
- [X] - option `--no-color`
- [X] - output `-o csv`

## v0.1.alpha1 (June 2022)
- [X] - Basics `astra db` CRUD
- [X] - Astra Shell with interactive shell
- [X] - Configuration Management with `astra config`
