# NAME

`astra` - CLI for DataStax Astra™

# SYNOPSIS

`astra` [ *group* ] *command* [ *command-args* ]

# COMMANDS

- `?`

  Display this help version

- `help`

  Display help information

- `setup`

  Initialize configuration file

- `config create`

  Create a new section in configuration

- `config delete`

  Delete section in configuration

- `config get`

  Show details for a configuration.

- `config list`

  Show the list of available configurations.

- `db count`

  Count items for a table, a query

- `db cqlsh`

  Start Cqlsh

- `db create`

  Create a database with cli

- `db create-dotenv`

  Generate an .env configuration file associate with the db

- `db create-keyspace`

  Create a new keyspace

- `db delete`

  Delete an existing database

- `db download-scb`

  Delete an existing database

- `db dsbulk`

  Count items for a table, a query

- `db get`

  Show details of a database

- `db list`

  Display the list of Databases in an organization

- `db list-keyspaces`

  Display the list of Keyspaces in an database

- `db load`

  Load data leveraging DSBulk

- `db resume`

  Resume a db if needed

- `db status`

  Show status of a database

- `db unload`

  Unload data leveraging DSBulk

- `org get`

  Show details of an organization

- `org id`

  Show organization id.

- `org list-regions-classic`

  Show available regions (classic).

- `org list-regions-serverless`

  Show available regions (serverless).

- `org name`

  Show organization name.

- `role get`

  Show role details

- `role list`

  Display the list of Roles in an organization

- `streaming create`

  Create a tenant in streaming with cli

- `streaming create-dotenv`

  Generate an .env configuration file associate with the tenant

- `streaming delete`

  Delete an existing tenant

- `streaming exist`

  Show existence of a tenant

- `streaming get`

  Show details of a tenant

- `streaming list`

  Display the list of Tenant in an organization

- `streaming pulsar-shell`

  Start pulsar admin against your tenant

- `streaming pulsar-token`

  Show status of a tenant

- `streaming status`

  Show status of a tenant

- `user delete`

  Delete an existing user

- `user get`

  Show user details

- `user invite`

  Invite a user to an organization

- `user list`

  Display the list of Users in an organization

---

# NAME

`astra` `?` - Display this help version

# SYNOPSIS

`astra` `?` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ `--no-color` ] [ {
`-o` | `--output` } *FORMAT* ] [ { `-v` | `--verbose` } ] [ `--version` ]

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-v` , `--verbose`

  Verbose mode with log in console

- `--version`

  Show version

---

# NAME

`astra` `help` - Display help information

# SYNOPSIS

`astra` `help` [ `--` ] [ *command* ]

# OPTIONS

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *command*



---

# NAME

`astra` `setup` - Initialize configuration file

# SYNOPSIS

`astra` `setup` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ `--no-color` ]
[ { `-o` | `--output` } *FORMAT* ] [ { `-t` | `--token` } *TOKEN* ] [ { `-v` |
`--verbose` } ]

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-t` *TOKEN* , `--token` *TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

---

# NAME

`astra` `config` `create` - Create a new section in configuration

# SYNOPSIS

`astra` `config` `create` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [
`--no-color` ] [ { `-o` | `--output` } *FORMAT* ] [ { `-t` | `--token` }
*AuthToken* ] [ { `-v` | `--verbose` } ] [ `--` ] *sectionName*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-t` *AuthToken* , `--token` *AuthToken*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *sectionName*

  Section in configuration file to as as default.

---

# NAME

`astra` `config` `delete` - Delete section in configuration

# SYNOPSIS

`astra` `config` `delete` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [
`--no-color` ] [ { `-o` | `--output` } *FORMAT* ] [ { `-v` | `--verbose` } ] [ `--` ]
*sectionName*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *sectionName*

  Section in configuration file to as as default.

---

# NAME

`astra` `config` `get` - Show details for a configuration.

# SYNOPSIS

`astra` `config` `get` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ { `-k` |
`--key` } *Key in the section* ] [ `--no-color` ] [ { `-o` | `--output` }
*FORMAT* ] [ { `-v` | `--verbose` } ] [ `--` ] *sectionName*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-k` *Key in the section* , `--key` *Key in the section*

  If provided return only value for a key.

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *sectionName*

  Section in configuration file to as as default.

