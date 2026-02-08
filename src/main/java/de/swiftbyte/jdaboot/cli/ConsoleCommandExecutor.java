package de.swiftbyte.jdaboot.cli;

import org.jspecify.annotations.NonNull;

public interface ConsoleCommandExecutor {

    void onCommand(@NonNull String @NonNull [] args);
}
