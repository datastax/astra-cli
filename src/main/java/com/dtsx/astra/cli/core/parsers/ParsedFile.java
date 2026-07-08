package com.dtsx.astra.cli.core.parsers;

import com.dtsx.astra.cli.core.output.AstraColors;
import lombok.SneakyThrows;
import lombok.val;
import picocli.CommandLine.Help.Ansi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import static com.dtsx.astra.cli.utils.StringUtils.NL;

public abstract class ParsedFile {
    public abstract String render(AstraColors colors);

    @SneakyThrows
    public void writeToFile(Path file) {
        try (val writer = Files.newBufferedWriter(file)) {
            writer.write(render(new AstraColors(Ansi.OFF)));
        }
    }

    protected interface Parser<F, FileParseException extends Exception> {
        F parse(Scanner scanner) throws FileParseException;
    }

    @SuppressWarnings("RedundantThrows")
    protected static <F, FileParseException extends Exception> F readFile(Path path, Parser<F, FileParseException> parse) throws FileNotFoundException, FileParseException {
        if (!Files.exists(path)) {
            throw new FileNotFoundException(path.toString());
        }

        if (Files.isDirectory(path)) {
            throw new FileNotFoundException("Expected a file but found a directory: " + path);
        }

        try {
            // because of how scanner drops the last, trailing newline if it exists, we can mitigate that by just
            // adding another newline that'll always be dropped, allowing the actual last newline to be preserved if it exists,
            // or if there's no trailing newline, then nothing changes anyway
            val content = Files.readString(path) + NL;

            try (val scanner = new Scanner(content)) {
                return parse.parse(scanner);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
