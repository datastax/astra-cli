package com.dtsx.astra.cli.core;

import static com.dtsx.astra.cli.commands.AbstractCmd.DEFAULT_END;
import static com.dtsx.astra.cli.commands.AbstractCmd.DEFAULT_START;

public abstract class CliConstants {
    public static class $Db {
        public static final String LABEL = "DB";
    }

    public static class $Keyspace {
        public static final String LONG = "--keyspace";
        public static final String SHORT = "-k";
        public static final String LABEL = "KEYSPACE";
        public static final String DEFAULT = "default_keyspace";
    }

    public static class $Table {
        public static final String LONG = "--table";
        public static final String SHORT = "-t";
        public static final String LABEL = "TABLE";
    }

    public static class $Collection {
        public static final String LONG = "--collection";
        public static final String SHORT = "-c";
        public static final String LABEL = "COLLECTION";
    }

    public static class $Tenant {
        public static final String LONG = "--tenant";
        public static final String LABEL = "TENANT";
    }

    public static class $Regions {
        public static final String LONG = "--region";
        public static final String SHORT = "-r";
        public static final String LABEL = "REGION";
    }

    public static class $Cloud {
        public static final String LONG = "--cloud";
        public static final String SHORT = "-c";
        public static final String LABEL = "CLOUD";
    }

    public static class $ConfigFile {
        public static final String LONG = "--config-file";
        public static final String SHORT = "-cf";
        public static final String LABEL = "PATH";
        public static final String DEFAULT_DESC = DEFAULT_START + "${cli.rc-file.path}" + DEFAULT_END;
    }

    public static class $Profile {
        public static final String LONG = "--profile";
        public static final String SHORT = "-p";
        public static final String LABEL = "NAME";
    }

    public static class $Token {
        public static final String LONG = "--token";
        public static final String SHORT = "-t";
        public static final String LABEL = "TOKEN";
    }

    public static class $Env {
        public static final String LONG = "--env";
        public static final String SHORT = "-e";
        public static final String LABEL = "ENV";
        public static final String DEFAULT = "prod";
    }
}
