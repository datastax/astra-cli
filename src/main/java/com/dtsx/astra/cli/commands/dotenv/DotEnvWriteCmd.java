package com.dtsx.astra.cli.commands.dotenv;

import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;

import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.dotenv.DotEnvOperation.*;
import picocli.CommandLine.Command;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

@Command(
    name = "write",
    description = "Fill bound keys and write to a .env file."
)
public class DotEnvWriteCmd extends AbstractDotEnvGenCmd {
    @Override
    protected boolean isPrint() {
        return false;
    }

    @Override
    protected OutputAll execute(Supplier<DotEnvResult> result) {
        return switch (result.get()) {
            case CreatedDotEnvFile(var outputFile) -> OutputAll.response(
                """
                A new env file has been created at %s.
                
                Please double check the content of the file before using it, and manually add it to your .gitignore file.
                """.formatted(ctx.highlight(outputFile)),
                mkData("created", false, outputFile),
                List.of(new Hint("View the env file", "cat " + outputFile))
            );

            case UpdatedDotEnvFile(var outputFile, var overwritten) -> OutputAll.response(
                """
                The .env file has been updated at %s.
                
                If there were any duplicate keys, they %s.
                
                Please double check the content of the file before using it, and ensure it is added to your .gitignore file.
                """.formatted(
                    ctx.highlight(outputFile),
                    overwritten ? "were overwritten" : "were left in the env file, with the new keys added to the end"
                ),
                mkData("updated", overwritten, outputFile),
                List.of(new Hint("View the env file", "cat " + outputFile))
            );

            case NothingToUpdate(var outputFile) -> OutputAll.response(
                """
                No changes needed to be made to the .env file at %s.
                """.formatted(ctx.highlight(outputFile)),
                mkData("no_change", false, outputFile),
                List.of(new Hint("View the env file", "cat " + outputFile))
            );

            case CreatedDotEnvContent _ -> throw new CongratsYouFoundABugException("Should not be able to get to CreatedDotEnvContent in DotEnvWriteCmd");
        };
    }

    private LinkedHashMap<String, Object> mkData(String status, boolean overwriteOccurred, Path file) {
        return sequencedMapOf(
            "status", status,
            "overwriteOccurred", overwriteOccurred,
            "file", Optional.ofNullable(file).map(Path::toString)
        );
    }
}
