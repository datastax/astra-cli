
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
astra streaming create-cdc <tenant>
    -db,--database <db>
    -k,--keyspace  <keyspace>
    -t,--table  <table>
    -p,--partition <partition>

#Namespace

#Topics 
astra streaming list-topics <tenant> (--namespace default)
astra streaming create-topic <tenant> --topic <topic> (--namespace default) --persistent -partition X
astra streaming delete-topic <tenant> --topic <topic> (--namespace default)
astra streaming write-topic <tenant> --topic <topic> (--message "Hello World")


#Sinks
#Source



```