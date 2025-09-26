package com.dtsx.astra.cli.core.upgrades;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.CliProperties;
import com.dtsx.astra.cli.core.models.Version;
import lombok.val;

import java.nio.file.Path;
import java.time.Instant;

public class UpdateStatusKeeper {
    public static void runIfNecessary(CliContext ctx, Path path, UpgradeStatus status, boolean shouldCheckForUpdate, boolean userWasAnnoyed) {
        if (!shouldCheckForUpdate && !userWasAnnoyed) {
            return;
        }

        val script = ctx.isWindows()
            ? runWindows(status, path, shouldCheckForUpdate, userWasAnnoyed)
            : runUnix(status, path, shouldCheckForUpdate, userWasAnnoyed);

       try {
           new ProcessBuilder()
               .command(ctx.isWindows() ? "cmd.exe" : "sh", ctx.isWindows() ? "/c" : "-c", script)
               .start();
       } catch (Exception e) {
           ctx.log().exception("Unable to update upgrade status file at " + path);
           ctx.log().exception("Script was:\n" + script);
           ctx.log().exception(e);
       }
    }

    public static String runUnix(UpgradeStatus status, Path path, boolean shouldCheckForUpdate, boolean userWasAnnoyed) {
        var script = """
          # just return if `curl` is not available
          command -v curl >/dev/null 2>&1 || { exit 0; }
        
          # set initial variables to update as needed
          latest_version="%s"
          last_checked=%d
          last_notified=%d
        """.formatted(
            status.latestVersion().map(Version::toString).orElse(""),
            status.lastChecked().toEpochMilli(),
            status.lastNotified().toEpochMilli()
        );

        if (shouldCheckForUpdate) {
            script += """
              # get latest release from github api
              latest_release=$(curl -s "%s/releases/latest")
              latest_version=$(echo "$latest_release" | sed -n 's/.*"tag_name":\\s*"\\([^"]*\\)".*/\\1/p')
              last_checked=%d
            """.formatted(CliProperties.cliGithubApiReposUrl(), Instant.now().toEpochMilli());
        }

        if (userWasAnnoyed) {
            script += """
              # guess
              last_notified=%d
            """.formatted(Instant.now().toEpochMilli());
        }

        script += """
          # update properties file
          echo "LATEST_VERSION=$latest_version" > "%s"
          echo "LAST_CHECKED=$last_checked" >> "%s"
          echo "LAST_NOTIFIED=$last_notified" >> "%s"
        """.formatted(
            path.toAbsolutePath(),
            path.toAbsolutePath(),
            path.toAbsolutePath()
        );

        return script;
    }

    public static String runWindows(UpgradeStatus status, Path path, boolean shouldCheckForUpdate, boolean userWasAnnoyed) {
        // TODO
        return "";
    }
}