---

# NAME

`astra` `config` `list` - Show the list of available configurations.

# SYNOPSIS

`astra` `config` `list` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [
`--no-color` ] [ { `-o` | `--output` } *FORMAT* ] [ { `-v` | `--verbose` } ]

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-v` , `--verbose`

  Verbose mode with log in console

---

# NAME

`astra` `db` `count` - Count items for a table, a query

# SYNOPSIS

`astra` `db` `count` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ { `-conf`
| `--config` } *CONFIG_SECTION* ] [ `-delim` *delim* ] [ `--dsbulk-config`
*DSBULK_CONF_FILE* ] [ `-encoding` *ENCODING* ] [ `-header` ] [ { `-k` |
`--keyspace` } *KEYSPACE* ] [ `-logDir` *log directory* ] [ { `-m` |
`--schema.mapping` } *mapping* ] [ `-maxConcurrentQueries`
*maxConcurrentQueries* ] [ `-maxErrors` *maxErrors* ] [ `--no-color` ] [ { `-o`
| `--output` } *FORMAT* ] [ `--schema.query` *QUERY* ] [ `-skipRecords`
*skipRecords* ] [ { `-t` | `--table` } *TABLE* ] [ `--token` *AUTH_TOKEN* ] [
`-url` *url* ] [ { `-v` | `--verbose` } ] [ `--` ] *DB*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `-delim` *delim*

  The character(s) to use as field delimiter. Field delimiters containing more
  than one character are accepted.

- `--dsbulk-config` *DSBULK_CONF_FILE*

  Not all options offered by the loader DSBulk are exposed in this CLI

- `-encoding` *ENCODING*

  The file name format to use when writing. This setting is ignored when
  reading and for non-file URLs.

- `-header`

  Enable or disable whether the files to read or write begin with a header
  line.

- `-k` *KEYSPACE* , `--keyspace` *KEYSPACE*

  Keyspace used for loading or unloading data.

- `-logDir` *log directory*

  Optional filter

- `-m` *mapping* , `--schema.mapping` *mapping*

  The field-to-column mapping to use, that applies to both loading and
  unloading; ignored when counting.

- `-maxConcurrentQueries` *maxConcurrentQueries*

  The maximum number of concurrent queries that should be carried in parallel.

- `-maxErrors` *maxErrors*

  The maximum number of errors to tolerate before aborting the entire
  operation.

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--schema.query` *QUERY*

  Optional to unload or count

- `-skipRecords` *skipRecords*

  The number of records to skip from each input file before the parser can
  begin to execute. Note that if the file contains a header line, that line is
  not counted as a valid record. This setting is ignored when writing.

- `-t` *TABLE* , `--table` *TABLE*

  Table used for loading or unloading data. Table names should not be quoted
  and are case-sensitive.

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-url` *url*

  The URL or path of the resource(s) to read from or write to.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *DB*

  Database name (not unique)

---

# NAME

`astra` `db` `cqlsh` - Start Cqlsh

# SYNOPSIS

`astra` `db` `cqlsh` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ { `-conf`
| `--config` } *CONFIG_SECTION* ] [ `--debug` ] [ { `-e` | `--execute` }
*STATEMENT* ] [ `--encoding` *ENCODING* ] [ { `-f` | `--file` } *FILE* ] [ {
`-k` | `--keyspace` } *KEYSPACE* ] [ `--no-color` ] [ { `-o` | `--output` }
*FORMAT* ] [ `--token` *AUTH_TOKEN* ] [ { `-v` | `--verbose` } ] [ `--version`
] [ `--` ] *DB*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--debug`

  Show additional debugging information.

- `-e` *STATEMENT* , `--execute` *STATEMENT*

  Execute the statement and quit.

