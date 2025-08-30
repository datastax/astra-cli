package com.dtsx.astra.cli.snapshot.commands.db;

import com.dtsx.astra.cli.AstraCli;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class DbGetCmdSnapshotTest extends BaseCmdSnapshotTest {
    @Nested
    public class human {
        @Test
        public void gets_full_information_about_a_database() {
            AstraCli.run(null, null, "db", "get", "my_db");


        }
    }
}
