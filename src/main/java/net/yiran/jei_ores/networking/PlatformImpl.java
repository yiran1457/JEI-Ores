package net.yiran.jei_ores.networking;

import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class PlatformImpl {
    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}
