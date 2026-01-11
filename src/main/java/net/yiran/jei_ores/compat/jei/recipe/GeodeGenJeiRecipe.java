package net.yiran.jei_ores.compat.jei.recipe;

import net.yiran.jei_ores.JeiOres;
import net.yiran.jei_ores.compat.jei.JEIFeaturesData;
import net.yiran.jei_ores.compat.jei.stack.IBiomeIngredient;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.placement.*;
import net.yiran.jei_ores.mixin.accessor.*;

import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("removal")
public class GeodeGenJeiRecipe extends AbstractPlacedFeatureJeiRecipe {
    public static RecipeType<JEIFeaturesData> recipeType = RecipeType.create(JeiOres.MODID, "geode", JEIFeaturesData.class);

    public GeodeGenJeiRecipe(IGuiHelper guiHelper) {
        super(
                recipeType,
                Component.translatable("jei.category.jei_ores.geode"),
                guiHelper.createDrawableItemLike(Blocks.BUDDING_AMETHYST),
                160,
                90
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, JEIFeaturesData featuresData, IFocusGroup iFocusGroup) {
        var id = featuresData.id();
        var feature = featuresData.feature();

        List<Biome> biomes = List.of();
        for (PlacementModifier modifier : feature.placement()) {
            if (modifier instanceof BiomeFilter) {
                biomes = getBiomes(id, feature);
            }
        }

        if (!biomes.isEmpty())
            builder.addSlot(RecipeIngredientRole.CATALYST, 96, 18)
                    .setStandardSlotBackground()
                    .addIngredients(
                            IBiomeIngredient.INSTANCE,
                            biomes
                    );

        GeodeConfiguration config = (GeodeConfiguration) feature.feature().value().config();

        GeodeBlockSettings blockSettings = config.geodeBlockSettings;
        GeodeLayerSettings layerSettings = config.geodeLayerSettings;

        builder.addSlot(RecipeIngredientRole.OUTPUT, 46, 36)
                .setStandardSlotBackground()
                //float chance = config.useAlternateLayer0Chance;
                .addIngredients(VanillaTypes.ITEM_STACK,
                        ingredientForStateProvider(blockSettings.alternateInnerLayerProvider)
                                .map(ItemLike::asItem)
                                .map(Item::getDefaultInstance)
                                .toList()
                )
                .addTooltipCallback((iRecipeSlotView, list) -> {
                    list.add(Component.translatable("jei_ores.chance.tooltip", config.useAlternateLayer0Chance));
                });

        builder.addSlot(RecipeIngredientRole.OUTPUT, 0, 18)
                .setStandardSlotBackground()
                .addIngredients(VanillaTypes.ITEM_STACK,
                        ingredientForStateProvider(blockSettings.fillingProvider)
                                .map(ItemLike::asItem)
                                .map(Item::getDefaultInstance)
                                .map(itemStack -> {
                                    itemStack.setCount((int) Math.ceil(layerSettings.filling));
                                    return itemStack;
                                })
                                .toList()
                );

        builder.addSlot(RecipeIngredientRole.OUTPUT, 0, 36)
                .setStandardSlotBackground()
                .addIngredients(VanillaTypes.ITEM_STACK,
                        ingredientForStateProvider(blockSettings.innerLayerProvider)
                                .map(ItemLike::asItem)
                                .map(Item::getDefaultInstance)
                                .map(itemStack -> {
                                    itemStack.setCount((int) Math.ceil(layerSettings.innerLayer - layerSettings.filling));
                                    return itemStack;
                                })
                                .toList()
                );

        builder.addSlot(RecipeIngredientRole.OUTPUT, 0, 54)
                .setStandardSlotBackground()
                .addIngredients(VanillaTypes.ITEM_STACK,
                        ingredientForStateProvider(blockSettings.middleLayerProvider)
                                .map(ItemLike::asItem)
                                .map(Item::getDefaultInstance)
                                .map(itemStack -> {
                                    itemStack.setCount((int) Math.ceil(layerSettings.middleLayer - layerSettings.innerLayer));
                                    return itemStack;
                                })
                                .toList()
                );

        builder.addSlot(RecipeIngredientRole.OUTPUT, 0, 72)
                .setStandardSlotBackground()
                .addIngredients(VanillaTypes.ITEM_STACK,
                        ingredientForStateProvider(blockSettings.outerLayerProvider)
                                .map(ItemLike::asItem)
                                .map(Item::getDefaultInstance)
                                .map(itemStack -> {
                                    itemStack.setCount((int) Math.ceil(layerSettings.outerLayer - layerSettings.middleLayer));
                                    return itemStack;
                                })
                                .toList()
                );

        builder.addSlot(RecipeIngredientRole.OUTPUT, 46, 18)
                .setStandardSlotBackground()
                //float chance = config.useAlternateLayer0Chance;
                .addIngredients(VanillaTypes.ITEM_STACK,
                        blockSettings.innerPlacements.stream()
                                .map(BlockState::getBlock)
                                .map(ItemLike::asItem)
                                .map(Item::getDefaultInstance)
                                .toList()
                )
                .addTooltipCallback((iRecipeSlotView, list) -> {
                    list.add(Component.translatable("jei_ores.chance.tooltip", config.useAlternateLayer0Chance));
                });
    }

    private static Stream<Block> ingredientForStateProvider(BlockStateProvider provider) {
        if (provider instanceof SimpleStateProvider simple) {
            return Stream.of(((SimpleStateProviderAccessor) simple).getState().getBlock());
        } else if (provider instanceof WeightedStateProvider weighted) {
            // ignore the weights
            return ((WeightedStateProviderAccessor) weighted).getWeightedList()
                    .unwrap()
                    .stream()
                    .map(WeightedEntry.Wrapper::getData)
                    .map(BlockState::getBlock);
        } else if (provider instanceof NoiseProvider noise) {
            return ((NoiseProviderAccessor) noise).getStates()
                    .stream()
                    .map(BlockState::getBlock)
                    .distinct();
        }

        return Stream.empty();
    }

    public void createRecipeExtras(IRecipeExtrasBuilder builder, JEIFeaturesData featuresData, IFocusGroup focuses) {
        var feature = featuresData.feature();

        HeightProvider heightProvider = null;
        int rarityChance = -1;

        for (PlacementModifier modifier : feature.placement()) {
            if (modifier instanceof HeightRangePlacement heightRange) {
                heightProvider = ((HeightRangePlacementAccessor) heightRange).getHeight();
            } else if (modifier instanceof RarityFilter rarityFilter) {
                rarityChance = ((RarityFilterAccessor) rarityFilter).getChance();
            }
        }

        addDistributionGraph(builder, 64, 0, heightProvider);

        Component veinFreq = getVeinFreqComponent(-1, -1, rarityChance);
        if (veinFreq != null) {
            builder.addText(veinFreq, 160, 45)
                    .setTextAlignment(HorizontalAlignment.RIGHT)
                    .setTextAlignment(VerticalAlignment.BOTTOM);
        }
    }

}
