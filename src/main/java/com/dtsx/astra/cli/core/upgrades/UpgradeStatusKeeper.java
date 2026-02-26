package com.dtsx.astra.cli.core.upgrades;

import com.datastax.astra.client.core.vectorize.EmbeddingProvider;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.models.Version;
import lombok.val;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static com.dtsx.astra.cli.utils.StringUtils.trimIndent;

public class UpgradeStatusKeeper {
    public static void runIfNecessary(CliContext ctx, Path path, UpgradeStatus status, boolean shouldCheckForUpdate, boolean userWasAnnoyed) {
        if (!shouldCheckForUpdate && !userWasAnnoyed) {
            return;
        }

        val script = runUnix(ctx, status, path, shouldCheckForUpdate, userWasAnnoyed);

        try {
            val tempScript = Files.createTempFile("astra-upgrade-", ".sh");
            Files.writeString(tempScript, script);

            new ProcessBuilder()
                .command("sh", "-c", "nohup sh " + tempScript.toAbsolutePath() + " > /dev/null 2>&1 &")
                .start();
        } catch (Exception e) {
            ctx.log().exception("Unable to update upgrade status file at " + path);
            ctx.log().exception("Script was:\n" + script);
            ctx.log().exception(e);
        }
    }

    // I know this is prone to race conditions,
    // but the issue is, I just don't care.
    private static String runUnix(CliContext ctx, UpgradeStatus status, Path path, boolean shouldCheckForUpdate, boolean userWasAnnoyed) {
        val pathStr = path.toAbsolutePath().toString().replace("\"", "\\\"");

        var script = """
          # initiating self destruct sequence
          trap 'rm -f "$0"' EXIT

          # just return if `curl` is not available
          command -v curl >/dev/null 2>&1 || { exit 0; }

          LOCKDIR="%s.lock"

          # not perfect but doesn't matter if minor race condition occurs
          if ! mkdir "$LOCKDIR" 2>/dev/null; then
            if [ -d "$LOCKDIR" ]; then
              # backup protection against a stale lock
              if stat --version >/dev/null 2>&1; then
                lock_age=$(($(date +%%s) - $(stat -c %%Y "$LOCKDIR")))
              else
                lock_age=$(($(date +%%s) - $(stat -f %%m "$LOCKDIR")))
              fi

              if [ "$lock_age" -gt 300 ]; then
                rm -rf "$LOCKDIR"
              fi
            else
              exit 0
            fi
          fi

          trap 'rmdir "$LOCKDIR"' 0

          # set initial variables to update as needed
          latest_version="%s"
          last_checked=%d
          last_notified=%d
        """.formatted(
            pathStr,
            status.latestVersion().map(Version::toString).orElse(""),
            status.lastChecked().toEpochMilli(),
            status.lastNotified().toEpochMilli()
        );

        if (shouldCheckForUpdate) {
            script += """
               # get latest release from github api
               latest_release=$(curl -fsSL "%s/releases/latest")
               maybe_latest_version=$(echo "$latest_release" | sed -n 's/.*"tag_name".*:.*"\\(.*\\)".*/\\1/p')

               if [ -n "$maybe_latest_version" ]; then
                 latest_version="$maybe_latest_version"
                 last_checked=%d
               fi
            """.formatted(ctx.properties().cliGithubApiReposUrl(), Instant.now().toEpochMilli());
        }

        if (userWasAnnoyed) {
            script += """
              # guess
              last_notified=%d
            """.formatted(Instant.now().toEpochMilli());
        }

        script += trimIndent("""
          # update properties file
          echo "LATEST_VERSION=$latest_version
          LAST_CHECKED=$last_checked
          LAST_NOTIFIED=$last_notified" > '%s'
        """).formatted(pathStr);

        return script;
    }
}
