
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


Pulsar
```
#cdc
astra streaming list-cdcs <tenant>

astra streaming create-cdc <tenant> \
    -db,--database <db> \
    -k,--keyspace  <keyspace> \
    -t,--table     <table> \
    -p,--partition <partition>

astra streaming create-cdc <tenant>

astra db create-cdc ${DB} \
    --tenant ${TENANT} \
    -k,--keyspace ${KEYSPACE_NAME} \
    -t,--table ${TABLE_NAME} \
    -p,--partition ${NUM_PARTITION}


#Namespace

#Topics 
astra streaming list-topics <tenant> (--namespace default)
astra streaming create-topic <tenant> --topic <topic> (--namespace default) --persistent -partition X
astra streaming delete-topic <tenant> --topic <topic> (--namespace default)
astra streaming write-topic <tenant> --topic <topic> (--message "Hello World")


#Sinks
#Source

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