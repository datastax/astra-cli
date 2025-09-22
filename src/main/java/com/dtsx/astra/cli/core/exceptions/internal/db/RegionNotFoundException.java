package com.dtsx.astra.cli.core.exceptions.internal.db;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;

import static com.dtsx.astra.cli.core.output.ExitCode.REGION_NOT_FOUND;

public class RegionNotFoundException extends AstraCliException {
    public RegionNotFoundException(DbRef dbRef, RegionName region) {
        super(REGION_NOT_FOUND, """
          @|bold,red Error: Region '%s' not found in database '%s'.|@
        """.formatted(
            region.unwrap(),
            dbRef
        ));
    }
}