- `--encoding` *ENCODING*

  Output encoding. Default encoding: utf8.

- `-f` *FILE* , `--file` *FILE*

  Execute commands from a CQL file, then exit.

- `-k` *KEYSPACE* , `--keyspace` *KEYSPACE*

  Authenticate to the given keyspace.

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--version`

  Display information of cqlsh.

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *DB*

  Database name (not unique)

---

# NAME

`astra` `db` `create` - Create a database with cli

# SYNOPSIS

`astra` `db` `create` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ { `-conf`
| `--config` } *CONFIG_SECTION* ] [ { `--if-not-exist` | `--if-not-exists` } ]
[ { `-k` | `--keyspace` } *KEYSPACE* ] [ `--no-color` ] [ { `-o` | `--output` }
*FORMAT* ] [ { `-r` | `--region` } *DB_REGION* ] [ `--timeout` *timeout* ] [
`--token` *AUTH_TOKEN* ] [ { `-v` | `--verbose` } ] [ `--wait` ] [ `--` ] *DB*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--if-not-exist` , `--if-not-exists`

  will create a new DB only if none with same name

- `-k` *KEYSPACE* , `--keyspace` *KEYSPACE*

  Default keyspace created with the Db

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-r` *DB_REGION* , `--region` *DB_REGION*

  Cloud provider region to provision

- `--timeout` *timeout*

  Provide a limit to the wait period in seconds, default is 300s.

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--wait`

  Will wait until the database become ACTIVE

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *DB*

  Database name (not unique)

---

# NAME

`astra` `db` `create-dotenv` - Generate an .env configuration file associate
with the db

# SYNOPSIS

`astra` `db` `create-dotenv` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ {
`-conf` | `--config` } *CONFIG_SECTION* ] [ { `-d` | `--directory` }
*DIRECTORY* ] [ { `-k` | `--keyspace` } *KEYSPACE* ] [ `--no-color` ] [ { `-o`
| `--output` } *FORMAT* ] [ { `-r` | `--region` } *DB_REGION* ] [ `--token`
*AUTH_TOKEN* ] [ { `-v` | `--verbose` } ] [ `--` ] *DB*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `-d` *DIRECTORY* , `--directory` *DIRECTORY*

  Destination for the config file

- `-k` *KEYSPACE* , `--keyspace` *KEYSPACE*

  Default keyspace created with the Db

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-r` *DB_REGION* , `--region` *DB_REGION*

  Cloud provider region to provision

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *DB*

  Database name (not unique)

---

# NAME

`astra` `db` `create-keyspace` - Create a new keyspace

# SYNOPSIS

`astra` `db` `create-keyspace` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [
{ `-conf` | `--config` } *CONFIG_SECTION* ] [ `--if-not-exist` ] { `-k` |
`--keyspace` } *KEYSPACE* [ `--no-color` ] [ { `-o` | `--output` } *FORMAT* ] [
`--token` *AUTH_TOKEN* ] [ { `-v` | `--verbose` } ] [ `--` ] *DB*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--if-not-exist`

  will create a new DB only if none with same name

- `-k` *KEYSPACE* , `--keyspace` *KEYSPACE*

  Name of the keyspace to create

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *DB*

  Database name (not unique)

---

# NAME

`astra` `db` `delete` - Delete an existing database

# SYNOPSIS

`astra` `db` `delete` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ { `-conf`
| `--config` } *CONFIG_SECTION* ] [ `--no-color` ] [ { `-o` | `--output` }
*FORMAT* ] [ `--timeout` *timeout* ] [ `--token` *AUTH_TOKEN* ] [ { `-v` |
`--verbose` } ] [ `--wait` ] [ `--` ] *DB*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--timeout` *timeout*

  Provide a limit to the wait period in seconds, default is 300s.

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--wait`

  Will wait until the database become ACTIVE

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *DB*

  Database name (not unique)

---

# NAME

`astra` `db` `download-scb` - Delete an existing database

# SYNOPSIS

