package net.yiran.jei_ores.compat.jei;

import net.yiran.jei_ores.Config;
import net.yiran.jei_ores.JeiOres;
import net.yiran.jei_ores.client.FeaturesReciever;
import net.yiran.jei_ores.compat.jei.recipe.GeodeGenJeiRecipe;
import net.yiran.jei_ores.compat.jei.recipe.OreGenJeiRecipe;
import net.yiran.jei_ores.compat.jei.stack.IBiomeIngredient;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JeiPlugin
public class EmiOresEmiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(JeiOres.MODID, JeiOres.MODID);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        // 对应 registry.addCategory(...)
        registration.addRecipeCategories(
                new OreGenJeiRecipe(registration.getJeiHelpers().getGuiHelper()),
                new GeodeGenJeiRecipe(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        if (!Config.addBiomesToIndex.get()) return;

        // 关键：JEI 自定义 ingredient
        // 你大概率需要一个“BiomeIngredient(保存 biomeId 或 holder)”的包装类，而不是直接用 Biome 对象
        List<Biome> allBiomes = Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.BIOME).stream().toList();

        registration.register(
                IBiomeIngredient.INSTANCE,          // IIngredientType<BiomeIngredient>
                allBiomes,
                IBiomeIngredient.INSTANCE,  // IIngredientHelper<BiomeIngredient>
                IBiomeIngredient.INSTANCE // IIngredientRenderer<BiomeIngredient>
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // 对应 addDeferredRecipes：把 features 转成 “JEI recipe wrapper”
        Map<ResourceLocation, PlacedFeature> features = FeaturesReciever.getFeatures();
        if (features.isEmpty()) return;

        List<JEIFeaturesData> ores = new ArrayList<>();
        List<JEIFeaturesData> geodes = new ArrayList<>();

        features.forEach((id, placedFeature) -> {
            FeatureConfiguration fc = placedFeature.feature().value().config();
            if (fc instanceof OreConfiguration) {
                ores.add(new JEIFeaturesData(id,placedFeature));
            } else if (fc instanceof GeodeConfiguration) {
                geodes.add(new JEIFeaturesData(id,placedFeature));
            }
        });

        registration.addRecipes(OreGenJeiRecipe.recipeType, ores);
        registration.addRecipes(GeodeGenJeiRecipe.recipeType, geodes);
    }

}
