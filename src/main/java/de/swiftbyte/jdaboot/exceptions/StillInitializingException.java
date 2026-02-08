package de.swiftbyte.jdaboot.exceptions;

public class StillInitializingException extends IllegalStateException {
    public StillInitializingException() {
        super("JDABoot is still initializing");
    }
}
