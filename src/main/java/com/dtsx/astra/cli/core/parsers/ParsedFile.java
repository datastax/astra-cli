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

        try (val scanner = new Scanner(path)) {
            return parse.parse(scanner);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
