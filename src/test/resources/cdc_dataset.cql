CREATE TABLE IF NOT EXISTS ks1.demo (foo text PRIMARY KEY, bar text);
CREATE TABLE IF NOT EXISTS ks1.table2 (foo text PRIMARY KEY, bar text);
INSERT INTO ks1.demo(foo,bar) VALUES('1','item1');
INSERT INTO ks1.demo(foo,bar) VALUES('1','item2');