`astra` `db` `download-scb` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ {
`-conf` | `--config` } *CONFIG_SECTION* ] [ { `-f` | `--output-file` } *DEST* ]
[ `--no-color` ] [ { `-o` | `--output` } *FORMAT* ] [ { `-r` | `--region` }
*REGION* ] [ `--token` *AUTH_TOKEN* ] [ { `-v` | `--verbose` } ] [ `--` ] *DB*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `-f` *DEST* , `--output-file` *DEST*

  Destination file

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-r` *REGION* , `--region` *REGION*

  Cloud provider region

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *DB*

  Database name (not unique)

---

# NAME

`astra` `db` `dsbulk` - Count items for a table, a query

# SYNOPSIS

`astra` `db` `dsbulk` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ { `-conf`
| `--config` } *CONFIG_SECTION* ] [ `--no-color` ] [ { `-o` | `--output` }
*FORMAT* ] [ `--token` *AUTH_TOKEN* ] [ { `-v` | `--verbose` } ] [ `--` ] [
*dsbulkArguments* ]

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *dsbulkArguments*

  Provide as many dsbulk parameters as you want.

---

# NAME

`astra` `db` `get` - Show details of a database

# SYNOPSIS

`astra` `db` `get` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ { `-conf` |
`--config` } *CONFIG_SECTION* ] [ { `-k` | `--key` } *Key* ] [ `--no-color` ] [
{ `-o` | `--output` } *FORMAT* ] [ `--token` *AUTH_TOKEN* ] [ { `-v` |
`--verbose` } ] [ `--` ] *DB*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `-k` *Key* , `--key` *Key*

  Show value for a property among: 'id', 'status', 'cloud', 'keyspace',
  'keyspaces', 'region', 'regions'

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *DB*

  Database name (not unique)

---

# NAME

`astra` `db` `list` - Display the list of Databases in an organization

# SYNOPSIS

`astra` `db` `list` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ { `-conf` |
`--config` } *CONFIG_SECTION* ] [ `--no-color` ] [ { `-o` | `--output` }
*FORMAT* ] [ `--token` *AUTH_TOKEN* ] [ { `-v` | `--verbose` } ]

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

---

# NAME

`astra` `db` `list-keyspaces` - Display the list of Keyspaces in an database

# SYNOPSIS

`astra` `db` `list-keyspaces` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ {
`-conf` | `--config` } *CONFIG_SECTION* ] [ `--no-color` ] [ { `-o` |
`--output` } *FORMAT* ] [ `--token` *AUTH_TOKEN* ] [ { `-v` | `--verbose` } ] [ `--` ]
*DB*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *DB*

  Database name (not unique)

---

# NAME

`astra` `db` `load` - Load data leveraging DSBulk

# SYNOPSIS

`astra` `db` `load` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ { `-conf` |
`--config` } *CONFIG_SECTION* ] [ `-delim` *delim* ] [ `-dryRun` ] [
`--dsbulk-config` *DSBULK_CONF_FILE* ] [ `-encoding` *ENCODING* ] [ `-header` ]
[ { `-k` | `--keyspace` } *KEYSPACE* ] [ `-logDir` *log directory* ] [ { `-m` |
`--schema.mapping` } *mapping* ] [ `-maxConcurrentQueries`
*maxConcurrentQueries* ] [ `-maxErrors` *maxErrors* ] [ `--no-color` ] [ { `-o`
| `--output` } *FORMAT* ] [ `--schema.query` *QUERY* ] [ `-skipRecords`
*skipRecords* ] [ { `-t` | `--table` } *TABLE* ] [ `--token` *AUTH_TOKEN* ] [
`-url` *url* ] [ { `-v` | `--verbose` } ] [ `--` ] *DB*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `-delim` *delim*

  The character(s) to use as field delimiter. Field delimiters containing more
  than one character are accepted.

- `-dryRun`

  Enable or disable dry-run mode, a test mode that runs the command but does
  not load data.

- `--dsbulk-config` *DSBULK_CONF_FILE*

  Not all options offered by the loader DSBulk are exposed in this CLI

- `-encoding` *ENCODING*

  The file name format to use when writing. This setting is ignored when
  reading and for non-file URLs.

- `-header`

  Enable or disable whether the files to read or write begin with a header
  line.

- `-k` *KEYSPACE* , `--keyspace` *KEYSPACE*

  Keyspace used for loading or unloading data.

- `-logDir` *log directory*

  Optional filter

- `-m` *mapping* , `--schema.mapping` *mapping*

  The field-to-column mapping to use, that applies to both loading and
  unloading; ignored when counting.

- `-maxConcurrentQueries` *maxConcurrentQueries*

  The maximum number of concurrent queries that should be carried in parallel.

- `-maxErrors` *maxErrors*

  The maximum number of errors to tolerate before aborting the entire
  operation.

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--schema.query` *QUERY*

  Optional to unload or count

