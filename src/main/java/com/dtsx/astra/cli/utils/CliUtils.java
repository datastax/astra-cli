package com.dtsx.astra.cli.utils;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.ExitCode;
import com.dtsx.astra.cli.core.output.Hint;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class CliUtils {
    public static List<String> removeDbFromExtraArgs(List<String> extraArgs, String example) {
        if (!extraArgs.isEmpty()) {
            if (extraArgs.getFirst().trim().startsWith("-")) {
                throw new AstraCliException(ExitCode.VALIDATION_ISSUE, """
                  @|bold,red Database must explicitly be passed as the first positional argument when using extra flags after '--'|@
            
                  @|italic Note: if you really have a database name that starts with a dash which triggered this check, pass the db's ID instead (which you can get with @!${cli.name} db list!@).|@
                """, List.of(
                    new Hint("Example usage", "${cli.name} " + example)
                ));
            }
            return extraArgs.subList(1, extraArgs.size());
        }
        return extraArgs;
    }
}
