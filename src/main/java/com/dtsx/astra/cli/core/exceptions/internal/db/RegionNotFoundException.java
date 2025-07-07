package com.dtsx.astra.cli.core.exceptions.internal.db;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;

public class RegionNotFoundException extends AstraCliException {
    public RegionNotFoundException(DbRef dbRef, RegionName region) {
        super("""
          @|bold,red Error: Region '%s' not found in database '%s'.|@
        """.formatted(
            region.unwrap(),
            dbRef
        ));
    }
}
