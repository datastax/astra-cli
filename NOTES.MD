
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
