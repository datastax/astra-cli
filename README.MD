# Getting Started with Astra CLI

[![License Apache2](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)

Astra Shell is a standalone command line interface proposing functions to operate the Datastax Astra platform.

## 📋 Table of content

<img src="https://github.com/datastax/astra-sdk-java/blob/main/docs/img/astra-shell.png?raw=true" align="right" width="400px"/>

1. [Installation](#1-installation)
2. [Configuration Management](#2-configuration-management)
3. [Working with Databases](#3-working-with-databases)
4. [Working with Users](#4-working-with-users)
5. [Working with Roles](#5-working-with-roles)

## 1. Installation

### 1.1 - Prequisites

On your machine you will need :
- A bash shell
- Commands: `unzip`, `curl`
- A Java JRE or JDK 8+ (temporary)

You will also need to:
- [Create an Astra account](https://astra.datastax.com)
- [Create an authentication token](https://dtsx.io/create-astra-token)

### 1.2 - Installation

```
curl -Ls "https://dtsx.io/get-astra-cli" | bash
```

### 1.3 - Setup

#### ✅ Step 1.3.a - Execute `startup` command

```
astra setup
```

#### ✅ Step 1.3.b - Provide a token of your choice when needed

```
Enter your token (AstraCS...) : 
AstraCS:.......
```

![](https://github.com/datastax/astra-sdk-java/blob/main/docs/img/astra-shell-config.png?raw=true")

```
[What's NEXT ?]
You are all set. You can now:
   • Use any command, 'astra help' will get you the list
   • Try with 'astra db list'
   • Enter interactive mode using 'astra'

Happy Coding !
```

#### ✅ Step 1.3.c - Open a `NEW` shell to finalize install

- Get the current version

```
astra --version
```

- Get some more informations

```
astra help
```

- Use you configuration (spoiler)

```
astra user list
```

## 2. Configuration Management

#### ✅ Step 2.1 - List Configuration

- In your configuration file you see all config and one is the default. This is the one used when no option provided.

```
astra config list
```

#### ✅ Step 2.2 - Create new configuration

If you have multiple organizations it could be useful to switch from one to another.

- Create another org called `dev`

```
astra config create dev -t <token_of_org_2>
```

- Look at the new list with `dev`

```
astra config list
```

- But nothing changed, your organization is still selected as `default`

```
astra user list
```

- You can use any organization anytime with `--config <onfig_name>` or `-t <token>`

```
astra user list --config dev
```


#### ✅ Step 2.3 - Change default configuration

You can change the default org at any time with the following command:

- Change the default
```
astra config default dev
```

- See your new list 

 ```
 astra config list
 ```

- Test again, user list should be different now

```
astra user list
```

- Back to original org (change with your email)

```
astra config default <your_email>
```

- Check the modification

```
astra config list
```

#### ✅ Step 2.4 = Delete configuration

You can delete any organization. I see you.. if you delete the default one you are on your own and you would need to define a new one.

- Delete you config
```
astra config delete dev
```

- See the new list

```
astra config list
```

## 3. Working with Databases

#### ✅ Step 3.1 List Databases

```
astra db list
```

#### ✅ Step 3.2 Create a new database

If not provided the region will be the default free region and the keyspace will be the database name but you can change then with `-r` and `-k` respectivitely.

- Create the Database

```
astra db create demo
```

- Check the status

```
astra db list
```

#### ✅ Step 3.3 Get information on your database

```
astra db get demo
```

> **Note:** You never provided any the technical ids.


#### ✅ Step 3.4 Output Format

This CLI wil be used by CI/CD and script, sometimes the output must be parsed. There are 3 outputs: *human,json,csv"

- Show the list as a json

```
astra db list -o json
```

- Show the list as a csv

```
astra db list -o csv
```

#### ✅ Step 3.5 Troubleshooting

The return type is also important to check the errors. O mean everything is OK

- Any command can be execute as `verbose` addin `-v`

```
astra db list -v
```

Return codes are carefully designed to ease their usage later:

- Execute command with error
```
astra db get i_do_not_exist -v
```

- Get the error code
```
echo $?
```

- Error codes table:

| Error             | Code      |
|-------------------|-----------|
| SUCCESS           | 0         |
| PARSE_ERROR       | 1         |
| INVALID_PARAMETER | 10        |
| NOT_FOUND         | 20        |
| CONFLICT          | 21        |
| ALREADY_EXIST     | 22        |
| CANNOT_CONNECT    | 30        |
| INTERNAL_ERROR    | 40        |


#### ✅ Step 3.6 - If not Exists

The cli implemented `If not Exist` for both keyspace and database (convenient for workshops and training)


- Run the command with `--if-not-exist`

```
astra db create demo --if-not-exist
```

- All commands can also be in versbose mode anytime with `-v`

```
astra db create demo --if-not-exist -v
```

Make sure the database is now Active or you will get some expected errors.

```
astra db get demo | grep ACTIVE
```

#### ✅ Step 3.7 - Create Keyspace
```
astra db create-keyspace demo -k ks2
```

- Keyspace is created

```
astra db get demo
```

#### ✅ Step 3.8 - Blow your mind 🥁

The first execution is expected to be slow we are downloading cqlsh if not present on your machine. This is why we put the `-v`.

```
astra db cqlsh demo -v
```

- Run a cql command

```sql
describe keyspace demo;
```

- Quit the `cqlshell` not forgetting the `;`

```sql
quit;
```

- Execute a CQL statement (or a CQL file with `-f`)

```
astra db cqlsh demo -e "describe keyspaces;"
```

#### ✅ Step 3.9 - Interactive Shell

Commands we used so far are stateless, no connection is maintained after the execution. What if you need more an interactive shell than a cli ?

- Start the shell by do not providing any command

```
astra
```

- List the DB 

```
db list
```

- Now what if we want some commands on this particular db ?

```
db use demo
```

- Get info of a 

```
info
```

> **Note**: *Still not sure about the info command to display the current context of the shell.*

- Create a keyspace

```
create keyspace kso
```

- See the created keyspace 

```
info
```

- With Database back to active you can now access the keyspace:

```
cqlsh -k kso
```

- You can quit the db with 

```
quit
```

- You can exit the shell with

```
exit
```

## 4. Working with Users

#### ✅ Step 4.1 - List your users

```
astra user list
```

#### ✅ Step 4.2 - Invite your best friend (me). 

- The role will be `DataStax Administrator` by default

```
astra user invite cedrick.lunven@datastax.com
```

- And here I am 

```
astra user list
```

#### ✅ Step 4.3 - Delete a user

```
astra user delete cedrick.lunven@datastax.com
```

## 5. Working with Roles

#### ✅ Step 5.1 - List your roles

```
astra role list
```

#### ✅ Step 5.2 - Get a role

```
astra role get "Organization Administrator"
```





