package com.dtsx.astra.cli.bash;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.dtsx.astra.cli.AstraCli;
import com.github.rvesse.airline.Cli;
import com.github.rvesse.airline.help.GlobalUsageGenerator;
import com.github.rvesse.airline.help.cli.bash.BashCompletionGenerator;

public class GenerateBashCompletionTest {

    /** Autocompletion file generated. */
    private static final String DESTINATION = "src/main/dist/astra-init.sh";
    
    @Test
    public void testGenerateAutoComplete() {
        Cli<Runnable> cli = new Cli<>(AstraCli.class);
        GlobalUsageGenerator<Runnable> helpGenerator = new BashCompletionGenerator<>();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            helpGenerator.usage(cli.getMetadata(), baos);
            try (FileOutputStream fos = new FileOutputStream(DESTINATION)) {
				fos.write(baos.toString().replace("#!/bin/bash", "" +
				        "#!/bin/bash\n\n" +
				        "# Support for zsh\n" +
				        "autoload -U +X compinit > /dev/null 2>&1 && compinit\n" +
				        "autoload -U +X bashcompinit > /dev/null 2>&1 && bashcompinit\n").getBytes());

				//helpGenerator.usage(cli.getMetadata(), fos);
				fos.write("\n\nexport PATH=\"$PATH:$HOME/.astra/cli\"".getBytes());
			}
            Assertions.assertTrue(new File(DESTINATION).exists());

        } catch (IOException e) {
            e.printStackTrace();
           throw new IllegalArgumentException("Test should fail");
        }
    }
}



