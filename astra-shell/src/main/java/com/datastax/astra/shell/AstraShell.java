package com.datastax.astra.shell;

import com.datastax.astra.shell.cmd.ExitCmd;
import com.datastax.astra.shell.cmd.HelpCmd;
import com.datastax.astra.shell.cmd.QuitCmd;
import com.datastax.astra.shell.cmd.db.DbCqlShellSh;
import com.datastax.astra.shell.cmd.db.DbCreateSh;
import com.datastax.astra.shell.cmd.db.DbDeleteSh;
import com.datastax.astra.shell.cmd.db.DbGetSh;
import com.datastax.astra.shell.cmd.db.DbInfoSh;
import com.datastax.astra.shell.cmd.db.DbListSh;
import com.datastax.astra.shell.cmd.db.DbUseSh;
import com.datastax.astra.shell.cmd.db.OperationsDb;
import com.datastax.astra.shell.cmd.db.keyspace.DbCreateKeyspaceSh;
import com.datastax.astra.shell.cmd.db.keyspace.DbListKeyspacesSh;
import com.datastax.astra.shell.cmd.iam.OperationIam;
import com.datastax.astra.shell.cmd.iam.RoleGetCmd;
import com.datastax.astra.shell.cmd.iam.RoleListSh;
import com.datastax.astra.shell.cmd.iam.UserDeleteSh;
import com.datastax.astra.shell.cmd.iam.UserGetSh;
import com.datastax.astra.shell.cmd.iam.UserInviteSh;
import com.datastax.astra.shell.cmd.iam.UserListSh;
import com.datastax.astra.shell.cmd.shell.ConnectSh;
import com.datastax.astra.shell.cmd.shell.EmptySh;
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
    EmptySh.class, 
  commands       = { 
    ConnectSh.class,
    EmptySh.class,
    HelpCmd.class,
    ExitCmd.class,
    
    // With selected db
    DbCreateKeyspaceSh.class,
    DbInfoSh.class,
    DbCqlShellSh.class,
    QuitCmd.class,
},
  groups = {
          @Group(name = OperationsDb.DB, description = "Commands acting of database", commands = {
                  DbCreateSh.class, 
                  DbDeleteSh.class, 
                  DbListSh.class,
                  DbGetSh.class, 
                  DbInfoSh.class, 
                  DbUseSh.class,
                  // Keyspaces
                  DbCreateKeyspaceSh.class, DbListKeyspacesSh.class
          }),
          
          @Group(name= OperationIam.COMMAND_ROLE, description = "Manage roles (RBAC)", commands = {
                  RoleListSh.class,
                  RoleGetCmd.class
          }),
          @Group(name= OperationIam.COMMAND_USER, description = "Manage users permission", commands = {
                  UserListSh.class, 
                  UserGetSh.class, 
                  UserInviteSh.class, 
                  UserDeleteSh.class
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
