package com.datastax.astra.shell;

import com.datastax.astra.shell.cmd.ExitCommand;
import com.datastax.astra.shell.cmd.HelpCommand;
import com.datastax.astra.shell.cmd.QuitCommand;
import com.datastax.astra.shell.cmd.db.DbCqlShellShell;
import com.datastax.astra.shell.cmd.db.DbCreateShell;
import com.datastax.astra.shell.cmd.db.DbDeleteShell;
import com.datastax.astra.shell.cmd.db.DbGetShell;
import com.datastax.astra.shell.cmd.db.DbInfoShell;
import com.datastax.astra.shell.cmd.db.DbListShell;
import com.datastax.astra.shell.cmd.db.DbUseShell;
import com.datastax.astra.shell.cmd.db.OperationsDb;
import com.datastax.astra.shell.cmd.db.keyspace.DbCreateKeyspaceShell;
import com.datastax.astra.shell.cmd.iam.OperationIam;
import com.datastax.astra.shell.cmd.iam.RoleGetCli;
import com.datastax.astra.shell.cmd.iam.RoleListShell;
import com.datastax.astra.shell.cmd.iam.UserDeleteShell;
import com.datastax.astra.shell.cmd.iam.UserGetShell;
import com.datastax.astra.shell.cmd.iam.UserInviteShell;
import com.datastax.astra.shell.cmd.iam.UserListShell;
import com.datastax.astra.shell.cmd.shell.ConnectCommand;
import com.datastax.astra.shell.cmd.shell.EmptyCommand;
import com.datastax.astra.shell.out.LoggerShell;
import com.github.rvesse.airline.annotations.Cli;
import com.github.rvesse.airline.annotations.Group;
import com.github.rvesse.airline.parser.errors.ParseArgumentsUnexpectedException;

/**
 * Shell in an interactive CLI.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Cli(
  name        = "shell", 
  description = "Interactive Shell for DataStax Astra",
  defaultCommand = 
    EmptyCommand.class, 
  commands       = { 
    ConnectCommand.class,
    EmptyCommand.class,
    HelpCommand.class,
    ExitCommand.class,
    
    // With selected db
    DbCreateKeyspaceShell.class,
    DbInfoShell.class,
    DbCqlShellShell.class,
    QuitCommand.class,
},
  groups = {
          @Group(name = OperationsDb.DB, description = "Commands acting of database", commands = {
                  DbCreateShell.class, 
                  DbDeleteShell.class, 
                  DbListShell.class,
                  DbGetShell.class, 
                  DbInfoShell.class, 
                  DbUseShell.class
          }),
          @Group(name = OperationsDb.CMD_KEYSPACE, description = "Manage keyspaces (db must be selected)", commands = {
                  DbCreateKeyspaceShell.class,
          }),
          @Group(name= OperationIam.COMMAND_ROLE, description = "Manage roles (RBAC)", commands = {
                  RoleListShell.class,
                  RoleGetCli.class
          }),
          @Group(name= OperationIam.COMMAND_USER, description = "Manage users permission", commands = {
                  UserListShell.class, 
                  UserGetShell.class, 
                  UserInviteShell.class, 
                  UserDeleteShell.class
          })
    })
public class AstraShell {
    
    /**
     * Main program for the interactive Shell.
     * 
     * @param args
     *      cli arguments
     */
    public static void main(String[] args) {
        try {

            new com.github.rvesse.airline.Cli<Runnable>(AstraShell.class)
               .parse(args)  // Find the processor for the command 
               .run();       // Run the command
            
        } catch(ParseArgumentsUnexpectedException ex) {
            LoggerShell.error("Invalid command: " + ex.getMessage());
        } catch(Exception e) {
            LoggerShell.error("Execution error:" + e.getMessage());
        }
    }

}
