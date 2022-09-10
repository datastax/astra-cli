package com.datastax.astra.cli.cmd;

import java.util.ArrayList;
import java.util.List;

import com.datastax.astra.cli.ExitCode;
import com.github.rvesse.airline.annotations.Command;

/**
 * Unforce update of the program.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "update", description = " Update this program to latest version. Make sure that you have sufficient permissions (run with sudo if needed)")
public class UpdateCmd extends BaseCmd {
    
    /** {@inheritDoc} */
    public ExitCode execute() {
       List<String> cmdUpdate = new ArrayList<>();
       cmdUpdate.add("curl");
       cmdUpdate.add("-Ls");
       cmdUpdate.add("\"https://dtsx.io/get-astra-cli\"");
       //cmdUpdate.add("|");
       //cmdUpdate.add("bash");
       try {
           Process p = new ProcessBuilder(
                   cmdUpdate.toArray(new String[0]))
               .inheritIO()
               .start();
           p.waitFor();
       } catch (Exception e) {
           e.printStackTrace();
           throw new IllegalStateException("Cannot update program: " + e.getMessage());
       }
       return ExitCode.SUCCESS;
    }

}
