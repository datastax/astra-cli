package com.datastax.astra.bash;

import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.datastax.astra.shell.AstraCli;
import com.github.rvesse.airline.Cli;
import com.github.rvesse.airline.help.GlobalUsageGenerator;
import com.github.rvesse.airline.help.cli.bash.BashCompletionGenerator;

public class GenerateBashCompletion {

    @Test
    public void generateAutoComplete() {
        Cli<Runnable> cli = new Cli<Runnable>(AstraCli.class);
        GlobalUsageGenerator<Runnable> helpGenerator = new BashCompletionGenerator<>();
        try {
            FileOutputStream fos = new FileOutputStream("dist/astra-init.sh");
            helpGenerator.usage(cli.getMetadata(), fos);
            
            /** Add cli to the classpath. */
            fos.write("\n\nexport PATH=\"$PATH:$HOME/.astra/cli\"".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



