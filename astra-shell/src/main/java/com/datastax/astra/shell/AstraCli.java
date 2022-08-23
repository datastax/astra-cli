package com.datastax.astra.shell;

import java.io.File;

import org.fusesource.jansi.AnsiConsole;

import com.datastax.astra.shell.cmd.HelpCommand;
import com.datastax.astra.shell.cmd.config.BaseConfigCommand;
import com.datastax.astra.shell.cmd.config.ConfigCreate;
import com.datastax.astra.shell.cmd.config.ConfigUse;
import com.datastax.astra.shell.cmd.config.ConfigDelete;
import com.datastax.astra.shell.cmd.config.ConfigGet;
import com.datastax.astra.shell.cmd.config.ConfigList;
import com.datastax.astra.shell.cmd.config.ConfigSetup;
import com.datastax.astra.shell.cmd.db.DbCqlShellCli;
import com.datastax.astra.shell.cmd.db.DbCreateCli;
import com.datastax.astra.shell.cmd.db.DbCreateKeyspaceCli;
import com.datastax.astra.shell.cmd.db.DbDSBulkCli;
import com.datastax.astra.shell.cmd.db.DbDeleteCli;
import com.datastax.astra.shell.cmd.db.DbDownloadScbCli;
import com.datastax.astra.shell.cmd.db.DbGetCli;
import com.datastax.astra.shell.cmd.db.DbListCli;
import com.datastax.astra.shell.cmd.db.DbResumeCli;
import com.datastax.astra.shell.cmd.db.OperationsDb;
import com.datastax.astra.shell.cmd.iam.OperationIam;
import com.datastax.astra.shell.cmd.iam.RoleGetCli;
import com.datastax.astra.shell.cmd.iam.RoleListCli;
import com.datastax.astra.shell.cmd.iam.UserDeleteCli;
import com.datastax.astra.shell.cmd.iam.UserGetCli;
import com.datastax.astra.shell.cmd.iam.UserInviteCli;
import com.datastax.astra.shell.cmd.iam.UserListCli;
import com.datastax.astra.shell.cmd.shell.ShellCommand;
import com.datastax.astra.shell.out.LoggerShell;
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
  defaultCommand = ShellCommand.class, // no command => interactive
  commands       = { 
    ConfigSetup.class,
    HelpCommand.class,
    ShellCommand.class
  },
  groups = {
          @Group(name = OperationsDb.DB, description = "Manage databases", commands = { 
                  DbCreateCli.class,
                  DbCqlShellCli.class,
                  DbDeleteCli.class,
                  DbGetCli.class,
                  DbListCli.class,
                  DbCreateKeyspaceCli.class,
                  DbDSBulkCli.class,
                  DbResumeCli.class,
                  DbDownloadScbCli.class
          }),
          @Group(name = BaseConfigCommand.COMMAND_CONFIG, description = "Manage configuration file", commands = { 
                  ConfigCreate.class,
                  ConfigUse.class,
                  ConfigDelete.class,
                  ConfigGet.class,
                  ConfigList.class
          }),
          @Group(name= OperationIam.COMMAND_ROLE, description = "Manage roles (RBAC)", commands = {
                  RoleListCli.class,
                  RoleGetCli.class
          }),
          /*
          @Group(name= OperationsStreaming.STREAMING, description = "Manage Astra Streaming", commands = {
                  PulsarClientCli.class,
                  PulsarAdminCli.class,
                  PulsarPerfCli.class
          }),
          */
          @Group(name= OperationIam.COMMAND_USER, description = "Manage users", commands = {
                  UserListCli.class,
                  UserGetCli.class,
                  UserInviteCli.class,
                  UserDeleteCli.class
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
            
        } catch(Exception e) {
            LoggerShell.error("Invalid options or error execution:\n - try 'astra help' to get general help\n - "
                    + "try 'astra help <cmd>' to get help on a particular command "
                    + "(eg: astra help db create)\n - [TAB][TAB] help you with autocompletion.");
            LoggerShell.error("\nError Message:" + e.getMessage());
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