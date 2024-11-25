# NAME

`astra` - CLI for DataStax Astra™

# SYNOPSIS

`astra` [ *group* ] *command* [ *command-args* ]

# COMMANDS

- `?`

  Display this help version

- `help`

  Display help information

- `login`

  Authenticate against Astra.

- `setup`

  Initialize configuration file

- `config create`

  Create a new section in configuration

- `config delete`

  Delete section in configuration

- `config describe`

  Show details for a configuration.

- `config get`

  Show details for a configuration.

- `config list`

  Show the list of available configurations.

- `config use`

  Make a section the one used by default

- `db count`

  Count items for a table, a query

- `db cqlsh`

  Start Cqlsh

- `db create`

  Create a database with cli

- `db create-cdc`

  Create a CDC to pulsar

- `db create-collection`

  Create a new collection

- `db create-dotenv`

  Generate an .env configuration file associate with the db

- `db create-keyspace`

  Create a new keyspace

- `db create-region`

  Expand database to a new region

- `db delete`

  Delete an existing database

- `db delete-cdc`

  Delete a CDC connection

- `db delete-collection`

  Delete an existing collection

- `db delete-keyspace`

  Delete an existing keyspace

- `db delete-region`

  Delete a region from a database

- `db describe`

  Show details of a database

- `db describe-collection`

  Describe an existing collection

- `db describe-embedding-provider`

  Describe an existing embedding provider

- `db download-scb`

  Download secure connect bundle archive for a region

- `db get`

  Show details of a database

- `db get-endpoint-api`

  Open the json api endpoint

- `db get-endpoint-playground`

  Expand database to a new region

- `db get-endpoint-swagger`

  Open the swagger user interface

- `db list`

  Display the list of Databases in an organization

- `db list-cdc`

  List CDC available on this databse

- `db list-clouds`

  Show available clouds

- `db list-collections`

  Display the list of collections in an database

- `db list-embedding-providers`

  Display the list of embedding providers in an database

- `db list-keyspaces`

  Display the list of Keyspaces in an database

- `db list-regions`

  List regions for a database

- `db list-regions-classic`

  Show available regions (classic).

- `db list-regions-serverless`

  Show available regions (serverless).

- `db list-regions-vector`

  Show available regions (vector).

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

- `org name`

  Show organization name.

- `role describe`

  Show role details

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

- `streaming describe`

  Show details of a tenant

- `streaming exist`

  Show existence of a tenant

- `streaming get`

  Show details of a tenant

- `streaming list`

  Display the list of Tenant in an organization

- `streaming list-cdc`

  List CDC available on this tenant

- `streaming list-clouds`

  Display the list of clouds

- `streaming list-regions`

  Display the list of Tenant in an organization

- `streaming pulsar-shell`

  Start pulsar admin against your tenant

- `streaming pulsar-token`

  Show status of a tenant

- `streaming status`

  Show status of a tenant

- `token create`

  Display the list of tokens in an organization

- `token delete`

  Delete a token

- `token get`

  Show current token

- `token list`

  Display the list of tokens in an organization

- `token revoke`

  Revoke a token (delete)

- `user delete`

  Delete an existing user

- `user describe`

  Show user details

- `user get`

  Show user details

- `user invite`

  Invite a user to an organization

- `user list`

  Display the list of Users in an organization