package com.dtsx.astra.cli.core.help;

import lombok.experimental.UtilityClass;
import lombok.val;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Help.Ansi.Text;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;

import java.util.Arrays;

import static com.dtsx.astra.cli.commands.AbstractCmd.SHOW_CUSTOM_DEFAULT;

@UtilityClass
public class DefaultsRenderer {
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
        };
    }

    private static Text[][] addDefaultIfNecessary(Text[][] result, ArgSpec arg, ColorScheme cs) {
        if (!arg.required() && arg.showDefaultValue() != Visibility.NEVER) {
            if (shouldShowCustomDefault(result)) {
                result[result.length - 1] = mkDefaultText(result[result.length - 1][result[result.length - 1].length - 1].plainString().substring(SHOW_CUSTOM_DEFAULT.length()), cs);
            }
            else if (arg.defaultValue() != null || arg.initialValue() != null) {
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
}
