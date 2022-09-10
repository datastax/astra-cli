package com.datastax.astra.cli;

import java.io.File;

import org.fusesource.jansi.AnsiConsole;

import com.datastax.astra.cli.cmd.HelpCmd;
import com.datastax.astra.cli.cmd.UpdateCmd;
import com.datastax.astra.cli.cmd.config.BaseConfigCommand;
import com.datastax.astra.cli.cmd.config.ConfigCreateCmd;
import com.datastax.astra.cli.cmd.config.ConfigDeleteCmd;
import com.datastax.astra.cli.cmd.config.ConfigGetCmd;
import com.datastax.astra.cli.cmd.config.ConfigListCmd;
import com.datastax.astra.cli.cmd.config.ConfigSetupCmd;
import com.datastax.astra.cli.cmd.config.ConfigUseCmd;
import com.datastax.astra.cli.cmd.db.DbCreateCmd;
import com.datastax.astra.cli.cmd.db.DbDeleteCmd;
import com.datastax.astra.cli.cmd.db.DbDownloadScbCmd;
import com.datastax.astra.cli.cmd.db.DbGetCmd;
import com.datastax.astra.cli.cmd.db.DbListCmd;
import com.datastax.astra.cli.cmd.db.DbResumeCmd;
import com.datastax.astra.cli.cmd.db.DbStatusCmd;
import com.datastax.astra.cli.cmd.db.OperationsDb;
import com.datastax.astra.cli.cmd.db.cqlsh.DbCqlShellCmd;
import com.datastax.astra.cli.cmd.db.dsbulk.DbDSBulkCmd;
import com.datastax.astra.cli.cmd.db.keyspace.DbCreateKeyspaceCmd;
import com.datastax.astra.cli.cmd.db.keyspace.DbListKeyspacesCmd;
import com.datastax.astra.cli.cmd.iam.OperationIam;
import com.datastax.astra.cli.cmd.iam.RoleGetCmd;
import com.datastax.astra.cli.cmd.iam.RoleListCmd;
import com.datastax.astra.cli.cmd.iam.UserDeleteCmd;
import com.datastax.astra.cli.cmd.iam.UserGetCmd;
import com.datastax.astra.cli.cmd.iam.UserInviteCmd;
import com.datastax.astra.cli.cmd.iam.UserListCmd;
import com.datastax.astra.cli.cmd.shell.ShellCmd;
import com.datastax.astra.cli.cmd.streaming.OperationsStreaming;
import com.datastax.astra.cli.cmd.streaming.StreamingCreateCmd;
import com.datastax.astra.cli.cmd.streaming.pulsarshell.PulsarShellCmd;
import com.datastax.astra.cli.out.LoggerShell;
import com.github.rvesse.airline.annotations.Cli;
import com.github.rvesse.airline.annotations.Group;
import com.github.rvesse.airline.parser.errors.ParseArgumentsUnexpectedException;

/**
 * Main class for the program. Will route commands to proper class 
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Cli(
  name = "astra", 
  description    = "CLI for DataStax Astra™ including an interactive mode",
  defaultCommand = ShellCmd.class, // no command => interactive
  commands       = { 
    ConfigSetupCmd.class,
    HelpCmd.class,
    ShellCmd.class,
    UpdateCmd.class
  },
  groups = {
          @Group(name = OperationsDb.DB, description = "Manage databases", commands = { 
                  // CRUD
                  DbCreateCmd.class, DbGetCmd.class, DbDeleteCmd.class,
                  // Infos
                  DbListCmd.class, DbStatusCmd.class,
                  // Operations
                  DbResumeCmd.class, DbDownloadScbCmd.class,
                  // External Tools
                  DbCqlShellCmd.class, DbDSBulkCmd.class,
                  // Keyspaces
                  DbCreateKeyspaceCmd.class, DbListKeyspacesCmd.class
          }),
          @Group(name = OperationsStreaming.STREAMING, description = "Manage Streaming tenants", commands = { 
                  StreamingCreateCmd.class,
                  PulsarShellCmd.class
          }),
          @Group(name = BaseConfigCommand.COMMAND_CONFIG, description = "Manage configuration file", commands = { 
                  ConfigCreateCmd.class,
                  ConfigUseCmd.class,
                  ConfigDeleteCmd.class,
                  ConfigGetCmd.class,
                  ConfigListCmd.class
          }),
          @Group(name= OperationIam.COMMAND_ROLE, description = "Manage roles (RBAC)", commands = {
                  RoleListCmd.class,
                  RoleGetCmd.class
          }),
          /*
          @Group(name= OperationsStreaming.STREAMING, description = "Manage Astra Streaming", commands = {
                  PulsarClientCli.class,
                  PulsarAdminCli.class,
                  PulsarPerfCli.class
          }),
          */
          @Group(name= OperationIam.COMMAND_USER, description = "Manage users", commands = {
                  UserListCmd.class,
                  UserGetCmd.class,
                  UserInviteCmd.class,
                  UserDeleteCmd.class
          }),
          /*
          @Group(name= "token", description = "Manage security tokens", commands = {
          }),
          @Group(name= "acl", description = "Manage Access lists", commands = {
          })*/
  })
public class AstraCli {
    
    /** Environment variable coding user home. */
    public static final String ENV_USER_HOME = "user.home";
    
    /** Path to save third-parties. */
    public static final String ASTRA_HOME = System.getProperty(ENV_USER_HOME) + File.separator + ".astra";
    
    /** Folder name where to download SCB. */
    public static final String SCB_FOLDER = "scb";
    
    /** Folder name to download archives */
    public static final String TMP_FOLDER = "tmp";
    
    /**
     * Main Program.
     *
     * @param args
     *           start options for the shell
     */
    public static void main(String[] args) {
        
        try {
            
            // Enable Colored outputs
            AnsiConsole.systemInstall();
            
            // Save the command line to log it later
            ShellContext.getInstance().setRawCommand(args);
            
            // Command Line Interface
            new com.github.rvesse.airline.Cli<Runnable>(AstraCli.class)
                .parse(args)  // Find the processor for the command 
                .run();       // Run the command
            
        } catch(ParseArgumentsUnexpectedException ex) {
            LoggerShell.error("Invalid command\n - try 'astra help' to get general help\n - "
                    + "try 'astra help <cmd>' to get help on a particular command "
                    + "(eg: astra help db create)\n - [TAB][TAB] help you with autocompletion." );
            ex.printStackTrace();
        } catch(Exception e) {
            LoggerShell.error("Invalid options or error execution:\n - try 'astra help' to get general help\n - "
                    + "try 'astra help <cmd>' to get help on a particular command "
                    + "(eg: astra help db create)\n - [TAB][TAB] help you with autocompletion.");
            LoggerShell.error("\nError Message:" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Run the Program with varArgs.
     * 
     * @param args
     *      arguments
     */
    public static void exec(String ...args) {
        main(args);
    }
    
    
}