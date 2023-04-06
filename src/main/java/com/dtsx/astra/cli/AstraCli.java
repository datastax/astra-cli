package com.dtsx.astra.cli;

/*-
 * #%L
 * Astra CLI
 * --
 * Copyright (C) 2022 - 2023 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.dtsx.astra.cli.config.*;
import com.dtsx.astra.cli.core.AbstractCmd;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.DefaultCmd;
import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.core.exception.*;
import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.cli.core.out.LoggerShell;
import com.dtsx.astra.cli.db.*;
import com.dtsx.astra.cli.db.cdc.DbCreateCdcCmd;
import com.dtsx.astra.cli.db.cdc.DbDeleteCdcCmd;
import com.dtsx.astra.cli.db.cdc.DbListCdcCmd;
import com.dtsx.astra.cli.db.cqlsh.DbCqlShellCmd;
import com.dtsx.astra.cli.db.dsbulk.DbCountCmd;
import com.dtsx.astra.cli.db.dsbulk.DbLoadCmd;
import com.dtsx.astra.cli.db.dsbulk.DbUnLoadCmd;
import com.dtsx.astra.cli.db.exception.*;
import com.dtsx.astra.cli.db.keyspace.DbCreateKeyspaceCmd;
import com.dtsx.astra.cli.db.keyspace.DbDeleteKeyspaceCmd;
import com.dtsx.astra.cli.db.keyspace.DbListKeyspacesCmd;
import com.dtsx.astra.cli.db.list.DbListCloudsCmd;
import com.dtsx.astra.cli.db.list.DbListCmd;
import com.dtsx.astra.cli.db.list.DbListRegionsClassicCmd;
import com.dtsx.astra.cli.db.list.DbListRegionsServerlessCmd;
import com.dtsx.astra.cli.db.region.DbCreateRegionCmd;
import com.dtsx.astra.cli.db.region.DbDeleteRegionCmd;
import com.dtsx.astra.cli.db.region.DbListRegionsCmd;
import com.dtsx.astra.cli.db.tool.DbGraphqlPlaygroundCmd;
import com.dtsx.astra.cli.db.tool.DbSwaggerUICmd;
import com.dtsx.astra.cli.iam.role.RoleDescribeCmd;
import com.dtsx.astra.cli.iam.role.RoleGetCmd;
import com.dtsx.astra.cli.iam.role.RoleListCmd;
import com.dtsx.astra.cli.iam.role.exception.RoleNotFoundException;
import com.dtsx.astra.cli.iam.token.*;
import com.dtsx.astra.cli.iam.user.*;
import com.dtsx.astra.cli.iam.user.exception.UserAlreadyExistException;
import com.dtsx.astra.cli.iam.user.exception.UserNotFoundException;
import com.dtsx.astra.cli.org.OrgCmd;
import com.dtsx.astra.cli.org.OrgIdCmd;
import com.dtsx.astra.cli.org.OrgNameCmd;
import com.dtsx.astra.cli.streaming.*;
import com.dtsx.astra.cli.streaming.cdc.StreamingListCdcCmd;
import com.dtsx.astra.cli.streaming.pulsarshell.PulsarShellCmd;
import com.dtsx.astra.cli.utils.AstraCliUtils;
import com.dtsx.astra.sdk.db.exception.*;
import com.dtsx.astra.sdk.streaming.exception.TenantAlreadyExistException;
import com.dtsx.astra.sdk.streaming.exception.TenantNotFoundException;
import com.github.rvesse.airline.Cli;
import com.github.rvesse.airline.annotations.Group;
import com.github.rvesse.airline.help.Help;
import com.github.rvesse.airline.parser.errors.*;
import org.fusesource.jansi.AnsiConsole;

import java.util.Arrays;

/**
 * Main class for the program. Will route commands to proper class 
 */
