package com.dtsx.astra.cli.core.exceptions.db;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.AstraColors;

import java.util.UUID;

public class RegionNotFoundException extends AstraCliException {
    public RegionNotFoundException(DbRef dbRef, String region) {
        super("""
          @|bold,red Error: Region '%s' not found in database '%s'.|@
        """.formatted(
            region,
            dbRef
        ));
    }
}
