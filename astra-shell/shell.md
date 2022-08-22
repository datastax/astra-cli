# NAME

`shell` - Interactive Shell for DataStax Astra

# SYNOPSIS

`shell` [ *group* ] *command* [ *command-args* ]

# COMMANDS

- `<empty>`

  New line

- `connect`

  Connect to another Astra Organization

- `cqlsh`

  Start Cqlsh (db must be selected first)

- `create`

  Create a new keyspace (db must be selected)

- `exit`

  Exit program.

- `help`

  View help for any command

- `info`

  Show details of a database (db must be selected)

- `quit`

  Remove scope focus on an entity (prompt changed).

- `db create`

  Create a database with shell

- `db delete`

  Delete an existing database

- `db info`

  Show details of a database (db must be selected)

- `db list`

  Display the list of Databases in an organization

- `db use`

  Select a database in the shell

- `keyspace create`

  Create a new keyspace (db must be selected)

- `role get`

  Show role details

- `role list`

  Display the list of Roles in an organization

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

`shell` `<empty>` - New line

# SYNOPSIS

`shell` `<empty>`

---

# NAME

`shell` `connect` - Connect to another Astra Organization

# SYNOPSIS

`shell` `connect` [ `--no-color` ] [ { `-o` | `--output` } *FORMAT* ] [ { `-v`
| `--verbose` } ] [ `--` ] *configName*

# OPTIONS

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *configName*

  Section name in configuration file

---

# NAME

`shell` `cqlsh` - Start Cqlsh (db must be selected first)

# SYNOPSIS

`shell` `cqlsh` [ `--debug` ] [ { `-e` | `--execute` } *STATEMENT* ] [
`--encoding` *ENCODING* ] [ { `-f` | `--file` } *FILE* ] [ { `-k` |
`--keyspace` } *KEYSPACE* ] [ `--no-color` ] [ { `-o` | `--output` } *FORMAT* ]
[ { `-v` | `--verbose` } ] [ `--version` ]

# OPTIONS

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

- `-v` , `--verbose`

  Verbose mode with log in console

- `--version`

  Display information of cqlsh.

---

# NAME

`shell` `create` - Create a new keyspace (db must be selected)

# SYNOPSIS

`shell` `create` [ `--if-not-exist` ] [ `--no-color` ] [ { `-o` | `--output` }
*FORMAT* ] [ { `-v` | `--verbose` } ] [ `--` ] *KEYSPACE*

# OPTIONS

- `--if-not-exist`

  will create a new DB only if none with same name

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *KEYSPACE*

  Name of the keyspace

---

# NAME

`shell` `exit` - Exit program.

# SYNOPSIS

`shell` `exit` [ `--no-color` ] [ { `-o` | `--output` } *FORMAT* ] [ { `-v` |
`--verbose` } ]

# OPTIONS

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-v` , `--verbose`

  Verbose mode with log in console

---

# NAME

`shell` `help` - View help for any command

# SYNOPSIS

`shell` `help` [ `--` ] [ *command* ]

# OPTIONS

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *command*



---

# NAME

`shell` `info` - Show details of a database (db must be selected)

# SYNOPSIS

`shell` `info` [ `--no-color` ] [ { `-o` | `--output` } *FORMAT* ] [ { `-v` |
`--verbose` } ]

# OPTIONS

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-v` , `--verbose`

  Verbose mode with log in console

---

# NAME

`shell` `quit` - Remove scope focus on an entity (prompt changed).

# SYNOPSIS

`shell` `quit` [ `--no-color` ] [ { `-o` | `--output` } *FORMAT* ] [ { `-v` |
`--verbose` } ]

# OPTIONS

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-v` , `--verbose`

  Verbose mode with log in console

---

# NAME

`shell` `db` `create` - Create a database with shell

# SYNOPSIS

`shell` `db` `create` [ `--if-not-exist` ] [ { `-ks` | `--keyspace` }
*KEYSPACE* ] [ `--no-color` ] [ { `-o` | `--output` } *FORMAT* ] [ { `-r` |
`--region` } *DB_REGION* ] [ { `-v` | `--verbose` } ] [ `--` ] *DB_NAME*

# OPTIONS

- `--if-not-exist`

  will create a new DB only if none with same name

- `-ks` *KEYSPACE* , `--keyspace` *KEYSPACE*

  Default keyspace created with the Db

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-r` *DB_REGION* , `--region` *DB_REGION*

  Cloud provider region to provision

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *DB_NAME*

  Database name (not unique)

---

# NAME

`shell` `db` `delete` - Delete an existing database

# SYNOPSIS

`shell` `db` `delete` [ `--no-color` ] [ { `-o` | `--output` } *FORMAT* ] [ {
`-v` | `--verbose` } ] [ `--` ] *DB*

