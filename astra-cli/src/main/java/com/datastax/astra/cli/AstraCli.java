package com.datastax.astra.cli;

import java.util.Arrays;
import java.util.List;

import org.fusesource.jansi.AnsiConsole;

import com.datastax.astra.cli.config.ConfigCreateCmd;
import com.datastax.astra.cli.config.ConfigDeleteCmd;
import com.datastax.astra.cli.config.ConfigGetCmd;
import com.datastax.astra.cli.config.ConfigListCmd;
import com.datastax.astra.cli.config.ConfigSetupCmd;
import com.datastax.astra.cli.config.ConfigUseCmd;
import com.datastax.astra.cli.config.OperationsConfig;
import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.core.HelpCmd;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.cli.core.shell.ShellCmd;
import com.datastax.astra.cli.db.DbCreateCmd;
import com.datastax.astra.cli.db.DbDeleteCmd;
import com.datastax.astra.cli.db.DbDotEnvCmd;
import com.datastax.astra.cli.db.DbDownloadScbCmd;
import com.datastax.astra.cli.db.DbGetCmd;
import com.datastax.astra.cli.db.DbListCmd;
import com.datastax.astra.cli.db.DbResumeCmd;
import com.datastax.astra.cli.db.DbStatusCmd;
import com.datastax.astra.cli.db.OperationsDb;
import com.datastax.astra.cli.db.cqlsh.DbCqlShellCmd;
import com.datastax.astra.cli.db.dsbulk.DbDSBulkCmd;
import com.datastax.astra.cli.db.keyspace.DbCreateKeyspaceCmd;
import com.datastax.astra.cli.db.keyspace.DbListKeyspacesCmd;
import com.datastax.astra.cli.iam.OperationIam;
import com.datastax.astra.cli.iam.RoleGetCmd;
import com.datastax.astra.cli.iam.RoleListCmd;
import com.datastax.astra.cli.iam.UserDeleteCmd;
import com.datastax.astra.cli.iam.UserGetCmd;
import com.datastax.astra.cli.iam.UserInviteCmd;
import com.datastax.astra.cli.iam.UserListCmd;
import com.datastax.astra.cli.org.OrgCmd;
import com.datastax.astra.cli.org.OrgIdCmd;
import com.datastax.astra.cli.org.OrgListRegionsClassicCmd;
import com.datastax.astra.cli.org.OrgListRegionsServerlessCmd;
import com.datastax.astra.cli.org.OrgNameCmd;
import com.datastax.astra.cli.streaming.OperationsStreaming;
import com.datastax.astra.cli.streaming.StreamingCreateCmd;
import com.datastax.astra.cli.streaming.StreamingDeleteCmd;
import com.datastax.astra.cli.streaming.StreamingExistCmd;
import com.datastax.astra.cli.streaming.StreamingGetCmd;
import com.datastax.astra.cli.streaming.StreamingListCmd;
import com.datastax.astra.cli.streaming.StreamingPulsarTokenCmd;
import com.datastax.astra.cli.streaming.StreamingStatusCmd;
import com.datastax.astra.cli.streaming.pulsarshell.PulsarShellCmd;
import com.github.rvesse.airline.annotations.Cli;
import com.github.rvesse.airline.annotations.Group;
import com.github.rvesse.airline.parser.errors.ParseArgumentsMissingException;
import com.github.rvesse.airline.parser.errors.ParseArgumentsUnexpectedException;
import com.github.rvesse.airline.parser.errors.ParseCommandMissingException;
import com.github.rvesse.airline.parser.errors.ParseCommandUnrecognizedException;
import com.github.rvesse.airline.parser.errors.ParseException;
import com.github.rvesse.airline.parser.errors.ParseOptionGroupException;
import com.github.rvesse.airline.parser.errors.ParseOptionIllegalValueException;
import com.github.rvesse.airline.parser.errors.ParseOptionMissingException;
import com.github.rvesse.airline.parser.errors.ParseOptionMissingValueException;
import com.github.rvesse.airline.parser.errors.ParseOptionOutOfRangeException;
import com.github.rvesse.airline.parser.errors.ParseRestrictionViolatedException;
import com.github.rvesse.airline.parser.errors.ParseTooManyArgumentsException;

