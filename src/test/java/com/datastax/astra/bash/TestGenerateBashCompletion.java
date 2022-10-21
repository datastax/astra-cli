package com.datastax.astra.bash;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.datastax.astra.cli.AstraCli;
import com.github.rvesse.airline.Cli;
import com.github.rvesse.airline.help.GlobalUsageGenerator;
import com.github.rvesse.airline.help.cli.bash.BashCompletionGenerator;

public class TestGenerateBashCompletion {

    /** Autocompletion file generated. */
    private static final String DESTINATION = "src/main/dist/astra-cli-autocomplete.sh";
    
    @Test
    public void testGenerateAutoComplete() {
        Cli<Runnable> cli = new Cli<>(AstraCli.class);
        GlobalUsageGenerator<Runnable> helpGenerator = new BashCompletionGenerator<>();
        try {
            FileOutputStream fos = new FileOutputStream(DESTINATION);
            helpGenerator.usage(cli.getMetadata(), fos);
            fos.write("\n\nexport PATH=\"$PATH:$HOME/.astra/cli\"".getBytes());
            Assertions.assertTrue(new File(DESTINATION).exists());


        } catch (IOException e) {
            e.printStackTrace();
           throw new IllegalArgumentException("Test should fail");
        }
    }
}