- `-skipRecords` *skipRecords*

  The number of records to skip from each input file before the parser can
  begin to execute. Note that if the file contains a header line, that line is
  not counted as a valid record. This setting is ignored when writing.

- `-t` *TABLE* , `--table` *TABLE*

  Table used for loading or unloading data. Table names should not be quoted
  and are case-sensitive.

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-url` *url*

  The URL or path of the resource(s) to read from or write to.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *DB*

  Database name (not unique)

---

# NAME

`astra` `db` `resume` - Resume a db if needed

# SYNOPSIS

`astra` `db` `resume` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ { `-conf`
| `--config` } *CONFIG_SECTION* ] [ `--no-color` ] [ { `-o` | `--output` }
*FORMAT* ] [ `--timeout` *timeout* ] [ `--token` *AUTH_TOKEN* ] [ { `-v` |
`--verbose` } ] [ `--wait` ] [ `--` ] *DB*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--timeout` *timeout*

  Provide a limit to the wait period in seconds, default is 180s.

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--wait`

  Will wait until the database become ACTIVE

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *DB*

  Database name (not unique)

---

# NAME

`astra` `db` `status` - Show status of a database

# SYNOPSIS

`astra` `db` `status` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ { `-conf`
| `--config` } *CONFIG_SECTION* ] [ `--no-color` ] [ { `-o` | `--output` }
*FORMAT* ] [ `--token` *AUTH_TOKEN* ] [ { `-v` | `--verbose` } ] [ `--` ] *DB*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *DB*

  Database name (not unique)

---

# NAME

`astra` `db` `unload` - Unload data leveraging DSBulk

# SYNOPSIS

`astra` `db` `unload` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ { `-conf`
| `--config` } *CONFIG_SECTION* ] [ `-delim` *delim* ] [ `--dsbulk-config`
*DSBULK_CONF_FILE* ] [ `-encoding` *ENCODING* ] [ `-header` ] [ { `-k` |
`--keyspace` } *KEYSPACE* ] [ `-logDir` *log directory* ] [ { `-m` |
`--schema.mapping` } *mapping* ] [ `-maxConcurrentQueries`
*maxConcurrentQueries* ] [ `-maxErrors` *maxErrors* ] [ `--no-color` ] [ { `-o`
| `--output` } *FORMAT* ] [ `--schema.query` *QUERY* ] [ `-skipRecords`
*skipRecords* ] [ { `-t` | `--table` } *TABLE* ] [ `--token` *AUTH_TOKEN* ] [
`-url` *url* ] [ { `-v` | `--verbose` } ] [ `--` ] *DB*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `-delim` *delim*

  The character(s) to use as field delimiter. Field delimiters containing more
  than one character are accepted.

- `--dsbulk-config` *DSBULK_CONF_FILE*

  Not all options offered by the loader DSBulk are exposed in this CLI

- `-encoding` *ENCODING*

  The file name format to use when writing. This setting is ignored when
  reading and for non-file URLs.

- `-header`

  Enable or disable whether the files to read or write begin with a header
  line.

- `-k` *KEYSPACE* , `--keyspace` *KEYSPACE*

  Keyspace used for loading or unloading data.

- `-logDir` *log directory*

  Optional filter

- `-m` *mapping* , `--schema.mapping` *mapping*

  The field-to-column mapping to use, that applies to both loading and
  unloading; ignored when counting.

- `-maxConcurrentQueries` *maxConcurrentQueries*

  The maximum number of concurrent queries that should be carried in parallel.

- `-maxErrors` *maxErrors*

  The maximum number of errors to tolerate before aborting the entire
  operation.

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--schema.query` *QUERY*

  Optional to unload or count

