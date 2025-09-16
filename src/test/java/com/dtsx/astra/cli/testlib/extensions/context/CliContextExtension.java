package com.dtsx.astra.cli.testlib.extensions.context;

import com.dtsx.astra.cli.testlib.extensions.ExtensionUtils;
import lombok.val;
import net.jqwik.api.lifecycle.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.dtsx.astra.cli.testlib.extensions.context.TestCliContextOptions.emptyTestCliContextOptionsBuilder;

public class CliContextExtension implements ParameterResolver, TestInstancePostProcessor, ResolveParameterHook, AroundTryHook {
    @Override
    public boolean supportsParameter(ParameterContext pc, ExtensionContext ec) {
        val parameter = pc.getParameter();

        return parameter.isAnnotationPresent(UseTestCtx.class)
            && parameter.getType() == TestCliContext.class;
    }

    @Override // resolves method parameter usages for junit
    public Object resolveParameter(ParameterContext pc, ExtensionContext ec) {
        return addToJunitStore(mkCtx(pc.getParameter().getAnnotation(UseTestCtx.class)), ec);
    }

    @Override // resolves class field usages for junit
    public void postProcessTestInstance(Object testInstance, ExtensionContext ec) throws Exception {
        for (val field : ExtensionUtils.getAllFields(testInstance)) {
            if (field.isAnnotationPresent(UseTestCtx.class) && field.getType() == TestCliContext.class) {
                field.setAccessible(true);
                field.set(testInstance, addToJunitStore(mkCtx(field.getAnnotation(UseTestCtx.class)), ec));
            }
        }
    }

    public @NotNull TryExecutionResult aroundTry(TryLifecycleContext context, @NotNull TryExecutor aTry, @NotNull List<Object> parameters) throws Throwable {
        for (val instance : context.testInstances()) {
            for (val field : ExtensionUtils.getAllFields(instance)) {
                if (field.isAnnotationPresent(UseTestCtx.class) && field.getType() == TestCliContext.class) {
                    field.setAccessible(true);
                    field.set(instance, addToJqwikStore(mkCtx(field.getAnnotation(UseTestCtx.class))));
                }
            }
        }

        return aTry.execute(parameters);
    }

    @Override // resolves method parameter usages for jqwik
    public @NotNull Optional<ParameterSupplier> resolve(ParameterResolutionContext pc, @NotNull LifecycleContext lc) {
        val annotation = pc.typeUsage().findAnnotation(UseTestCtx.class);

        if (pc.typeUsage().isOfType(TestCliContext.class) && annotation.isPresent()) {
            return Optional.of(_ -> addToJqwikStore(mkCtx(annotation.get())));
        }

        return Optional.empty();
    }

    @Override
    public @NotNull PropagationMode propagateTo() {
        return PropagationMode.ALL_DESCENDANTS;
    }

    private record CloseOnResetCtxWrapper(TestCliContext ctx) implements Store.CloseOnReset {
        @Override
        public void close() {
            ctx.close();
        }
    }

    private TestCliContext mkCtx(UseTestCtx opts) {
        var builder = emptyTestCliContextOptionsBuilder();

        switch (opts.fs()) {
            case "jimfs" -> builder = builder.useJimfs();
            case "real" -> builder = builder.useRealFs();
        }

        return new TestCliContext(builder);
    }

    private TestCliContext addToJunitStore(TestCliContext ctx, ExtensionContext ec) {
        ec.getStore(ExtensionContext.Namespace.create(getClass(), ec.getUniqueId()))
            .put("cliContext", ctx);

        return ctx;
    }

    private TestCliContext addToJqwikStore(TestCliContext ctx) {
        return Store.getOrCreate(UUID.randomUUID(), Lifespan.TRY, () -> new CloseOnResetCtxWrapper(ctx)).get().ctx();
    }
}
