## Migrations Operations

- Export cassandra `ddl` file  

```
astra cassandra-export-schema \
  --host localhost \
  --username cassandra \
  --password cassandra \
  -d ./tmp
  -k ks1,ks2
```

- Cassandra local backup

```
astra cassandra-backup \
  --host localhost \
  --username cassandra \
  --password cassandra \
  -d ./tmp/backupXXX
  -k ks1,ks2
```

- Astra backup

```
astra db backup mydb \
   -k ks1
   -d ./tmp/astra-backup-xxx 
```

- Restore an backup (astra/cassandra) into astra

```
astra db restore mydb \ 
 -d backup-folder
```

- Live migration cassandra->astra

```
astra cassandra-live mydb
  --host localhost \
  --username cassandra \
  --password cassandra \
  -k ks1, ks2
```

- Import a schema
```
astra db import-schema mydb -f tables.cql -k ks1
```

```
astra db generate-ddl db1 -k ks1 -dest ./ddd  
```

```
astra db copy-keyspace db1 -k ks1 -dest db2  
```





```
java -jar /Users/cedricklunven/.astra/dsbulk-migrator-1.0/dsbulk-migrator-1.0-dsbulk.jar generate-ddl \
  --export-bundle=/Users/cedricklunven/dev/work/scb.zip \
  --export-password=AstraCS:uZclXTYecCAqPPjiNmkezapR:e87d6edb702acd87516e4ef78e0c0e515c32ab2c3529f5a3242688034149a0e4 \
  --export-username=token \
  -k=ks1
```

```
java -jar dsbulk-migrator-1.0-dsbulk.jar \
  migrate-live \
  -e \
  --keyspaces=zdmapp \
  --export-host 127.0.0.1  \
  --export-username=cassandra \
  --export-password=cassandra \
  --import-username=token \
  --import-password=AstraCS:uZclXTYecCAqPPjiNmkezapR:e87d6edb702acd87516e4ef78e0c0e515c32ab2c3529f5a3242688034149a0e4 \
  --import-bundle=${ASTRA_DB_SECURE_BUNDLE_PATH}
```

```
java -jar dsbulk-migrator-1.0-dsbulk.jar generate-ddl --export-host 127.0.0.1 
```

## Interesting commands.

Move Schemas
```
astra db schema-dump <db> -k <keyspace> -f schema.cql
astra db schema-clone <kb> --source ks1 -- dest ks2
```

Export data:
```
astra db backup <db> -k keyspace -f destination
astra db restore <db> -k keyspace -f destination
```

Clone a db
```
astra db clone <db> -destination <db2>
```

```
CREATE TABLE IF NOT EXISTS user (
    email text,
    firstname text,
    lastname text,
    PRIMARY KEY ((email))
);

INSERT INTO user (email, firstname, lastname) VALUES('a@a.com', 'a', 'a');
INSERT INTO user (email, firstname, lastname) VALUES('b@b.com', 'b', 'b');
INSERT INTO user (email, firstname, lastname) VALUES('c@c.com', 'c', 'c');
```

-- FULLL



CLONE PROCEDURE

```
astra db create db_init -k ks1
```

```
astra db cqlsh db_init -e "CREATE TABLE user(id text primary key, name text);insert into user(id, name) VALUES('1', 'cedrick')";
```

-- Create destination

```
astra db create db_target -k ks1
```

-- dump DDL

```
java -jar /Users/cedricklunven/.astra/dsbulk-migrator-1.0/dsbulk-migrator-1.0-dsbulk.jar generate-ddl \
  --export-bundle=/Users/cedricklunven/dev/work/db2.zip \
  --export-password=AstraCS:uZclXTYecCAqPPjiNmkezapR:e87d6edb702acd87516e4ef78e0c0e515c32ab2c3529f5a3242688034149a0e4 \
  --export-username=token \
  -a
  -k=ks1
```

-- import ddl

```
astra db cqlsh sdk_tests -f /Users/cedricklunven/dev/work/data/dsbulk-migrator-ddl-tables.cql
```

-- create scripts

```
java -jar /Users/cedricklunven/.astra/dsbulk-migrator-1.0/dsbulk-migrator-1.0-dsbulk.jar generate-script \
  --export-bundle=/Users/cedricklunven/dev/work/db2.zip \
  --export-password=AstraCS:uZclXTYecCAqPPjiNmkezapR:e87d6edb702acd87516e4ef78e0c0e515c32ab2c3529f5a3242688034149a0e4 \
  --export-username=token \
  --import-bundle=/Users/cedricklunven/dev/work/sdk_test.zip \
  --import-password=AstraCS:uZclXTYecCAqPPjiNmkezapR:e87d6edb702acd87516e4ef78e0c0e515c32ab2c3529f5a3242688034149a0e4 \
  --import-username=token \
  --dsbulk-cmd=/Users/cedricklunven/.astra/dsbulk-1.10.0/bin/dsbulk \
  -k=ks1
```

-- export data

```
dsbulk-migrator-export.sh
```

-- import data

```
dsbulk-migrator-import.sh
```

-- test

```
astra db cqlsh sdk_tests -e "select * from ks1.user;"
```