- `-skipRecords` *skipRecords*

  The number of records to skip from each input file before the parser can
  begin to execute. Note that if the file contains a header line, that line is
  not counted as a valid record. This setting is ignored when writing.

- `-t` *TABLE* , `--table` *TABLE*

  Table used for loading or unloading data. Table names should not be quoted
  and are case-sensitive.

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-url` *url*

  The URL or path of the resource(s) to read from or write to.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *DB*

  Database name (not unique)

---

# NAME

`astra` `org` `get` - Show details of an organization

# SYNOPSIS

`astra` `org` `get` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ { `-conf` |
`--config` } *CONFIG_SECTION* ] [ `--no-color` ] [ { `-o` | `--output` }
*FORMAT* ] [ `--token` *AUTH_TOKEN* ] [ { `-v` | `--verbose` } ]

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

---

# NAME

`astra` `org` `id` - Show organization id.

# SYNOPSIS

`astra` `org` `id` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ { `-conf` |
`--config` } *CONFIG_SECTION* ] [ `--no-color` ] [ { `-o` | `--output` }
*FORMAT* ] [ `--token` *AUTH_TOKEN* ] [ { `-v` | `--verbose` } ]

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

---

# NAME

`astra` `org` `list-regions-classic` - Show available regions (classic).

# SYNOPSIS

`astra` `org` `list-regions-classic` [ { `-cf` | `--config-file` }
*CONFIG_FILE* ] [ { `-conf` | `--config` } *CONFIG_SECTION* ] [ `--no-color` ]
[ { `-o` | `--output` } *FORMAT* ] [ `--token` *AUTH_TOKEN* ] [ { `-v` |
`--verbose` } ]

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

---

# NAME

`astra` `org` `list-regions-serverless` - Show available regions (serverless).

# SYNOPSIS

`astra` `org` `list-regions-serverless` [ { `-cf` | `--config-file` }
*CONFIG_FILE* ] [ { `-conf` | `--config` } *CONFIG_SECTION* ] [ `--no-color` ]
[ { `-o` | `--output` } *FORMAT* ] [ `--token` *AUTH_TOKEN* ] [ { `-v` |
`--verbose` } ]

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

---

# NAME

`astra` `org` `name` - Show organization name.

# SYNOPSIS

`astra` `org` `name` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ { `-conf`
| `--config` } *CONFIG_SECTION* ] [ `--no-color` ] [ { `-o` | `--output` }
*FORMAT* ] [ `--token` *AUTH_TOKEN* ] [ { `-v` | `--verbose` } ]

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

---

# NAME

`astra` `role` `get` - Show role details

# SYNOPSIS

`astra` `role` `get` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ { `-conf`
| `--config` } *CONFIG_SECTION* ] [ `--no-color` ] [ { `-o` | `--output` }
*FORMAT* ] [ `--token` *AUTH_TOKEN* ] [ { `-v` | `--verbose` } ] [ `--` ]
*ROLE*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *ROLE*

  Role name or identifier

---

# NAME

`astra` `role` `list` - Display the list of Roles in an organization

# SYNOPSIS

`astra` `role` `list` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ { `-conf`
| `--config` } *CONFIG_SECTION* ] [ `--no-color` ] [ { `-o` | `--output` }
*FORMAT* ] [ `--token` *AUTH_TOKEN* ] [ { `-v` | `--verbose` } ]

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

---

# NAME

`astra` `streaming` `create` - Create a tenant in streaming with cli

# SYNOPSIS

`astra` `streaming` `create` [ { `-c` | `--cloud` } *cloudProvider* ] [ { `-cf`
| `--config-file` } *CONFIG_FILE* ] [ { `-conf` | `--config` } *CONFIG_SECTION*
] [ { `-e` | `--email` } *email* ] [ `--no-color` ] [ { `-o` | `--output` }
*FORMAT* ] [ { `-p` | `--plan` } *plan* ] [ { `-r` | `--region` } *cloudRegion*
] [ `--token` *AUTH_TOKEN* ] [ { `-v` | `--verbose` } ] [ `--` ] *TENANT*

# OPTIONS

- `-c` *cloudProvider* , `--cloud` *cloudProvider*

  Cloud Provider to create a tenant

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `-e` *email* , `--email` *email*

  User Email

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-p` *plan* , `--plan` *plan*

  Plan for the tenant

