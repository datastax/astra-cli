package com.dtsx.astra.cli.docs;

import com.dtsx.astra.cli.AstraCli;
import com.github.rvesse.airline.Cli;
import com.github.rvesse.airline.help.GlobalUsageGenerator;
import com.github.rvesse.airline.help.markdown.MarkdownMultiPageGlobalUsageGenerator;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;

public class MarkDownDocumentationGenerator {

    @Test
    public void generateMDDocumentation() {
        Cli<Runnable> cli = new Cli<>(AstraCli.class);

        GlobalUsageGenerator<Runnable> helpGenerator = new MarkdownMultiPageGlobalUsageGenerator<>();
        try {
            //helpGenerator.usage(cli.getMetadata(), System.out);

            FileOutputStream out = new FileOutputStream("cli.md");
            helpGenerator.usage(cli.getMetadata(), out);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
