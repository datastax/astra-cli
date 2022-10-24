package com.datastax.astra.cli;

import com.datastax.astra.cli.config.*;
import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.core.CliContext;
import com.datastax.astra.cli.core.DefaultCmd;
import com.datastax.astra.cli.core.ExitCode;
import com.datastax.astra.cli.core.exception.*;
import com.datastax.astra.cli.core.out.AstraCliConsole;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.cli.db.*;
import com.datastax.astra.cli.db.cqlsh.DbCqlShellCmd;
import com.datastax.astra.cli.db.dsbulk.DbCountCmd;
import com.datastax.astra.cli.db.dsbulk.DbDsBulkCmd;
import com.datastax.astra.cli.db.dsbulk.DbLoadCmd;
import com.datastax.astra.cli.db.dsbulk.DbUnLoadCmd;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.datastax.astra.cli.db.exception.InvalidDatabaseStateException;
import com.datastax.astra.cli.db.exception.KeyspaceAlreadyExistException;
import com.datastax.astra.cli.db.keyspace.DbCreateKeyspaceCmd;
import com.datastax.astra.cli.db.keyspace.DbListKeyspacesCmd;
import com.datastax.astra.cli.iam.*;
import com.datastax.astra.cli.iam.exception.RoleNotFoundException;
import com.datastax.astra.cli.iam.exception.UserAlreadyExistException;
import com.datastax.astra.cli.iam.exception.UserNotFoundException;
import com.datastax.astra.cli.org.*;
import com.datastax.astra.cli.streaming.*;
import com.datastax.astra.cli.streaming.exception.TenantAlreadyExistException;
import com.datastax.astra.cli.streaming.exception.TenantNotFoundException;
import com.datastax.astra.cli.streaming.pulsarshell.PulsarShellCmd;
import com.github.rvesse.airline.Cli;
import com.github.rvesse.airline.annotations.Group;
import com.github.rvesse.airline.help.Help;
import com.github.rvesse.airline.parser.errors.*;
import org.fusesource.jansi.AnsiConsole;

import java.util.Arrays;
import java.util.List;

