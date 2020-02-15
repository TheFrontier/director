package pw.dotdash.director.core.tree;

import org.jetbrains.annotations.NotNull;
import pw.dotdash.director.core.exception.CommandException;

@FunctionalInterface
public interface CommandExecutor<S, V, R> {

    @NotNull
    R execute(@NotNull S source, @NotNull V arguments) throws CommandException;
}