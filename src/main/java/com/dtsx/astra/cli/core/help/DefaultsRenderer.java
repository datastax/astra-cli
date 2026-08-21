package com.dtsx.astra.cli.core.help;

import com.dtsx.astra.cli.commands.CommonOptions.HelpLevel;
import com.dtsx.astra.cli.commands.user.AbstractCmd;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Help.Ansi.Text;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.Help.IParamLabelRenderer;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Model.*;

import java.util.*;

import static com.dtsx.astra.cli.commands.AbstractOperationalCmd.SHOW_CUSTOM_DEFAULT;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

@UtilityClass
public class DefaultsRenderer {
    private static final Set<String> COMMON_GROUP_HEADINGS = Set.of(
        "%nCommon Options:%n",
        "%nConnection Options:%n"
    );

    public static Help helpWithOverriddenDefaultsRendering(CommandSpec spec, ColorScheme cs) {
        return new Help(spec, cs) {
            @Override
            public IOptionRenderer createDefaultOptionRenderer() {
                val delegate = super.createDefaultOptionRenderer();

                return (option, optionLabelRenderer, cs) -> {
                    return addDefaultIfNecessary(delegate.render(option, optionLabelRenderer, cs), option, cs);
                };
            }

            @Override
            public IParameterRenderer createDefaultParameterRenderer() {
                val delegate = super.createDefaultParameterRenderer();

                return (param, parameterLabelRenderer, cs) -> {
                    return addDefaultIfNecessary(delegate.render(param, parameterLabelRenderer, cs), param, cs);
                };
            }

            @Override
            public String optionListGroupSections() {
                if (commandSpec().userObject() instanceof AbstractCmd c && c.common.helpLevel() != HelpLevel.ALL) {
                    val groups = optionSectionGroups().stream()
                        .filter(g -> !COMMON_GROUP_HEADINGS.contains(g.heading()))
                        .toList();

                    val getFullDisclaimer = NL + cs.string("@|italic Use|@ @|italic,blue:300 --help-all|@ @|italic to get the full options list|@") + NL;

                    return reflectiveOptionListGroupSections(this, groups, createDefaultOptionSort(), parameterLabelRenderer()) + getFullDisclaimer;
                }

                return super.optionListGroupSections();
            }
        };
    }

    private static Text[][] addDefaultIfNecessary(Text[][] result, ArgSpec arg, ColorScheme cs) {
        if (!arg.required() && arg.showDefaultValue() != Visibility.NEVER) {
            if (shouldShowCustomDefault(result)) {
                result[result.length - 1] = mkDefaultText(result[result.length - 1][result[result.length - 1].length - 1].plainString().substring(SHOW_CUSTOM_DEFAULT.length()), cs);
            } else if (arg.defaultValue() != null || arg.initialValue() != null) {
                if (arg.showDefaultValue() != Visibility.ALWAYS) {
                    result = Arrays.copyOf(result, result.length + 1); // if ALWAYS, then we're just overriding the line that Picocli already created for us
                }

                result[result.length - 1] = mkDefaultText(arg.defaultValueString(true), cs);
            }
        }

        return result;
    }

    private static boolean shouldShowCustomDefault(Text[][] result) {
        if (result.length > 0 && result[result.length - 1].length > 0) {
            return result[result.length - 1][result[result.length - 1].length - 1].plainString().startsWith(SHOW_CUSTOM_DEFAULT);
        }
        return false;
    }

    private static Text[] mkDefaultText(String defaultValue, ColorScheme cs) {
        val EMPTY = Ansi.OFF.new Text(0);
        return new Text[]{ EMPTY, EMPTY, EMPTY, EMPTY, cs.ansi().new Text("  @|faint (default: |@@|faint,italic " + defaultValue + "|@@|faint )|@", cs) };
    }

    @SneakyThrows
    private static String reflectiveOptionListGroupSections(Help target, List<ArgGroupSpec> groupList, Comparator<OptionSpec> optionSort, IParamLabelRenderer paramLabelRenderer) {
        // This should really be a public method but oh well.
        // I'd rather use reflection here than vendor the method as it's a little longer/more complex than I'd
        // want to copy and paste here
        val method = Help.class.getDeclaredMethod("optionListGroupSections", List.class, Comparator.class, IParamLabelRenderer.class);
        method.setAccessible(true);
        return (String) method.invoke(target, groupList, optionSort, paramLabelRenderer);
    }
}