- `-r` *cloudRegion* , `--region` *cloudRegion*

  Cloud Region for the tenant

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *TENANT*

  Tenant name (unique for the region)

---

# NAME

`astra` `streaming` `create-dotenv` - Generate an .env configuration file
associate with the tenant

# SYNOPSIS

`astra` `streaming` `create-dotenv` [ { `-cf` | `--config-file` } *CONFIG_FILE*
] [ { `-conf` | `--config` } *CONFIG_SECTION* ] [ { `-d` | `--directory` }
*DIRECTORY* ] [ `--no-color` ] [ { `-o` | `--output` } *FORMAT* ] [ `--token`
*AUTH_TOKEN* ] [ { `-v` | `--verbose` } ] [ `--` ] *TENANT*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `-d` *DIRECTORY* , `--directory` *DIRECTORY*

  Destination for the config file

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *TENANT*

  Tenant name

---

# NAME

`astra` `streaming` `delete` - Delete an existing tenant

# SYNOPSIS

`astra` `streaming` `delete` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ {
`-conf` | `--config` } *CONFIG_SECTION* ] [ `--no-color` ] [ { `-o` |
`--output` } *FORMAT* ] [ `--token` *AUTH_TOKEN* ] [ { `-v` | `--verbose` } ] [ `--` ]
*TENANT*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *TENANT*

  Tenant name

---

# NAME

`astra` `streaming` `exist` - Show existence of a tenant

# SYNOPSIS

`astra` `streaming` `exist` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ {
`-conf` | `--config` } *CONFIG_SECTION* ] [ `--no-color` ] [ { `-o` |
`--output` } *FORMAT* ] [ `--token` *AUTH_TOKEN* ] [ { `-v` | `--verbose` } ] [ `--` ]
*TENANT*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *TENANT*

  Tenant name

---

# NAME

`astra` `streaming` `get` - Show details of a tenant

# SYNOPSIS

`astra` `streaming` `get` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ {
`-conf` | `--config` } *CONFIG_SECTION* ] [ { `-k` | `--key` } *Key* ] [
`--no-color` ] [ { `-o` | `--output` } *FORMAT* ] [ `--token` *AUTH_TOKEN* ] [
{ `-v` | `--verbose` } ] [ `--` ] *TENANT*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `-k` *Key* , `--key` *Key*

  Show value for a property among: 'status', 'cloud', 'pulsar_token', 'region'

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *TENANT*

  Tenant name

---

# NAME

`astra` `streaming` `list` - Display the list of Tenant in an organization

# SYNOPSIS

`astra` `streaming` `list` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ {
`-conf` | `--config` } *CONFIG_SECTION* ] [ `--no-color` ] [ { `-o` |
`--output` } *FORMAT* ] [ `--token` *AUTH_TOKEN* ] [ { `-v` | `--verbose` } ]

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

---

# NAME

`astra` `streaming` `pulsar-shell` - Start pulsar admin against your tenant

# SYNOPSIS

