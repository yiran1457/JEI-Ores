package net.yiran.jei_ores;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.ConfigValue<Boolean> addBiomesToIndex;
    public static final ForgeConfigSpec.ConfigValue<Integer> oreGenPageHeight;
    static {
        addBiomesToIndex = BUILDER.define("addBiomesToIndex", true);
        oreGenPageHeight  = BUILDER.define("oreGenPageHeight", 57);
        SPEC = BUILDER.build();
    }
}
