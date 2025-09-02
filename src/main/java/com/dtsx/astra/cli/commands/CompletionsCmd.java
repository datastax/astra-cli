package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.core.completions.DynamicCompletion;
import lombok.val;
import picocli.AutoComplete;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

import java.util.Set;
import java.util.regex.Pattern;

import static com.dtsx.astra.cli.commands.AbstractCmd.DEFAULT_VALUE;
import static com.dtsx.astra.cli.utils.StringUtils.NL;
import static com.dtsx.astra.cli.utils.StringUtils.withIndent;

@Command(
    name = "completions",
    aliases = { "compgen" },
    mixinStandardHelpOptions = true,
    hidden = true
)
public class CompletionsCmd implements Runnable {
    @Spec
    private CommandSpec spec;

    @Option(
        names = { "-n", "--cli-name" },
        description = { "CLI name to use in the completion script", DEFAULT_VALUE },
        defaultValue = "${cli.name}"
    )
    public String $cliName;

    public void run() {
        val script = AutoComplete.bash(
            $cliName,
            spec.root().commandLine()
        );

        val instances = DynamicCompletion.getInstances();

        val lines = script.split(NL);
        val estimatedSize = script.length() + (instances.size() * 100) + 500;
        val sb = new StringBuilder(estimatedSize);
        var i = 0;

        i = appendUtilityFunctions(lines, sb, i);
        appendCompletionFunctions(sb, instances);
        updateCompletions(sb, lines, instances, i);

        spec.commandLine().getOut().println(sb);
    }

    private int appendUtilityFunctions(String[] lines, StringBuilder sb, int i) {
        for (; lines[i].startsWith("#") ; i++) {
            sb.append(lines[i]).append(NL);
        }
        i++;

        sb.append("function get_profile(){ for ((i=0;i<${#COMP_WORDS[@]};i++));do [[ ${COMP_WORDS[i]} == --profile ]]&&((i+1<${#COMP_WORDS[@]}))&&echo ${COMP_WORDS[i+1]}&&return;done; echo default;};").append(NL).append(NL);
        sb.append("function get_astra_dir(){ echo ~/.astra;};").append(NL).append(NL);
        return i;
    }

    private void appendCompletionFunctions(StringBuilder sb, Set<DynamicCompletion> instances) {
        for (val instance : instances) {
            sb.append(arr(instance)).append("=()").append(NL);
            sb.append("function ").append(fn(instance)).append("() {").append(NL);
            sb.append("  [ \"${#").append(arr(instance)).append("[@]}\" -ne 0 ] && return").append(NL);
            sb.append(NL);
            sb.append(withIndent(instance.getBash(), 2).replace("OUT", arr(instance))).append(NL);
            sb.append("}").append(NL).append(NL);
        }
    }

    private void updateCompletions(StringBuilder sb, String[] lines, Set<DynamicCompletion> instances, int i) {
        record Completion(DynamicCompletion instance, Pattern pattern) {}

        val completions = instances.stream()
            .map((instance) -> new Completion(
                instance,
                Pattern.compile(" {2}local (\\w+)=\\(.*" + Pattern.quote(DynamicCompletion.marker(instance)) + ".*\\).*")
            ))
            .toList();

        outer: for (; i < lines.length; i++) {
            if (!lines[i].startsWith("  local")) {
                sb.append(lines[i]).append(NL);
                continue;
            }

            for (val completion : completions) {
                val matcher = completion.pattern.matcher(lines[i]);

                if (matcher.matches()) {
                    sb.append("  ").append(fn(completion.instance)).append(NL);
                    sb.append("  local ").append(matcher.group(1)).append("=(\"${").append(arr(completion.instance)).append("[@]}\")").append(NL);
                    continue outer;
                }
            }

            sb.append(lines[i]).append(NL);
        }
    }

    private String fn(DynamicCompletion instance) {
        return instance.getClass().getSimpleName() + "_fn";
    }

    private String arr(DynamicCompletion instance) {
        return instance.getClass().getSimpleName() + "_arr";
    }
}
