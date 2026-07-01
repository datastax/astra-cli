package com.dtsx.astra.cli.snapshot.commands.dotenv;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForHumanOutput;
import com.dtsx.astra.cli.testlib.Fixtures;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContext;
import lombok.SneakyThrows;
import lombok.val;

import java.nio.file.Files;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class DotEnvPrintCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier mockOrg = o -> o
        .gateway(OrgGateway.class, mock -> {
            when(mock.current()).thenReturn(Fixtures.Organization);
        });

    private final SnapshotTestOptionsModifier mockDb = o -> o
        .gateway(DbGateway.class, mock -> {
            when(mock.findOne(any())).thenReturn(Fixtures.Databases.One);
        });

    @SneakyThrows
    private void createEnv(TestCliContext ctx, String content) {
        val target = ctx.get().path(".env");
        if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }
        Files.writeString(target, content);
    }

    @TestForHumanOutput
    public void no_args_when_no_tty_errors(OutputType outputType) {
        verifyRun("dotenv print", outputType, o -> o.useJimfs().use(mockOrg));
    }

    @TestForHumanOutput
    public void can_use_keys(OutputType outputType) {
        verifyRun("dotenv print -k ASTRA_ORG_ID=ORG_ID,ASTRA_ORG_NAME -k ASTRA_DB_ID --db ${DatabaseName}", outputType,
            o -> o.use(mockOrg).use(mockDb));
    }

    @TestForHumanOutput
    public void can_use_file(OutputType outputType) {
        verifyRun("dotenv print -f .env --db ${DatabaseName} --overwrite", outputType,
            o -> o.use(mockOrg).use(mockDb).useJimfs(),
            c -> createEnv(c, """
            # astra: ASTRA_ORG_ID (comment)
            ORG_ID=old_org_id
            
            # astra: ASTRA_DB_ID
            DB_NAME=old_db_id # astra: ASTRA_DB_NAME - comment
            
            # astra: ASTRA_DB_ID
            """));
    }

    @TestForHumanOutput
    public void can_use_file_without_auto_overwrite(OutputType outputType) {
        verifyRun("dotenv print -f .env --db ${DatabaseName}", outputType,
            o -> o.use(mockOrg).use(mockDb).useJimfs().stdin("y"),
            c -> createEnv(c, """
            # astra: ASTRA_ORG_ID # comment
            
            ORG_ID=old_org_id
            
            # astra: RANDOM_NONSENSE
            DB_NAME=two_regions_db # astra: ASTRA_DB_NAME comment
            
            UNKNOWN=3
            
            # astra: ASTRA_DB_ID
            """));
    }

    @TestForHumanOutput
    public void can_use_file_and_deny_overwrite(OutputType outputType) {
        verifyRun("dotenv print -f .env --db ${DatabaseName}", outputType,
            o -> o.use(mockOrg).use(mockDb).useJimfs().stdin("n"),
            c -> createEnv(c, """
            # astra: ASTRA_ORG_ID # comment
            # comment
            ORG_ID=old_org_id
            
            # astra: RANDOM_NONSENSE
            DB_NAME= # astra: ASTRA_DB_NAME comment
            
            UNKNOWN=3
            
            # astra: ASTRA_DB_ID
            """));
    }

    @TestForHumanOutput
    public void file_doesnt_ask_for_overwrite_if_empty_keys(OutputType outputType) {
        verifyRun("dotenv print -f .env --db ${DatabaseName}", outputType,
            o -> o.use(mockOrg).use(mockDb).useJimfs(),
            c -> createEnv(c, """
            ASTRA_ORG_ID=
            
            # astra: RANDOM_NONSENSE
            DB_NAME= # astra: ASTRA_DB_NAME comment
            
            UNKNOWN=
            
            # astra: ASTRA_DB_ID
            """));
    }

    @TestForHumanOutput
    public void file_doesnt_ask_for_overwrite_if_same_values(OutputType outputType) {
        verifyRun("dotenv print -f .env --db ${DatabaseName}", outputType,
            o -> o.use(mockOrg).use(mockDb).useJimfs(),
            c -> createEnv(c, """
            # astra: ASTRA_ORG_ID # comment
            ORG_ID=123e4567-e89b-12d3-a456-426614174000
            
            # astra: RANDOM_NONSENSE
            DB_NAME=two_regions_db # astra: ASTRA_DB_NAME comment
            
            UNKNOWN=3
            
            # astra: ASTRA_DB_ID
            """));
    }

    @TestForHumanOutput
    public void errors_if_invalid_key_with_key_flag(OutputType outputType) {
        verifyRun("dotenv print -k INVALID_KEY", outputType,
            o -> o.useJimfs().use(mockOrg));
    }

    @TestForHumanOutput
    public void errors_if_invalid_key_with_file_binding(OutputType outputType) {
        verifyRun("dotenv print -f .env", outputType,
            o -> o.use(mockOrg).use(mockDb).useJimfs(),
            c -> createEnv(c, """
            # astra: ASTRA_ORG_ID # comment
            ORG_ID=123e4567-e89b-12d3-a456-426614174000
            
            # astra: RANDOM_NONSENSE
            DB_NAME=two_regions_db
            
            UNKNOWN=3
            
            # astra: ASTRA_DB_ID
            """));
    }

    @TestForHumanOutput
    public void doesnt_screw_up_weird_formatting(OutputType outputType) {
        verifyRun("dotenv print -f .env", outputType,
            o -> o.use(mockOrg).use(mockDb).useJimfs(),
            c -> createEnv(c, """
            ASTRA_ORG_ID=123e4567-e89b-12d3-a456-426614174000
            
            KEY1="double quote: \\" single quote: '"
            KEY2='single quote: \\' double quote: "'
            
            #comment1
            # comment2
               #   comment3
            
            \t
            key3=value   ### #   comment
            key4="value"   ### #   comment
            """));
    }
}
