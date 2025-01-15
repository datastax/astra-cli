CREATE TABLE IF NOT EXISTS default_keyspace.demo (foo text PRIMARY KEY, bar text);
CREATE TABLE IF NOT EXISTS default_keyspace.table2 (foo text PRIMARY KEY, bar text);
INSERT INTO default_keyspace.demo(foo,bar) VALUES('1','item1');
INSERT INTO default_keyspace.demo(foo,bar) VALUES('1','item2');