/**
 * Main class for the program. Will route commands to proper class 
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@com.github.rvesse.airline.annotations.Cli(
  name = "astra", 
  description = "CLI for DataStax Astra™ ",
  defaultCommand = DefaultCmd.class, 
  commands = { 
    SetupCmd.class, Help.class, 
    DefaultCmd.class
  },
  groups = {
    
    @Group(
       name = "config", 
       description = "Manage configuration file", 
       defaultCommand = ConfigListCmd.class, 
       commands = {
         ConfigCreateCmd.class, ConfigGetCmd.class, ConfigDeleteCmd.class,
         ConfigListCmd.class
    }),
   
    @Group(name = "org", 
      description = "Display Organization Info", 
      defaultCommand = OrgCmd.class,  
      commands = {
        OrgIdCmd.class, 
        OrgNameCmd.class,
        OrgListRegionsClassicCmd.class, 
        OrgListRegionsServerlessCmd.class
    }),
    
    @Group(
       name = "db", 
       description = "Manage databases",
       defaultCommand = DbListCmd.class, 
       commands = { 
         // Create,delete
         DbCreateCmd.class,  DbDeleteCmd.class,
         // Read
         DbListCmd.class,  DbGetCmd.class, DbStatusCmd.class,
         // Operation
         DbResumeCmd.class, DbDownloadScbCmd.class, DbCreateDotEnvCmd.class,
         // Keyspaces
         DbCreateKeyspaceCmd.class, DbListKeyspacesCmd.class,
         // DsBulk
         DbCountCmd.class, DbLoadCmd.class, DbUnLoadCmd.class, DbDsBulkCmd.class,
         // Cqlshell
         DbCqlShellCmd.class,
     }),
    
    @Group(
       name = "streaming", 
       description = "Manage Streaming tenants", 
       defaultCommand = StreamingListCmd.class, 
       commands = { 
         // Create, Delete
         StreamingCreateCmd.class, StreamingDeleteCmd.class,
         // Read
         StreamingListCmd.class, StreamingGetCmd.class,
         StreamingExistCmd.class, StreamingStatusCmd.class, 
         StreamingPulsarTokenCmd.class, StreamingCreateDotEnvCmd.class,
         // Pulsar Shell
         PulsarShellCmd.class
    }),
    
    @Group(
       name= "role", 
       description = "Manage roles", 
       defaultCommand = RoleListCmd.class, 
       commands = {
         RoleListCmd.class, RoleGetCmd.class
    }),
    
    @Group(
       name= "user", 
       description = "Manage users", 
       defaultCommand = UserListCmd.class, 
       commands = {
         UserGetCmd.class, UserInviteCmd.class, UserDeleteCmd.class,
         UserListCmd.class
    }),
})
public class AstraCli {
    
    /**
     * Main Program.
     *
     * @param args
     *           start options for the shell
     */
    public static void main(String[] args) {
        // Enable Colors in terminal
        AnsiConsole.systemInstall();
       
        // Parse command
        ExitCode code = run(AstraCli.class, args);
        
        // Enable Colors in terminal
        AnsiConsole.systemUninstall();
        
        // Exit with proper to code
        System.exit(code.getCode());
    }
    
    /**
     * Parsing command and error relative to command.
     * 
     * @param clazz
     *      class name to marshall
     * @param args
     *      command line arguments
     * @return
     *      code for the parsing
     */
    public static ExitCode run(Class<?> clazz, String[] args) {
        try {
            
            // Persist command line to log it later
            CliContext.getInstance().setArguments(Arrays.asList(args));
            
            // Parse and Run
            new Cli<AbstractCmd>(clazz).parse(args).run();
            
            // Return all good
            return ExitCode.SUCCESS;
            
        } catch(ClassCastException ce) {
            // Help does its own things
            new Cli<Runnable>(clazz).parse(args).run();
            return ExitCode.SUCCESS;
        } catch(ParseArgumentsUnexpectedException |
                ParseArgumentsMissingException    |
                ParseTooManyArgumentsException    |
                ParseOptionGroupException ex) {
            LoggerShell.exception(ex, getInvalidCmd(args), null);
            return ExitCode.INVALID_ARGUMENT;
        } catch(ParseOptionIllegalValueException | 
                ParseOptionMissingException ex) {
            LoggerShell.exception(ex, getCmd(args), null);
            return ExitCode.INVALID_OPTION;
        } catch(ParseRestrictionViolatedException |
                ParseOptionConversionException ex) {
            LoggerShell.exception(ex, getCmd(args), null);
            return ExitCode.INVALID_OPTION_VALUE;
        } catch(ParseException ex) {
            LoggerShell.exception(ex, getCmd(args), null);
            return ExitCode.UNRECOGNIZED_COMMAND;
        } catch (InvalidTokenException | TokenNotFoundException |
                 FileSystemException | ConfigurationException e) {
            AstraCliConsole.outputError(ExitCode.CONFIGURATION, e.getMessage());
            return ExitCode.CONFIGURATION;
        } catch (InvalidArgumentException dex) {
           AstraCliConsole.outputError(ExitCode.INVALID_ARGUMENT, dex.getMessage());
           return  ExitCode.INVALID_ARGUMENT;
        } catch (DatabaseNotFoundException  |
                TenantNotFoundException    | 
                RoleNotFoundException      |
                UserNotFoundException ex) {
           AstraCliConsole.outputError(ExitCode.NOT_FOUND, ex.getMessage());
           return ExitCode.NOT_FOUND;
       } catch (DatabaseNameNotUniqueException | KeyspaceAlreadyExistException |
                TenantAlreadyExistException | UserAlreadyExistException e) {
           AstraCliConsole.outputError(ExitCode.ALREADY_EXIST, e.getMessage());
           return ExitCode.ALREADY_EXIST;
       } catch(InvalidDatabaseStateException ex) {
            AstraCliConsole.outputError(ExitCode.UNAVAILABLE, ex.getMessage());
            return ExitCode.UNAVAILABLE;
        } catch (Exception ex) {
           AstraCliConsole.outputError(ExitCode.INTERNAL_ERROR, ex.getMessage());
           return ExitCode.INTERNAL_ERROR;
       }
    }
    
    /**
     * Extract commands without options from command line.
     * 
     * @param args
     *      arguments
     * @return
     *      first part of command line
     */
    public static String getCmd(String[] args) {
        List <String > listArgs = Arrays.asList(args);
        boolean firstOption = false;
        int idx = 0;
        while (!firstOption && idx < listArgs.size()) {
            firstOption = listArgs.get(idx).startsWith("-");
            idx++;
        }
        if (firstOption) {
            idx--;
        }
        return String.join(" ", listArgs.subList(0, idx));
    }
    
    /**
     * Extract commands without options from command line.
     * 
     * @param args
     *      arguments
     * @return
     *      first part of command line
     */
    public static String getInvalidCmd(String[] args) {
        List <String > listArgs = Arrays.asList(args);
        boolean firstOption = false;
        int idx = 0;
        while (!firstOption && idx < listArgs.size()) {
            firstOption = listArgs.get(idx).startsWith("-");
            idx++;
        }
        if (firstOption) {
            idx--;
        }
        return String.join(" ", listArgs.subList(0, idx-1));
    }
    
}