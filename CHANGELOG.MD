# Changelog

To get the latest version:
```
curl -Ls "https://dtsx.io/get-astra-cli" | bash
```

## v0.1.alpha4 

### In Progress
- [ ] - Change create keyspace `astra db keyspace create workshops -k XXX`
- [ ] - Delete keyspace
- [ ] - List keyspaces
- [ ] - `astra db keyspace backup workshops -k xxx`

### 8/30/2022
- [X] - Adding a CHANGELOG files to check changes
- [x] - update to DSBulk `1.10.0`
- [x] - `astra --version` should no fail if no config available
- [X] - Output [INFO] on success / human
- [X] - Wait for async operation to complete with `--wait` for create, resume
- [X] - New command: `db status`
- [X] - Remove DSBulk warning on credentials (0 is too low, 1 is too high)
 
###  8/22/2022
- New commands `db resume`, `db download-scb`
- Moved to dedicated repository `/datastax/astra-cli`

## v0.1.alpha3 (August 2022)
- New commands: DSBulk Integration
- Proper installation script

## v0.1.alpha2 (July 2022)
- New commands: CqlSh Integration
- `astra setup` with token save

## v0.1.alpha1 (June 2022)
- Basics `astra db` CRUD.
- Configuration Management with `astra config`