# OPTIONS

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *DB*

  Database name or identifier

---

# NAME

`shell` `db` `info` - Show details of a database (db must be selected)

# SYNOPSIS

`shell` `db` `info` [ `--no-color` ] [ { `-o` | `--output` } *FORMAT* ] [ {
`-v` | `--verbose` } ]

# OPTIONS

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-v` , `--verbose`

  Verbose mode with log in console

---

# NAME

`shell` `db` `list` - Display the list of Databases in an organization

# SYNOPSIS

`shell` `db` `list` [ `--no-color` ] [ { `-o` | `--output` } *FORMAT* ] [ {
`-v` | `--verbose` } ]

# OPTIONS

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-v` , `--verbose`

  Verbose mode with log in console

---

# NAME

`shell` `db` `use` - Select a database in the shell

# SYNOPSIS

`shell` `db` `use` [ `--no-color` ] [ { `-o` | `--output` } *FORMAT* ] [ { `-v`
| `--verbose` } ] [ `--` ] *DB*

# OPTIONS

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *DB*

  Database name or identifier

---

# NAME

`shell` `keyspace` `create` - Create a new keyspace (db must be selected)

# SYNOPSIS

`shell` `keyspace` `create` [ `--if-not-exist` ] [ `--no-color` ] [ { `-o` |
`--output` } *FORMAT* ] [ { `-v` | `--verbose` } ] [ `--` ] *KEYSPACE*

# OPTIONS

- `--if-not-exist`

  will create a new DB only if none with same name

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *KEYSPACE*

  Name of the keyspace

---

# NAME

`shell` `role` `get` - Show role details

# SYNOPSIS

`shell` `role` `get` [ { `-conf` | `--config` } *CONFIG_SECTION* ] [
`--config-file` *CONFIG_FILE* ] [ `--log` *LOG_FILE* ] [ `--no-color` ] [ {
`-o` | `--output` } *FORMAT* ] [ { `-t` | `--token` } *AUTH_TOKEN* ] [ { `-v` |
`--verbose` } ] [ `--` ] *ROLE*

# OPTIONS

- `-conf` *CONFIG_SECTION* , `--config` *CONFIG_SECTION*

  Section in configuration file (default = ~/.astrarc)

- `--config-file` *CONFIG_FILE*

  Configuration file (default = ~/.astrarc)

- `--log` *LOG_FILE*

  Logs will go in the file plus on console

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-t` *AUTH_TOKEN* , `--token` *AUTH_TOKEN*

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

`shell` `role` `list` - Display the list of Roles in an organization

# SYNOPSIS

`shell` `role` `list` [ `--no-color` ] [ { `-o` | `--output` } *FORMAT* ] [ {
`-v` | `--verbose` } ]

# OPTIONS

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-v` , `--verbose`

  Verbose mode with log in console

---

# NAME

`shell` `user` `delete` - Delete an existing user

# SYNOPSIS

`shell` `user` `delete` [ `--no-color` ] [ { `-o` | `--output` } *FORMAT* ] [ {
`-v` | `--verbose` } ] [ `--` ] *EMAIL*

# OPTIONS

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *EMAIL*

  User email or identifier

---

# NAME

`shell` `user` `get` - Show user details

# SYNOPSIS

`shell` `user` `get` [ `--no-color` ] [ { `-o` | `--output` } *FORMAT* ] [ {
`-v` | `--verbose` } ] [ `--` ] *EMAIL*

# OPTIONS

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *EMAIL*

  User Email

---

# NAME

`shell` `user` `invite` - Invite a user to an organization

# SYNOPSIS

`shell` `user` `invite` [ `--no-color` ] [ { `-o` | `--output` } *FORMAT* ] [ {
`-r` | `--role` } *ROLE* ] [ { `-v` | `--verbose` } ] [ `--` ] *EMAIL*

# OPTIONS

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-r` *ROLE* , `--role` *ROLE*

  Role for the user (default is Database Administrator)

- `-v` , `--verbose`

  Verbose mode with log in console

- `--`

  This option can be used to separate command-line options from the list of
  arguments (useful when arguments might be mistaken for command-line options)

- *EMAIL*

  User Email

---

# NAME

`shell` `user` `list` - Display the list of Users in an organization

# SYNOPSIS

`shell` `user` `list` [ `--no-color` ] [ { `-o` | `--output` } *FORMAT* ] [ {
`-v` | `--verbose` } ]

# OPTIONS

- `--no-color`

  Remove all colors in output

- `-o` *FORMAT* , `--output` *FORMAT*

  Output format, valid values are: human,json,csv

- `-v` , `--verbose`

  Verbose mode with log in console