@com.github.rvesse.airline.annotations.Cli(
  name = "astra", 
  description = "CLI for DataStax Astra™ ",
  defaultCommand = DefaultCmd.class, 
  commands = { 
    SetupCmd.class, Help.class, DefaultCmd.class
  },
  groups = {
    
    @Group(
       name = "config", 
       description = "Manage configuration file", 
       defaultCommand = ConfigListCmd.class,
       commands = {
         ConfigCreateCmd.class, ConfigGetCmd.class, ConfigDeleteCmd.class,
         ConfigListCmd.class, ConfigUseCmd.class, ConfigDescribeCmd.class
    }),
   
    @Group(name = "org", 
      description = "Display Organization Info", 
      defaultCommand = OrgCmd.class,
      commands = {
        OrgIdCmd.class,
        OrgNameCmd.class
    }),
    
    @Group(
       name = "db", 
       description = "Manage databases",
       defaultCommand = DbListCmd.class,
       commands = { 
         // Create,delete
         DbCreateCmd.class,  DbDeleteCmd.class,
         // Read
         DbListCmd.class,  DbGetCmd.class, DbDescribeCmd.class, DbStatusCmd.class,
         // Operation
         DbResumeCmd.class, DbDownloadScbCmd.class, DbCreateDotEnvCmd.class,
         // Keyspaces
         DbCreateKeyspaceCmd.class, DbDeleteKeyspaceCmd.class, DbListKeyspacesCmd.class,
         // Regions
         DbCreateRegionCmd.class, DbListRegionsCmd.class, DbDeleteRegionCmd.class,
         // DB Service Regions and Cloud
         DbListRegionsClassicCmd.class, DbListRegionsServerlessCmd.class, DbListCloudsCmd.class,
         // DsBulk
         DbCountCmd.class, DbLoadCmd.class, DbUnLoadCmd.class,
         // Cqlshell
         DbCqlShellCmd.class,
         // External Tools
         DbSwaggerUICmd.class, DbGraphqlPlaygroundCmd.class,
         // Cdc
         DbListCdcCmd.class, DbDeleteCdcCmd.class, DbCreateCdcCmd.class
     }),
    
    @Group(
       name = "streaming", 
       description = "Manage Streaming tenants", 
       defaultCommand = StreamingListCmd.class,
       commands = { 
         // Create, Delete
         StreamingCreateCmd.class, StreamingDeleteCmd.class,
         // Read
         StreamingListCmd.class, StreamingGetCmd.class, StreamingDescribeCmd.class,
         StreamingExistCmd.class, StreamingStatusCmd.class,
         StreamingPulsarTokenCmd.class, StreamingCreateDotEnvCmd.class,
         // list clouds and Regions
         StreamingListRegionsCmd.class, StreamingListCloudsCmd.class,
         // Pulsar Shell
         PulsarShellCmd.class,
         // Change Data Capture
         StreamingListCdcCmd.class
         // StreamingCreateCdcCmd.class, StreamingDeleteCdcCmd.class, StreamingGetCdcCmd.class
    }),
    
    @Group(
       name= "role", 
       description = "Manage roles", 
       defaultCommand = RoleListCmd.class,
       commands = {
         RoleListCmd.class, RoleGetCmd.class, RoleDescribeCmd.class
    }),
    
    @Group(
       name= "user", 
       description = "Manage users", 
       defaultCommand = UserListCmd.class,
       commands = {
         UserGetCmd.class, UserInviteCmd.class, UserDeleteCmd.class,
         UserListCmd.class, UserDescribeCmd.class
    }),

    @Group(
       name= "token",
       description = "Manage tokens",
       defaultCommand = TokenGetCmd.class,
       commands = {
         TokenListCmd.class, TokenGetCmd.class, TokenCreateCmd.class, TokenDeleteCmd.class, TokenRevokeCmd.class
     })
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

        // Create ~/.astra and required sub folders
        AstraCliUtils.createHomeAstraFolders();

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
            LoggerShell.exception(ex,
                    "You provided unknown or not well formatted argument.");
            return ExitCode.INVALID_ARGUMENT;
        } catch(ParseOptionIllegalValueException | ParseOptionMissingException ex) {
            LoggerShell.exception(ex,
                    "You provided unknown or not well formatted option. (-option)");
            return ExitCode.INVALID_OPTION;
        } catch(ParseRestrictionViolatedException | ParseOptionConversionException ex) {
            LoggerShell.exception(ex,
                    "You provided an invalid value for option. (-option)");
            return ExitCode.INVALID_OPTION_VALUE;
        } catch(InvalidRegionException regionException) {
            LoggerShell.exception(regionException,null);
            LoggerShell.info("Run " +
                    "astra db list-regions-serverless or " +
                    "astra streaming list-regions to list available regions.");
            return ExitCode.INVALID_OPTION_VALUE;
        } catch(InvalidCloudProviderException cloudException) {
            LoggerShell.exception(cloudException,null);
            LoggerShell.info("Use " +
                    "astra db list-clouds or " +
                    "astra streaming list-clouds to list available cloud providers.");
            return ExitCode.INVALID_OPTION_VALUE;
        } catch(ParseException ex) {
            LoggerShell.exception(ex,"Command is not properly formatted.");
            return ExitCode.UNRECOGNIZED_COMMAND;
        } catch (InvalidTokenException | TokenNotFoundException |
                FileSystemException | ConfigurationException e) {
            AstraCliConsole.outputError(ExitCode.CONFIGURATION, e.getMessage());
            return ExitCode.CONFIGURATION;
        } catch (InvalidArgumentException | IllegalArgumentException dex) {
           AstraCliConsole.outputError(ExitCode.INVALID_ARGUMENT, dex.getMessage());
           return  ExitCode.INVALID_ARGUMENT;
        } catch (DatabaseNotFoundException |
                TenantNotFoundException |
                RoleNotFoundException | ChangeDataCaptureNotFoundException |
                UserNotFoundException | RegionNotFoundException ex) {
            AstraCliConsole.outputError(ExitCode.NOT_FOUND, ex.getMessage());
            return ExitCode.NOT_FOUND;
       } catch (DatabaseNameNotUniqueException name) {
            AstraCliConsole.outputError(ExitCode.CONFLICT, name.getMessage());
            return ExitCode.CONFLICT;
       } catch (DatabaseAlreadyExistException | KeyspaceAlreadyExistException |
                TenantAlreadyExistException | UserAlreadyExistException |
                RegionAlreadyExistException e) {
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
    
}
