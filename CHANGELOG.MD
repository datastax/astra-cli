# Changelog

## v0.1.alpha4 

### In Progress
- [ ] - Change create keyspace `astra db keyspace create workshops -k XXX`
- [ ] - Version should no fail if no config
- [ ] - Delete keyspace
- [ ] - List keyspaces
- [ ] - `astra db keyspace backup workshops -k xxx`

### 8/30/2022
- [X] - Adding a CHANGELOG to the solution
- [X] - Output [INFO] on success / human
- [X] - Wait for async operation to complete with `--wait` for create, resume
- [X] - New command: `db status`
- [X] - Remove DSBulk warning on credentials (0 is too low, 1 is too high)
 
###  8/22/2022
- New commands `db resume`, `db download-scb`
- Moved to dedicated repository

## v0.1.alpha3 (August 2022)
- New commands: DSBulk Integration
- Proper installation script

## v0.1.alpha2 (July 2022)
- New commands: CqlSh Integration
- `astra setup` with token save

## v0.1.alpha1 (June 2022)
- Basics `astra db` CRUD.
- Configuration Management with `astra config`

