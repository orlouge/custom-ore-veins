package io.github.orlouge.customoreveins;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.nio.file.Path;

public class PlatformHelper {
    @ExpectPlatform
    public static Path getConfigDirectory() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static CustomOreVeinManager getCustomOreVeinManager() {
        throw new AssertionError();
    }
}
