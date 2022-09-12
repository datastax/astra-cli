package com.datastax.astra.cli;

import java.io.File;

import org.fusesource.jansi.AnsiConsole;

import com.datastax.astra.cli.config.BaseConfigCommand;
import com.datastax.astra.cli.config.ConfigCreateCmd;
import com.datastax.astra.cli.config.ConfigDeleteCmd;
import com.datastax.astra.cli.config.ConfigGetCmd;
import com.datastax.astra.cli.config.ConfigListCmd;
import com.datastax.astra.cli.config.ConfigSetupCmd;
import com.datastax.astra.cli.config.ConfigUseCmd;
import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.core.HelpCmd;
import com.datastax.astra.cli.core.UpdateCmd;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.cli.core.shell.ShellCmd;
import com.datastax.astra.cli.db.DbCreateCmd;
import com.datastax.astra.cli.db.DbDeleteCmd;
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
import com.datastax.astra.cli.org.OrgGetCmd;
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
    UpdateCmd.class,
    OrgGetCmd.class
  },
  groups = {
          
          @Group(name = AbstractCmd.ORG, defaultCommand = OrgGetCmd.class,  description = "Display Organization Info", commands = { 
                 OrgIdCmd.class, 
                 OrgNameCmd.class,
                 OrgListRegionsClassicCmd.class, 
                 OrgListRegionsServerlessCmd.class
          }),
          
          @Group(name = BaseConfigCommand.COMMAND_CONFIG, description = "Manage configuration file", commands = { 
                  ConfigCreateCmd.class,
                  ConfigUseCmd.class,
                  ConfigDeleteCmd.class,
                  ConfigGetCmd.class,
                  ConfigListCmd.class
          }),
          
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
                  // CRUD
                  StreamingCreateCmd.class, StreamingGetCmd.class, StreamingDeleteCmd.class,
                  // Infos
                  StreamingExistCmd.class, StreamingListCmd.class, 
                  StreamingStatusCmd.class, StreamingPulsarTokenCmd.class,
                  // External Tools
                  PulsarShellCmd.class
          }),
          
          @Group(name= OperationIam.COMMAND_ROLE, description = "Manage roles (RBAC)", commands = {
                  RoleListCmd.class,
                  RoleGetCmd.class
          }),
         
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