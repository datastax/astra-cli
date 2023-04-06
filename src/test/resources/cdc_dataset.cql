CREATE TABLE IF NOT EXISTS astra_cli_test.demo (foo text PRIMARY KEY, bar text);
CREATE TABLE IF NOT EXISTS astra_cli_test.table2 (foo text PRIMARY KEY, bar text);
INSERT INTO astra_cli_test.demo(foo,bar) VALUES('1','item1');
INSERT INTO astra_cli_test.demo(foo,bar) VALUES('1','item2');