/**
 * Main class for the program. Will route commands to proper class 
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Cli(
  name = "astra", 
  description = "CLI for DataStax Astra™ including an interactive mode",
  defaultCommand = ShellCmd.class, // no command => interactive
  commands = { 
    ConfigSetupCmd.class, HelpCmd.class, 
    ShellCmd.class
  },
  groups = {
    
    /* ------------------------------
     * astra config ...
     * ------------------------------
     */
    @Group(
       name = OperationsConfig.COMMAND_CONFIG, 
       description = "Manage configuration file", 
       defaultCommand = ConfigListCmd.class, 
       commands = {
         ConfigCreateCmd.class, ConfigGetCmd.class, ConfigDeleteCmd.class,
         ConfigUseCmd.class, ConfigListCmd.class
    }),
   
    /* ------------------------------
     * astra org ...
     * ------------------------------
     */
    @Group(name = AbstractCmd.ORG, 
      defaultCommand = OrgCmd.class,  
      description = "Display Organization Info", 
      commands = {
        OrgIdCmd.class, 
        OrgNameCmd.class,
        OrgListRegionsClassicCmd.class, 
        OrgListRegionsServerlessCmd.class
    }),
    
    /* ------------------------------
     * astra db ...
     * ------------------------------
     */
    @Group(
       name = OperationsDb.DB, 
       description = "Manage databases",
       defaultCommand = DbGetCmd.class, 
       commands = { 
         DbCreateCmd.class, DbGetCmd.class, DbDeleteCmd.class,
         DbListCmd.class, DbStatusCmd.class,
         DbResumeCmd.class, DbDownloadScbCmd.class, DbDotEnvCmd.class,
         DbCqlShellCmd.class, DbDSBulkCmd.class,
         DbCreateKeyspaceCmd.class, DbListKeyspacesCmd.class
     }),
    
    /* ------------------------------
     * astra streaming ...
     * ------------------------------
     */
    @Group(
       name = OperationsStreaming.STREAMING, 
       description = "Manage Streaming tenants", 
       defaultCommand = StreamingListCmd.class, 
       commands = { 
         StreamingCreateCmd.class, StreamingGetCmd.class, StreamingDeleteCmd.class,
         StreamingExistCmd.class, StreamingListCmd.class, 
         StreamingStatusCmd.class, StreamingPulsarTokenCmd.class,
         PulsarShellCmd.class
    }),
    
    /* ------------------------------
     * astra role ...
     * ------------------------------
     */
    @Group(
       name= OperationIam.COMMAND_ROLE, 
       description = "Manage roles", 
       defaultCommand = RoleListCmd.class, 
       commands = {
         RoleListCmd.class, RoleGetCmd.class
    }),
    
    /* ------------------------------
     * astra user ...
     * ------------------------------
     */
    @Group(
       name= OperationIam.COMMAND_USER, 
       description = "Manage users", 
       defaultCommand = UserListCmd.class, 
       commands = {
         UserGetCmd.class, UserInviteCmd.class, UserDeleteCmd.class,
         UserListCmd.class
    }),
    
          /*
          @Group(name= "token", description = "Manage security tokens", commands = {
          }),
          @Group(name= "acl", description = "Manage Access lists", commands = {
          })*/
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
        
        // Persist command line to log it later
        ShellContext.getInstance().setRawCommand(args);
        
        // Parse and execute
        ExitCode code = runCli(AstraCli.class, args);
        
        // Exit with proper to code
        System.exit(code.getCode());
    }
   
    /**
     * Run CLI and process exceptions.
     *
     * @param clazz
     *      current class.
     * @param args
     *      exception management
     */
    public static ExitCode runCli(Class<?> clazz, String[] args) {
        try {

            new com.github.rvesse.airline.Cli<Runnable>(clazz)
               .parse(args)  // Find the processor for the command 
               .run();       // Run the command
            
            return ExitCode.SUCCESS;
            
        } catch(ParseArgumentsMissingException ex) {
            LoggerShell.exception(ex, getCmd(args), null);
            return ExitCode.INVALID_ARGUMENT;
            //todo
        } catch(ParseArgumentsUnexpectedException ex) {
            LoggerShell.exception(ex, getInvalidCmd(args), null);
            return ExitCode.INVALID_ARGUMENT;
        } catch(ParseCommandMissingException ex) {
            LoggerShell.exception(ex, getCmd(args), null);
            return ExitCode.UNRECOGNIZED_COMMAND;
        } catch(ParseCommandUnrecognizedException ex) {
            LoggerShell.exception(ex, getInvalidCmd(args), null);
            return ExitCode.UNRECOGNIZED_COMMAND;
        } catch(ParseTooManyArgumentsException ex) {
            LoggerShell.exception(ex, getInvalidCmd(args), null);
            return ExitCode.INVALID_ARGUMENT;
        } catch(ParseOptionGroupException ex) {
            LoggerShell.exception(ex, getCmd(args), null);
            return ExitCode.INVALID_ARGUMENT;
        } catch(ParseOptionIllegalValueException ex) {
            LoggerShell.exception(ex, getCmd(args), null);
            return ExitCode.INVALID_OPTION;
        } catch(ParseOptionMissingException ex) {
            LoggerShell.exception(ex, getCmd(args), null);
            return ExitCode.INVALID_OPTION;
        } catch(ParseOptionMissingValueException ex) {
            LoggerShell.exception(ex, getCmd(args), null);
            return ExitCode.INVALID_OPTION_VALUE;
        } catch(ParseOptionOutOfRangeException ex) {
            LoggerShell.exception(ex, getCmd(args), null);
            return ExitCode.INVALID_OPTION_VALUE;
        } catch(ParseRestrictionViolatedException ex) {
            LoggerShell.exception(ex, getCmd(args), null);
            return ExitCode.INVALID_OPTION_VALUE;
        } catch(ParseException ex) {
            LoggerShell.exception(ex, getCmd(args), null);
            return ExitCode.UNRECOGNIZED_COMMAND;
        } catch(Exception ex) {
            LoggerShell.exception(ex, getCmd(args), null);
            return ExitCode.INTERNAL_ERROR;
        }
    }
    
    /**
     * Extract commands without options from command line.
     * 
     * @param args
     *      arguments
     * @return
     *      first part of commadn line
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
     *      first part of commadn line
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