package com.barbershop;

/**
 * Plain entry point for the application.
 *
 * <p>Launching a class that directly extends {@code Application} only works when the JavaFX
 * modules are on the module path. Routing through this small class keeps
 * {@code java -cp target/barbershop-system.jar com.barbershop.Launcher} working as well.</p>
 */
public final class Launcher {

    private Launcher() {
        // utility class
    }

    public static void main(String[] args) {
        Main.main(args);
    }
}