`astra` `streaming` `pulsar-shell` [ { `-cf` | `--config-file` } *CONFIG_FILE*
] [ { `-conf` | `--config` } *CONFIG_SECTION* ] [ { `-e` | `--execute-command`
} *command* ] [ { `-f` | `--filename` } *FILE* ] [ `--fail-on-error` ] [
`--no-color` ] [ { `-np` | `--no-progress` } ] [ { `-o` | `--output` } *FORMAT*
] [ `--token` *AUTH_TOKEN* ] [ { `-v` | `--verbose` } ] [ `--` ] *TENANT*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `-e` *command* , `--execute-command` *command*

  Execute the statement and quit.

- `-f` *FILE* , `--filename` *FILE*

  Input filename with a list of commands to be executed. Each command must be
  separated by a newline.

- `--fail-on-error`

  If true, the shell will be interrupted if a command throws an exception.

- `--no-color`

  Remove all colors in output

- `-np` , `--no-progress`

  Display raw output of the commands without the fancy progress visualization.

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *TENANT*

  Tenant unique name

---

# NAME

`astra` `streaming` `pulsar-token` - Show status of a tenant

# SYNOPSIS

`astra` `streaming` `pulsar-token` [ { `-cf` | `--config-file` } *CONFIG_FILE*
] [ { `-conf` | `--config` } *CONFIG_SECTION* ] [ `--no-color` ] [ { `-o` |
`--output` } *FORMAT* ] [ `--token` *AUTH_TOKEN* ] [ { `-v` | `--verbose` } ] [ `--` ]
*TENANT*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *TENANT*

  Tenant name

---

# NAME

`astra` `streaming` `status` - Show status of a tenant

# SYNOPSIS

`astra` `streaming` `status` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ {
`-conf` | `--config` } *CONFIG_SECTION* ] [ `--no-color` ] [ { `-o` |
`--output` } *FORMAT* ] [ `--token` *AUTH_TOKEN* ] [ { `-v` | `--verbose` } ] [ `--` ]
*TENANT*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *TENANT*

  Tenant name

---

# NAME

`astra` `user` `delete` - Delete an existing user

# SYNOPSIS

`astra` `user` `delete` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ {
`-conf` | `--config` } *CONFIG_SECTION* ] [ `--no-color` ] [ { `-o` |
`--output` } *FORMAT* ] [ `--token` *AUTH_TOKEN* ] [ { `-v` | `--verbose` } ] [ `--` ]
*EMAIL*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *EMAIL*

  User email or identifier

---

# NAME

`astra` `user` `get` - Show user details

# SYNOPSIS

`astra` `user` `get` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ { `-conf`
| `--config` } *CONFIG_SECTION* ] [ `--no-color` ] [ { `-o` | `--output` }
*FORMAT* ] [ `--token` *AUTH_TOKEN* ] [ { `-v` | `--verbose` } ] [ `--` ]
*EMAIL*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *EMAIL*

  User Email

---

# NAME

`astra` `user` `invite` - Invite a user to an organization

# SYNOPSIS

`astra` `user` `invite` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ {
`-conf` | `--config` } *CONFIG_SECTION* ] [ `--no-color` ] [ { `-o` |
`--output` } *FORMAT* ] [ { `-r` | `--role` } *ROLE* ] [ `--token` *AUTH_TOKEN*
] [ { `-v` | `--verbose` } ] [ `--` ] *EMAIL*

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-r` *ROLE* , `--role` *ROLE*

  Role for the user (default is Database Administrator)

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *EMAIL*

  User Email

---

# NAME

`astra` `user` `list` - Display the list of Users in an organization

# SYNOPSIS

`astra` `user` `list` [ { `-cf` | `--config-file` } *CONFIG_FILE* ] [ { `-conf`
| `--config` } *CONFIG_SECTION* ] [ `--no-color` ] [ { `-o` | `--output` }
*FORMAT* ] [ `--token` *AUTH_TOKEN* ] [ { `-v` | `--verbose` } ]

# OPTIONS

- `-cf` *CONFIG_FILE* , `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `--token` *AUTH_TOKEN*

  Key to use authenticate each call.

- `-v` , `--verbose`

  Verbose mode with log in console

